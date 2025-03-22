package org.example.honorsparkingsyncserver.scheduler;

import java.util.List;
import java.util.Map;
import org.example.honorsparkingsyncserver.service.ExternalDBService;
import org.example.honorsparkingsyncserver.service.FileService;
import org.example.honorsparkingsyncserver.service.PostParkingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExternalDBScheduler {

  @Autowired
  private ExternalDBService externalDBService;
  @Autowired
  private PostParkingDataService apiService;
  @Autowired
  private FileService fileService;

  public ExternalDBScheduler(ExternalDBService externalDBService) {
    this.externalDBService = externalDBService;
  }

  private static final int BATCH_SIZE = 1000;

  @Scheduled(fixedRate = 6000) // 1분마다 실행 (10000ms = 10초)
  public void syncExternalData() {
    try {
      long lastReadIndex = fileService.readLastReadIndex(); // 파일에서 lastReadIndex 값 읽기
      List<Map<String, Object>> dataList = externalDBService.fetchData(lastReadIndex, BATCH_SIZE); // DB에서 데이터 가져오기

      if (!dataList.isEmpty()) {
        boolean apiSuccess = apiService.callApi(dataList); // API 호출

        if (apiSuccess) {
          long newLastReadIndex = (long) dataList.get(dataList.size() - 1).get("id");
          fileService.updateLastReadIndex(newLastReadIndex); // 파일에서 lastReadIndex 업데이트

          // 첫 번째 row 출력
          Map<String, Object> firstRow = dataList.get(0);
          System.out.println("First Row: " + firstRow);

          // 마지막 row 출력
          Map<String, Object> lastRow = dataList.get(dataList.size() - 1);
          System.out.println("Last Row: " + lastRow);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
