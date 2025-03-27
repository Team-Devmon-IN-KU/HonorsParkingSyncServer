//package org.example.honorsparkingsyncserver.sync.inout.repository.mongo;
//
//import java.util.List;
//import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
//import org.springframework.data.mongodb.core.BulkOperations;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.mongodb.repository.Update;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public class CustomLogTableRepository {
//
//  private final MongoTemplate mongoTemplate;
//
//  public CustomLogTableRepository(MongoTemplate mongoTemplate) {
//    this.mongoTemplate = mongoTemplate;
//  }
//
//  public void saveAllEntities(List<MongoLogTableEntity> entities) {
//    BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
//        MongoLogTableEntity.class);
//
//    for (MongoLogTableEntity entity : entities) {
//      Query query = new Query().addCriteria(Criteria.where("_id").is(entity.getEntryId()));
//      Update update = new Update();
//      update.set("lastHash", entity.getLastHash()); // 업데이트할 필드 추가
//
//      bulkOps.upsert(query, update);
//    }
//
//    bulkOps.execute();
//  }
//}
