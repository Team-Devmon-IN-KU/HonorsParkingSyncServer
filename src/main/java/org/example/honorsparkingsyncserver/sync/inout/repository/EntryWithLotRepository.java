package org.example.honorsparkingsyncserver.sync.inout.repository;

import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsWithLotEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntryWithLotRepository extends JpaRepository<EntryMainRecordsWithLotEntity, Long> {

    @Query("SELECT e FROM EntryMainRecordsWithLotEntity e " +
            "JOIN FETCH e.parkingLot pl " +
            "WHERE e.vehicleNumber = :vehicleNumber " +
            "AND e.parkingStatus = '0' " +
            "AND e.entryId NOT IN (" +
            "   SELECT ex.entryMainRecord.entryId FROM ExitMainRecordEntity ex" +
            ") " +
            "ORDER BY e.entryTime DESC")
    List<EntryMainRecordsWithLotEntity> findActiveWithJoin(@Param("vehicleNumber") String vehicleNumber);
}
