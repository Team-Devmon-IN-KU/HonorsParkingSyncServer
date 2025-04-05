package org.example.honorsparkingsyncserver.sync.inout.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
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

  private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

  private final MongoOperations mongoOperations;
  private final SyncUpdateService syncHelperService;
  private final SyncNewService syncNewService;

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
      syncNewService.syncNewData();

      logger.info("Sync process completed successfully");
    } catch (Exception e) {
      logger.error("Error occurred during sync process", e);
    }
  }

}
