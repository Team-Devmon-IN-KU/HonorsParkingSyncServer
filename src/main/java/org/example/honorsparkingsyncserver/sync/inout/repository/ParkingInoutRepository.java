package org.example.honorsparkingsyncserver.sync.inout.repository;

import java.util.List;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingInoutRepository extends JpaRepository<EntryMainRecordsEntity, Long> {

  List<EntryMainRecordsEntity> findAllByEntryIdGreaterThan(Long entryId);

  List<EntryMainRecordsEntity> findAllByEntryIdIn(List<Long> entryIds);

}
