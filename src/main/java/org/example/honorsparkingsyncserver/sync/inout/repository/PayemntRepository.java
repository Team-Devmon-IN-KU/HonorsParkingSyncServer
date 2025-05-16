package org.example.honorsparkingsyncserver.sync.inout.repository;

import java.util.List;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.PaymentRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayemntRepository extends JpaRepository<PaymentRecord, Long> {

  @EntityGraph(attributePaths = {"entryMainRecord"})
  List<PaymentRecord> findAllByEntryMainRecord_EntryIdIn(List<Long> entryIds);
}
