package org.example.honorsparkingsyncserver.sync.inout.repository.mongo;

import java.util.List;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoLogTableRepository extends MongoRepository<MongoLogTableEntity, Long> {

  // ✅ 1000개씩 가져오는 페이징 메서드 추가
  @Query(value = "{}", sort = "{'entryId': 1}")
  // `_id` 기준 오름차순 정렬
  List<MongoLogTableEntity> findAllPaged(Pageable pageable);

  @Modifying
  void deleteByEntryIdIn(List<Long> entryIds);

  List<MongoLogTableEntity> findByEntryIdIn(List<Long> entryIds);
}
