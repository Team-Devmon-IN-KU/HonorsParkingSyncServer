package org.example.honorsparkingsyncserver.sync.inout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncInoutDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SyncFetchService {

  private static final Logger logger = LoggerFactory.getLogger(SyncUpdateService.class);

  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  @Value("${sync.url}")
  private String SYNC_URL;
  @Value("${sync.api-key}")
  private String API_KEY;
  @Value("${sync.header-name}")
  private String HEADER_NAME;
  //  private static final String CSRF_TOKEN = "your-csrf-token";

  /**
   * ✅ 개별 배치 API 요청 (트랜잭션 필요 없음)
   *
   * @param batch
   */
  public void sendBatchToServer(List<SyncInoutDataDTO> batch) {
    Map<String, Object> payloadMap = new HashMap<>();
    payloadMap.put("inoutList", batch);

    String jsonPayload = "";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HEADER_NAME, API_KEY);

    try {
      jsonPayload = objectMapper.writeValueAsString(payloadMap);

      HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
      ResponseEntity<String> response = restTemplate.exchange(SYNC_URL, HttpMethod.POST, request,
          String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new RuntimeException("❌ 앱서버로 통신 후 동기화 실패: " + response.getStatusCode());
      }

    } catch (JsonProcessingException e) {
      logger.error("❌ JSON 직렬화 실패", e);
      throw new RuntimeException("JSON 직렬화 중 예외 발생", e);

    } catch (HttpClientErrorException e) {
      logger.info("📤 에러발생 요청 URL: {}", SYNC_URL);
      logger.info("📤 에러발생 요청 헤더: {}", headers);
      logger.info("📤 에러발생 요청 바디: {}", jsonPayload);
      logger.error("❌ HTTP 요청 실패 - 상태 코드: {}, 응답 바디: {}", e.getStatusCode(),
          e.getResponseBodyAsString());
      throw new RuntimeException("HTTP 요청 실패: " + e.getStatusCode(), e);

    } catch (Exception e) {
      logger.error("❌ 기타 예외 발생", e);
      throw new RuntimeException("앱 서버 전송 중 알 수 없는 예외 발생", e);
    }
  }
}
