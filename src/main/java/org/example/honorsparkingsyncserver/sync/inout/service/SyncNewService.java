package org.example.honorsparkingsyncserver.sync.inout.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncInoutDataDTO;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.ExitMainRecordEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.PaymentRecord;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLastReadInoutLog;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.EntryRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.ExitRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.PayemntRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLastReadInoutLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class SyncNewService {

  private final MongoLastReadInoutLogRepository mongoLastReadInoutLogRepository;
  private final EntryRepository parkingInoutRepository;
  private final ExitRepository exitRepository;
  private final PayemntRepository payemntRepository;
  private final SyncFetchService syncFetchService;

  private final MongoOperations mongoOperations;


  private static final int BATCH_SIZE = 1000;
  private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

  @Value("${sync.url}")
  private String SYNC_URL;
  @Value("${sync.api-key}")
  private String API_KEY;
  @Value("${sync.header-name}")
  private String HEADER_NAME;


  @Transactional(propagation = Propagation.REQUIRED)
  public void syncNewData() {
    logger.info("New data Sync process started");

    // 1. 동기화가 필요한 최신 주차 로그 ID 가져오기
    Long latestInoutId = getLatestInoutId();
    // 2. 업데이트가 필요한 주차 로그 가져오기
    List<SyncInoutDataDTO> logsToUpdate = getLogsNew(latestInoutId);

    if (!logsToUpdate.isEmpty()) {
      // 3. Updatable한 데이터 몽고 DB로 저장
      saveUpdatableInoutDataToMongo(logsToUpdate);

      // 4. 가장 큰 entryId 값으로 setLastReadEntityId 설정 후 저장
      Long maxEntryId = logsToUpdate.stream()
          .mapToLong(SyncInoutDataDTO::getEntryId)
          .max()
          .orElseThrow(() -> new IllegalStateException("No entryId found"));

      mongoLastReadInoutLogRepository.save(
          MongoLastReadInoutLog.builder().lastReadEntityId(maxEntryId).id(1L).build());
      logger.info("Inserted new ID logs into MongoDB", maxEntryId);

      // 5. 앱 서버로 동기화 요청
      try {
        syncFetchService.sendBatchToServer(logsToUpdate);
        logger.info("Inserted {} new logs into App server", logsToUpdate.size());
      } catch (Exception e) {
        logger.error("🚨 새로운 데이터 동기화 중 오류 발생 {} ", e.getMessage());
      }
    }


  }

  /**
   * @return MongoDB에서 데이터를 읽어와 최신 ID 반환하는 메서드
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public Long getLatestInoutId() {
    // 1. 가장 마지막에 읽어왔던 최신 ID 불러오기 없다면 0
    MongoLastReadInoutLog mongoLastReadInoutLog = mongoLastReadInoutLogRepository.findById(1L)
        .orElseGet(() -> {
          // 없으면 새로 생성하여 저장
          MongoLastReadInoutLog newLog = MongoLastReadInoutLog.builder()
              .id(1L)
              .lastReadEntityId(0L)
              .build();
          return mongoLastReadInoutLogRepository.save(newLog);
        });

    return mongoLastReadInoutLog.getLastReadEntityId();
  }

  /**
   * @param latestInoutId
   * @return 최신 데이터 입차, 출차, 결제정보를 조합한 Sync api request형태의 배열로 반환
   */
  @Transactional(readOnly = true)
  public List<SyncInoutDataDTO> getLogsNew(Long latestInoutId) {

    logger.info("Last read ID from MongoDB: {}", latestInoutId);

    // 1. 새로운 Entry 리스트 (MongoDB에 없는 ID) READ from MS 디비
    List<EntryMainRecordsEntity> newParkingLogsInMsDB = parkingInoutRepository.findLimitedByEntryIdGreaterThan(
        latestInoutId, BATCH_SIZE, 0);

    // 2️⃣ entryId 리스트 생성
    List<Long> entryIds = newParkingLogsInMsDB.stream().map(EntryMainRecordsEntity::getEntryId)
        .collect(Collectors.toList());

    // 3️⃣ 외부 DB에서 READ (Exit + Payment 데이터 조회)
    List<ExitMainRecordEntity> exitMainRecordEntities = exitRepository.findAllByEntryMainRecord_EntryIdIn(
        entryIds);
    List<PaymentRecord> paymentRecordEntities = payemntRepository.findAllByEntryMainRecord_EntryIdIn(
        entryIds);

    // 4️⃣ entryId를 Key로 한 Map 생성 (Exit, Payment)
    Map<Long, ExitMainRecordEntity> exitRecordMap = exitMainRecordEntities.stream()
        .collect(Collectors.toMap(
            exit -> exit.getEntryMainRecord().getEntryId(),
            Function.identity(),
            (existing, replacement) -> existing // 기존 값 유지
        ));

    Map<Long, PaymentRecord> paymentRecordMap = paymentRecordEntities.stream()
        .collect(Collectors.toMap(
            pay -> pay.getEntryMainRecord().getEntryId(),
            Function.identity(),
            (existing, replacement) -> existing // 기존 값 유지
        ));

    // 5️⃣ DTO 리스트 생성 후 logsToUpdate에 추가
    List<SyncInoutDataDTO> logsToUpdate = newParkingLogsInMsDB.stream()
        .map(inout -> {
          ExitMainRecordEntity exitRecord = exitRecordMap.get(inout.getEntryId());
          PaymentRecord paymentRecord = paymentRecordMap.get(inout.getEntryId());

          return SyncInoutDataDTO.builder()
              .vehicleNumber(inout.getVehicleNumber())
              .entryTime(inout.getEntryTime())
              .entryId(inout.getEntryId())
              .exitTime(exitRecord != null ? exitRecord.getExitTime() : null)
              .fee(paymentRecord != null ? paymentRecord.getPaymentFee() : null)
              .paidAt(paymentRecord != null ? paymentRecord.getPaymentTime() : null)
              .parkinglotId(Long.parseLong(inout.getEntryLotId()))
              .build();
        })
        .collect(Collectors.toList());

    return logsToUpdate;
  }

  /**
   * 최신 데이터중 업테이트가능한(입차중인) Inout데이터를 몽고 DB에 저장
   *
   * @param newInoutDataDTOList 입차 출차 결제정보가 조합된 DTO 리스트
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public void saveUpdatableInoutDataToMongo(List<SyncInoutDataDTO> newInoutDataDTOList) {

    List<MongoLogTableEntity> updatableInoutData = newInoutDataDTOList.stream()
        .filter(inout -> inout.getExitTime() == null)
        .map(updatableInout -> MongoLogTableEntity.builder()
            .entryId(updatableInout.getEntryId())
            // 필요한 필드 추가로 세팅
            .build()
        )
        .collect(Collectors.toList());

    if (!updatableInoutData.isEmpty()) {
      BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED,
          MongoLogTableEntity.class);

      for (MongoLogTableEntity entity : updatableInoutData) {
        Query query = new Query(Criteria.where("_id").is(entity.getEntryId())); // ID 기준으로 찾음
        Update update = new Update()
            .set("lastHash", "testhashvalue");

        bulkOps.upsert(query, update);
      }

      bulkOps.execute(); // 안전하게 실행
    } else {
      logger.info("🚨 updatableInoutData가 비어 있어서 bulkOps 실행을 생략합니다.");
    }
  }

}