package org.example.honorsparkingsyncserver.sync.unit.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.example.honorsparkingsyncserver.sync.inout.service.SyncNewService;
import org.example.honorsparkingsyncserver.sync.inout.service.SyncService;
import org.example.honorsparkingsyncserver.sync.inout.service.SyncUpdateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SyncServiceTest {

  @Mock
  private SyncUpdateService syncHelperService;

  @Mock
  private SyncNewService syncNewService;

  @InjectMocks
  private SyncService syncService;

  @Test
  void syncData_정상호출되면_두서비스메서드모두실행() {
    // when
    syncService.syncData();

    // then
    verify(syncHelperService, times(1)).syncUpdatableData();
    verify(syncNewService, times(1)).syncNewData();
  }

  @Test
  void syncData_중간에예외가발생하면_예외를로깅만하고끝남() {
    // given
    doThrow(new RuntimeException("업데이트 중 오류")).when(syncHelperService).syncUpdatableData();

    // when
    syncService.syncData();

    // then
    // verifyNewService는 호출 안 됐을 수 있음 (catch문까지 갔는지 확인)
    verify(syncHelperService, times(1)).syncUpdatableData();
    // 실제로 catch 되므로 예외 터지지 않음
  }
}
