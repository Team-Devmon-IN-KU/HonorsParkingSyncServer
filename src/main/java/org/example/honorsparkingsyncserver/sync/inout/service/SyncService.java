package org.example.honorsparkingsyncserver.sync.inout.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLastReadInoutLog;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.ParkingInoutRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLastReadInoutLogRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLogTableRepository;
import org.example.honorsparkingsyncserver.sync.inout.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncService {

  private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

  private final MongoLogTableRepository mongoLogTableRepository;

  private final ParkingInoutRepository parkingInoutRepository;

  private final MongoLastReadInoutLogRepository mongoLastReadInoutLogRepository;

  // 2️⃣ 매일 1분마다 실행 (스케줄러로 주기적인 동기화)
  @Scheduled(fixedRate = 6000) // 6초마다 실행
  @Transactional
  public void syncData() {
    try {
      logger.info("Sync process started");

      // 1️⃣ MongoDB에서 존재하는 ID(업데이트 가능한 엔티티) 모두 가져오기
      List<MongoLogTableEntity> existingLogs = mongoLogTableRepository.findAll(); // 모든 엔티티를 가져옴
      logger.info("Fetched {} existing logs from MongoDB", existingLogs.size());

      // 2️⃣ Inout Repository에서 해당 ID가 최신화 되었는지 가져오기
      List<Long> logIds = existingLogs.stream()
          .map(MongoLogTableEntity::getId)
          .collect(Collectors.toList());

      List<EntryMainRecordsEntity> updatableLogs = parkingInoutRepository.findAllByEntryIdIn(
          logIds);
      logger.info("Fetched {} updatable logs from Inout Repository", updatableLogs.size());

      // 3️⃣ 기존 데이터와 최신화된 데이터의 hash 값을 비교 후 업데이트가 필요한 경우 `update` 리스트에 추가
      List<MongoLogTableEntity> logsToUpdate = updatableLogs.stream()
          .map(log -> {
            Optional<MongoLogTableEntity> existingLogOpt = existingLogs.stream()
                .filter(existingLog -> existingLog.getId().equals(log.getEntryId()))
                .findFirst();

            if (existingLogOpt.isPresent()) {
              MongoLogTableEntity existingLog = existingLogOpt.get();
              if (!existingLog.getLastHash().equals(generateHash(log))) {
                existingLog.setLastHash(generateHash(log));
                existingLog.setIsUpdatable(true);
                return existingLog;
              }
            }
            return null;
          })
          .filter(log -> log != null)
          .collect(Collectors.toList());

      // 4️⃣ 업데이트가 필요한 데이터들을 MongoDB에 한 번에 저장
      if (!logsToUpdate.isEmpty()) {
        saveLogsToMongo(logsToUpdate);
        logger.info("Updated {} logs in MongoDB", logsToUpdate.size());
      }

      // 마지막 읽은 ID를 가져오기
      MongoLastReadInoutLog mongoLastReadInoutLog = mongoLastReadInoutLogRepository.findById(1L)
          .orElseGet(() -> {
            // 없으면 새로 생성하여 저장
            MongoLastReadInoutLog newLog = MongoLastReadInoutLog.builder()
                .id(1L)
                .lastReadEntryId(0L)
                .build();
            return mongoLastReadInoutLogRepository.save(newLog);
          });

      Long latestId = mongoLastReadInoutLog.getLastReadEntryId();
      logger.info("Last read ID from MongoDB: {}", latestId);

      // 5️⃣ 새로운 엔티티들 (MongoDB에 없는 ID) 추가
      List<EntryMainRecordsEntity> newLogs = parkingInoutRepository.findLimitedByEntryIdGreaterThan(
          latestId, 1000, 0);

      List<MongoLogTableEntity> newEntities = newLogs.stream()
          .map(log -> MongoLogTableEntity.builder()
              .id(log.getEntryId())
              .lastHash(generateHash(log))
              .isUpdatable(true)
              .build())
          .collect(Collectors.toList());

      // 6️⃣ 새로운 데이터들을 MongoDB에 저장
      if (!newEntities.isEmpty()) {
        saveLogsToMongo(newEntities);
        // 새로운 엔티티를 넣는 데에 성공했다면
        mongoLastReadInoutLog.setLastReadEntryId(latestId + newEntities.size());
        mongoLastReadInoutLogRepository.save(mongoLastReadInoutLog);
        logger.info("Inserted {} new logs into MongoDB", newEntities.size());
      }

      logger.info("Sync process completed successfully");
    } catch (Exception e) {
      logger.error("Error occurred during sync process", e);
      // 트랜잭션이 적용되었기 때문에 예외 발생 시 롤백됨
    }
  }

  private void saveLogsToMongo(List<MongoLogTableEntity> logs) {
    try {
      mongoLogTableRepository.saveAll(logs); // 한 번에 저장
    } catch (Exception e) {
      logger.error("Failed to save logs to MongoDB", e);
      throw new RuntimeException("Failed to save logs to MongoDB", e); // 예외를 던져서 트랜잭션 롤백
    }
  }

  private String generateHash(EntryMainRecordsEntity log) {
    // 해시 값을 생성하는 로직 구현
    return HashUtil.generateHash(log);
  }

//  private void createAndSync(EntryMainRecordsEntity entryMainRecordsEntity) {
//    if (postSyncApi(entryMainRecordsEntity)) {
//      MongoLogTableEntity newLog = MongoLogTableEntity.builder()
//          .id(entryMainRecordsEntity.getEntryId())
////          .lastHash(entryMainRecordsEntity.getHash())
//          .lastHash("testhashhash")
//          .build();
//      mongoLogTableRepository.save(newLog);
//    }
//  }

//  private void updateAndSync(EntryMainRecordsEntity entryMainRecordsEntity) {
//    if (postSyncApi(entryMainRecordsEntity)) {
//      MongoLogTableEntity updatedLog = MongoLogTableEntity.builder()
//          .id(entryMainRecordsEntity.getEntryId())
////          .lastHash(entryMainRecordsEntity.getHash())
//          .lastHash("testhashhash")
//          .build();
//      mongoLogTableRepository.save(updatedLog);
//    }
//  }

//  private boolean postSyncApi(EntryMainRecordsEntity log) {
//    try {
////      나중에 구현 예정
////      restTemplate.postForObject(SYNC_API_URL, log, Void.class);
//      return true;
//    } catch (Exception e) {
//      return false;
//    }
//  }
}
