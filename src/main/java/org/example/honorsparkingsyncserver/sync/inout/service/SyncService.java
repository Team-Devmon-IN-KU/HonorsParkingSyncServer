package org.example.honorsparkingsyncserver.sync.inout.service;

import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLogTableRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncService {

  MongoLogTableRepository mongoLogTableRepository;

  private void createAndSync(EntryMainRecordsEntity entryMainRecordsEntity) {
    if (postSyncApi(entryMainRecordsEntity)) {
      MongoLogTableEntity newLog = MongoLogTableEntity.builder()
          .id(entryMainRecordsEntity.getEntryId())
//          .lastHash(entryMainRecordsEntity.getHash())
          .lastHash("testhashhash")
          .build();
      mongoLogTableRepository.save(newLog);
    }
  }

  private void updateAndSync(EntryMainRecordsEntity entryMainRecordsEntity) {
    if (postSyncApi(entryMainRecordsEntity)) {
      MongoLogTableEntity updatedLog = MongoLogTableEntity.builder()
          .id(entryMainRecordsEntity.getEntryId())
//          .lastHash(entryMainRecordsEntity.getHash())
          .lastHash("testhashhash")
          .build();
      mongoLogTableRepository.save(updatedLog);
    }
  }

  private boolean postSyncApi(EntryMainRecordsEntity log) {
    try {
//      나중에 구현 예정
//      restTemplate.postForObject(SYNC_API_URL, log, Void.class);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
