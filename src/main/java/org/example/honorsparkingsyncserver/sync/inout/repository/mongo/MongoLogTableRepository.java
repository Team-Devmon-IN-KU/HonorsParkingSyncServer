package org.example.honorsparkingsyncserver.sync.inout.repository.mongo;

import java.util.Optional;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoLogTableRepository extends MongoRepository<MongoLogTableEntity, String> {

  Optional<MongoLogTableEntity> findTopByOOrderByIdDesc();
}
