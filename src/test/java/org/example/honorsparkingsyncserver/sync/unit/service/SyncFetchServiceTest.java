package org.example.honorsparkingsyncserver.sync.unit.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncInoutDataDTO;
import org.example.honorsparkingsyncserver.sync.inout.service.SyncFetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class SyncFetchServiceTest {

  @InjectMocks
  private SyncFetchService syncFetchService;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    // @Value로 주입되는 값들을 강제로 세팅
    Field urlField = SyncFetchService.class.getDeclaredField("SYNC_URL");
    urlField.setAccessible(true);
    urlField.set(syncFetchService, "http://mock-api.com/sync");

    Field keyField = SyncFetchService.class.getDeclaredField("API_KEY");
    keyField.setAccessible(true);
    keyField.set(syncFetchService, "mock-api-key");

    Field headerField = SyncFetchService.class.getDeclaredField("HEADER_NAME");
    headerField.setAccessible(true);
    headerField.set(syncFetchService, "x-api-key");
  }

  @Test
  @DisplayName("✅ 정상적인 배치 요청이 성공하는 경우")
  void testSendBatchToServer_Success() throws Exception {
    // given
    List<SyncInoutDataDTO> batch = Collections.singletonList(new SyncInoutDataDTO());
    String expectedPayload = "{\"inoutList\":[]}";

    when(objectMapper.writeValueAsString(any())).thenReturn(expectedPayload);
    when(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(String.class))
    ).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

    // when
    syncFetchService.sendBatchToServer(batch);

    // then
    verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(),
        eq(String.class));
  }

  @Test
  @DisplayName("❌ HttpClientErrorException 발생 시 예외 처리")
  void testSendBatchToServer_HttpClientErrorException() throws Exception {
    // given
    List<SyncInoutDataDTO> batch = Collections.singletonList(new SyncInoutDataDTO());

    String expectedPayload = "{\"inoutList\":[]}";

    when(objectMapper.writeValueAsString(any())).thenReturn(expectedPayload);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

    // when & then
    assertThrows(HttpClientErrorException.class, () -> syncFetchService.sendBatchToServer(batch));
  }

  @Test
  @DisplayName("❌ 일반 Exception 발생 시 예외 처리")
  void testSendBatchToServer_GeneralException() throws Exception {
    // given
    List<SyncInoutDataDTO> batch = Collections.singletonList(new SyncInoutDataDTO());

    String expectedPayload = "{\"inoutList\":[]}";

    when(objectMapper.writeValueAsString(any())).thenReturn(expectedPayload);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
        .thenThrow(new RuntimeException("Something went wrong"));

    // when & then
    assertThrows(RuntimeException.class, () -> syncFetchService.sendBatchToServer(batch));
  }

}
