package org.example.honorsparkingsyncserver.sync.unit.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLogTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@DataMongoTest
public class MongoLogTableRepositoryTest extends InitMongoRepositoryTest {

  @Autowired
  private MongoLogTableRepository mongoLogTableRepository;
  @Autowired
  private MongoOperations mongoOperations;

  @BeforeEach
  void setUp() {
    BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED,
        MongoLogTableEntity.class);
    for (Long i = 1L; i <= 30L; i++) {
      Query query = new Query(Criteria.where("_id").is(i));
      Update update = new Update().set("lastHash", "testhashvalue");

      bulkOps.upsert(query, update);
    }
    bulkOps.execute();
  }

  @Test
  @DisplayName("페이지 요청에 맞는 값이 순차적으로 잘 오는지 확인")
  void testFindAllPaged() {
    // given + when
    Pageable pageable1 = PageRequest.of(0, 10); // 첫 페이지, 10개씩
    Pageable pageable2 = PageRequest.of(2, 10); // 첫 페이지, 10개씩
    List<MongoLogTableEntity> results1 = mongoLogTableRepository.findAllPaged(pageable1);
    List<MongoLogTableEntity> results2 = mongoLogTableRepository.findAllPaged(pageable2);

    // then
    assertThat(results1).hasSize(10);
    assertThat(results1.get(0).getEntryId()).isEqualTo(1L);
    assertThat(results1.get(9).getEntryId()).isEqualTo(10L);

    assertThat(results2).hasSize(10);
    assertThat(results2.get(0).getEntryId()).isEqualTo(21L);
    assertThat(results2.get(9).getEntryId()).isEqualTo(30L);
  }

  @Test
  @DisplayName("MongoLogTable 삭제가 잘 수행되는지 확인")
  void testDeleteLog() {
    // given
    List<Integer> allIds = IntStream.rangeClosed(1, 30).boxed().collect(Collectors.toList());
    Collections.shuffle(allIds);
    List<Long> randomIdsToDelete = allIds.subList(0, 10).stream().map(Long::valueOf)
        .collect(Collectors.toList());

    // when
    mongoLogTableRepository.deleteByEntryIdIn(randomIdsToDelete);

    // then

    List<MongoLogTableEntity> foundLogs = mongoLogTableRepository.findByEntryIdIn(
        randomIdsToDelete);
    assertThat(foundLogs).isEmpty();
  }
}
