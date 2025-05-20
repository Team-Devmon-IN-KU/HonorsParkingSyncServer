package org.example.honorsparkingsyncserver.sync.inout.repository;

import org.example.honorsparkingsyncserver.sync.inout.domain.entity.FeeStructureCoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeStructureCoreRepository extends JpaRepository<FeeStructureCoreEntity, String> {
    FeeStructureCoreEntity findByFeeCode(String feeCode);
}
