package org.example.honorsparkingsyncserver.sync.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncInoutDataDTO;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.ExitMainRecordEntity;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.PaymentRecord;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLastReadInoutLog;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.mongo.MongoLogTableEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.EntryRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.ExitRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.PayemntRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.mongo.MongoLastReadInoutLogRepository;
import org.example.honorsparkingsyncserver.sync.inout.service.SyncFetchService;
import org.example.honorsparkingsyncserver.sync.inout.service.SyncNewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
public class SyncNewServceTest {

  @InjectMocks
  private SyncNewService syncNewService;
  @Mock
  private MongoLastReadInoutLogRepository lastReadInoutLogRepository;
  @Mock
  private EntryRepository entryRepository;
  @Mock
  private ExitRepository exitRepository;
  @Mock
  private PayemntRepository payemntRepository;
  @Mock
  private MongoLastReadInoutLog mongoLastReadInoutLog;
  @Mock
  private MongoOperations mongoOperations;
  @Mock
  private SyncFetchService syncFetchService;


  @Test
  @DisplayName("LastRead가 DB에 없을 경우 처리검증 테스트")
  void getLatestInoutId_shouldReturnDefaultIfNotExists() {
    // given
    when(lastReadInoutLogRepository.findById(1L))
        .thenReturn(Optional.empty());
    when(lastReadInoutLogRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    Long id = syncNewService.getLatestInoutId();

    // then
    assertEquals(0L, id);
  }

  @Test
  @DisplayName("LastRead가 DB에 있을 경우 처리검증 테스트")
  void getLatestInoutId_shouldReturnDefaultIfExists() {
    // given
    MongoLastReadInoutLog mockEntity = MongoLastReadInoutLog.builder()
        .id(1L).lastReadEntityId(10L)
        .build();
    when(lastReadInoutLogRepository.findById(1L))
        .thenReturn(Optional.of(mockEntity)
        );

    // when
    Long id = syncNewService.getLatestInoutId();

    // then
    assertEquals(mockEntity.getLastReadEntityId(), id);
  }

  @Test
  @DisplayName("getLogsNew 메서드 검증 입출차 데이터가 섞여있는 경우 검증")
  void getLogsNew_shouldHandleMissingExitAndPaymentGracefully() {
    // given
    EntryMainRecordsEntity entry1 = EntryMainRecordsEntity.builder()
        .entryId(10L)
        .vehicleNumber("2222")
        .entryTime(LocalDateTime.now())
        .entryLotId("1")
        .build();

    EntryMainRecordsEntity entry2 = EntryMainRecordsEntity.builder()
        .entryId(11L)
        .vehicleNumber("2222")
        .entryTime(LocalDateTime.now())
        .entryLotId("1")
        .build();

    ExitMainRecordEntity exitEntity = ExitMainRecordEntity.builder()
        .exitId(11L)
        .vehicleNumber("2222")
        .exitTime(LocalDateTime.now())
        .exitLotId("1")
        .entryMainRecord(entry2)
        .build();

    PaymentRecord paymentEntity = PaymentRecord.builder()
        .paymentId(1L)
        .paymentTime(LocalDateTime.now())
        .initialFee(1234)
        .discountFee(0)
        .paymentFee(1234)
        .entryMainRecord(entry2)
        .build();

    when(entryRepository.findLimitedByEntryIdGreaterThan(anyLong(), anyInt(), anyInt()))
        .thenReturn(Arrays.asList(entry1, entry2));
    when(exitRepository.findAllByEntryMainRecord_EntryIdIn(anyList()))
        .thenReturn(Collections.singletonList(exitEntity));
    when(payemntRepository.findAllByEntryMainRecord_EntryIdIn(anyList()))
        .thenReturn(Collections.singletonList(paymentEntity));

    // when
    List<SyncInoutDataDTO> logs = syncNewService.getLogsNew(0L);

    // then
    assertEquals(2, logs.size());
    assertNull(logs.get(0).getExitTime());
    assertNull(logs.get(0).getFee());
    assertNotNull(logs.get(1).getExitTime());
    assertNotNull(logs.get(1).getFee());
  }

  @Test
  @DisplayName("saveUpdatableInoutDataToMongo 검증 입차 출차 데이터 섞여있을 때"
      + " 잘 입차 데이터만 잘 저장되는지 검증")
  void saveUpdatableInoutDataToMongoTest() {
    // given
    SyncInoutDataDTO entryOnlyDTO = new SyncInoutDataDTO();
    entryOnlyDTO.setEntryId(1L);

    SyncInoutDataDTO exitedDTO1 = new SyncInoutDataDTO();
    exitedDTO1.setEntryId(2L);
    exitedDTO1.setExitTime(LocalDateTime.now()); // 출차

    SyncInoutDataDTO entryOnly2 = new SyncInoutDataDTO();
    entryOnly2.setEntryId(3L);

    List<SyncInoutDataDTO> list = Arrays.asList(entryOnlyDTO, exitedDTO1, entryOnly2);

    // mock
    BulkOperations mockBulkOps = mock(BulkOperations.class);
    when(mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED,
        MongoLogTableEntity.class)).thenReturn(mockBulkOps);
    when(mockBulkOps.upsert(any(Query.class), any(Update.class))).thenReturn(mockBulkOps);

    // when
    syncNewService.saveUpdatableInoutDataToMongo(list); // 실제 메서드 호출

    // then  2개가 입차되어야하므로 2번의 upsert연산이 일어나야함
    verify(mongoOperations, times(1)).bulkOps(BulkOperations.BulkMode.UNORDERED,
        MongoLogTableEntity.class);
    verify(mockBulkOps, times(2)).upsert(any(Query.class), any(Update.class));
    verify(mockBulkOps, times(1)).execute();
  }

  @Test
  @DisplayName("saveUpdatableInoutDataToMongo 검증 데이터가 없을 때")
  void saveUpdatableInoutDataToMongoTestWhenEmptyData() {
    // given
    List<SyncInoutDataDTO> emptyList = new ArrayList<>();
    // when
    syncNewService.saveUpdatableInoutDataToMongo(emptyList);
    // then
    verifyNoInteractions(mongoOperations);
  }
}
