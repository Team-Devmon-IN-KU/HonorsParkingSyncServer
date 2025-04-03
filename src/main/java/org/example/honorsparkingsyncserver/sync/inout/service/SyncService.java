package org.example.honorsparkingsyncserver.sync.inout.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLastReadInoutLog;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.EntryRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLastReadInoutLogRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLogTableRepository;
import org.example.honorsparkingsyncserver.sync.inout.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncService {

  private static final int BATCH_SIZE = 1000;
  private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

  private final MongoLogTableRepository mongoLogTableRepository;
  private final EntryRepository parkingInoutRepository;
  private final MongoLastReadInoutLogRepository mongoLastReadInoutLogRepository;
  private final MongoOperations mongoOperations;
  private final SyncHelperService syncHelperService;

  // id중복 저장 시도 시에 update쿼리 보내는 bulk 연산
  public void saveAllBulkOps(List<MongoLogTableEntity> entities) {
    BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED,
        MongoLogTableEntity.class);

    for (MongoLogTableEntity entity : entities) {
      Query query = new Query(Criteria.where("_id").is(entity.getEntryId())); // ID 기준으로 찾음
      Update update = new Update()
          .set("lastHash", entity.getLastHash()); // 업데이트할 필드들 설정

      bulkOps.upsert(query, update);
    }

    bulkOps.execute();
  }

  // 매일 1분마다 실행 (스케줄러로 주기적인 동기화)
  @Scheduled(fixedDelay = 10000) // 6초마다 실행
  @Transactional
  public void syncData() {
    try {
      logger.info("Sync process started");
      /*
       * Section 1. update가 될 것같은 로그들을 가져와서 변화가 있다면 동기화 해주기
       * mongoDB에 update가 가능할만한 데이터들을 저장하고 있음
       * 1000개씩 반복하며 update
       * */

      syncHelperService.syncUpdatableData();

      /*
       * Section 2. 최신 데이터 1000개씩 읽어와서 동기화 하기
       * mongoDB에 lastReadInout 테이블에 마지막에 읽은 인덱스를 저장하고 있음
       * 이 때 읽을 때는 entry_id 필드 기준이 아니라 id필드 기준으로 가져올 거임
       * */
      List<MongoLogTableEntity> newParkingLogEntities = getLogsNew();

      if (!newParkingLogEntities.isEmpty()) {
        saveUpdatableLogsToMongo(newParkingLogEntities);

        // newEntities에서 가장 큰 entryId 값 찾기
        Long maxEntryId = newParkingLogEntities.stream()
            .mapToLong(MongoLogTableEntity::getEntryId)
            .max()
            .orElseThrow(() -> new IllegalStateException("No entryId found"));

        // 가장 큰 entryId 값으로 setLastReadEntityId 설정 후 저장
        mongoLastReadInoutLogRepository.save(
            MongoLastReadInoutLog.builder().lastReadEntityId(maxEntryId).id(1L).build());

        logger.info("Inserted {} new logs into MongoDB", newParkingLogEntities.size());
      }

      logger.info("Sync process completed successfully");
    } catch (Exception e) {
      logger.error("Error occurred during sync process", e);
    }
  }

  private List<MongoLogTableEntity> getLogsNew() {

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

    Long latestId = mongoLastReadInoutLog.getLastReadEntityId();
    logger.info("Last read ID from MongoDB: {}", latestId);

    // 2. 새로운 엔티티들 (MongoDB에 없는 ID) READ from MS 디비
    List<EntryMainRecordsEntity> newParkingLogsInMsDB = parkingInoutRepository.findLimitedByEntryIdGreaterThan(
        latestId, BATCH_SIZE, 0);

    // 6️⃣ 새로운 데이터들을 MongoDB에 저장
    List<MongoLogTableEntity> newParkingLogEntities = newParkingLogsInMsDB.stream()
        .map(log -> MongoLogTableEntity.builder()
            .entryId(log.getEntryId())
            .lastHash(generateHash(log))
            .build())
        .collect(Collectors.toList());

    return newParkingLogEntities;
  }

  // 최대 1000개씩 저장
  public void saveUpdatableLogsToMongo(List<MongoLogTableEntity> logs) {
    try {
      for (int i = 0; i < logs.size(); i += BATCH_SIZE) {
        int end = Math.min(i + BATCH_SIZE, logs.size());
        List<MongoLogTableEntity> batch = logs.subList(i, end);
        saveAllBulkOps(batch); // 배치 단위로 저장
      }
    } catch (Exception e) {
      logger.error("Failed to save logs to MongoDB", e);
      throw new RuntimeException("Failed to save logs to MongoDB", e);
    }
  }

  private String generateHash(EntryMainRecordsEntity log) {
    // 해시 값을 생성하는 로직 구현
    return HashUtil.generateHash(log);
  }

}
