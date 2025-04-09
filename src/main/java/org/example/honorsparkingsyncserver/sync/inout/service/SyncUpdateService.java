package org.example.honorsparkingsyncserver.sync.inout.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncInoutDataDTO;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.ExitMainRecordEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.PaymentRecord;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.ExitRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.PayemntRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLogTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncUpdateService {

  private static final Logger logger = LoggerFactory.getLogger(SyncUpdateService.class);
  private final MongoLogTableRepository mongoLogTableRepository;
  private final ExitRepository exitRepository;
  private final PayemntRepository payemntRepository;
  private final SyncFetchService syncFetchService;

  private static final int BATCH_SIZE = 500;

  /*
   * Section 1. update가 될 것같은 로그들을 가져와서 변화가 있다면 동기화 해주기
   * mongoDB에 update가 가능할만한 데이터들을 저장하고 있음
   * 1000개씩 반복하며 update
   * */
  @Transactional(propagation = Propagation.REQUIRED)
  public void syncUpdatableData() {
    logger.info("Update Sync process started");

    // 1. 업데이트가 필요한 주차 로그 가져오기
    List<SyncInoutDataDTO> logsToUpdate = getLogsToUpdateByMongo();
    int totalSize = logsToUpdate.size();
    int offset = 0;

    if (logsToUpdate.isEmpty()) {
//      logger.info("No logs to update.");
      return;
    }

    logger.info("Total logs to update: {}", totalSize);

    // 2. 1000개씩 배치 전송
    while (offset < totalSize) {
      List<SyncInoutDataDTO> batch = logsToUpdate.subList(offset,
          Math.min(offset + BATCH_SIZE, totalSize));

      try {
        syncFetchService.sendBatchToServer(batch);
      } catch (Exception e) {
        logger.error("🚨 배치 동기화 중 오류 발생. entryId 범위: {} ~ {}", offset, offset + BATCH_SIZE, e);
      }

      offset += BATCH_SIZE;
    }

    // 3. MongoDB에서 동기화된 데이터 삭제
    deleteSyncedLogs(logsToUpdate);
  }

  /**
   * ✅ MongoDB 데이터 삭제 (최적화)
   *
   * @param logsToUpdate
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteSyncedLogs(List<SyncInoutDataDTO> logsToUpdate) {
    List<Long> entryIds = logsToUpdate.stream()
        .map(SyncInoutDataDTO::getEntryId)
        .collect(Collectors.toList());

    if (entryIds.isEmpty()) {
//      logger.warn("⚠️ 삭제할 로그가 없습니다.");
      return;
    }

    try {
      mongoLogTableRepository.deleteByEntryIdIn(entryIds);
      logger.info("🗑️ MongoDB에서 {}개 데이터 삭제 완료 (Entry IDs: {})", entryIds.size(), entryIds);
    } catch (Exception e) {
      logger.error("❌ MongoDB 로그 삭제 중 예외 발생: {}", e.getMessage(), e);
      throw e;  // 트랜잭션 롤백을 위해 예외 다시 던짐
    }
  }

  /**
   * @return 몽고디비에 아직 입차중인 데이터를 가져온 후 출차가 된(업데이트된) 데이터들을 반환하는 메서드
   */
  private List<SyncInoutDataDTO> getLogsToUpdateByMongo() {
    int offset = 0;
    List<SyncInoutDataDTO> logsToUpdate = new ArrayList<>();

    while (true) {
      // 1️⃣ BATCH_SIZE 개씩 데이터 READ
      Pageable pageable = PageRequest.of(offset / BATCH_SIZE, BATCH_SIZE);
      List<MongoLogTableEntity> updatableLogsInMongo = mongoLogTableRepository.findAllPaged(
          pageable);

      // update할 데이터가 없다면 종료
      if (updatableLogsInMongo.isEmpty()) {
        break;
      }

      // 2️⃣ entryId 리스트 생성
      List<Long> entryIds = updatableLogsInMongo.stream()
          .map(MongoLogTableEntity::getEntryId)
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
      List<SyncInoutDataDTO> updatedInoutDataDTOs = exitRecordMap.keySet().stream()
          .map(entryId -> {
            ExitMainRecordEntity exitRecord = exitRecordMap.get(entryId);
            PaymentRecord paymentRecord = paymentRecordMap.get(entryId);

            return SyncInoutDataDTO.builder()
                .vehicleNumber(exitRecord.getVehicleNumber())
                .entryTime(exitRecord.getEntryMainRecord().getEntryTime())
                .entryId(entryId)
                .exitTime(exitRecord.getExitTime())
                .fee(paymentRecord != null ? paymentRecord.getPaymentFee() : null)
                .paidAt(paymentRecord != null ? paymentRecord.getPaymentTime() : null)
                .parkinglotId(Long.parseLong(exitRecord.getExitLotId())) // 숫자 변환 실패가능성 있음!!
                .build();
          })
          .collect(Collectors.toList());

      logsToUpdate.addAll(updatedInoutDataDTOs); // ✅ 한 번에 추가

      offset += BATCH_SIZE; // 다음 1000개 처리
    }

    return logsToUpdate;
  }

}
