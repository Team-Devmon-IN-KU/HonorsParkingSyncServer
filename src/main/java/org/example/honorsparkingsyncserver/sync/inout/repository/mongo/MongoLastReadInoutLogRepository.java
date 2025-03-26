package org.example.honorsparkingsyncserver.sync.inout.repository.mongo;

import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLastReadInoutLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoLastReadInoutLogRepository extends
    MongoRepository<MongoLastReadInoutLog, Long> {

//  Optional<MongoLastReadInoutLog> findByInoutId(Long inoutId);
}
