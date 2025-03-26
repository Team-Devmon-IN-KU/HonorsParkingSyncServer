package org.example.honorsparkingsyncserver.sync.inout.repository;

import java.util.List;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingInoutRepository extends JpaRepository<EntryMainRecordsEntity, Long> {

  @Query(value = "SELECT * FROM Entry_main_records " +
      "WHERE entry_id > :entry_id " +
      "ORDER BY entry_id ASC " +
      "OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY",
      nativeQuery = true)
  List<EntryMainRecordsEntity> findLimitedByEntryIdGreaterThan(
      @Param("entry_id") Long entryId,
      @Param("limit") int limit,
      @Param("offset") int offset);

  List<EntryMainRecordsEntity> findAllByEntryIdIn(List<Long> entryIds);

}
