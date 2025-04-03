package org.example.honorsparkingsyncserver.sync.inout.repository;

import java.util.List;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.ExitMainRecordEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExitRepository extends JpaRepository<ExitMainRecordEntity, Long> {

  @EntityGraph(attributePaths = {"entryMainRecord"})
  List<ExitMainRecordEntity> findAllByEntryMainRecord_EntryIdIn(List<Long> entryIds);
}
