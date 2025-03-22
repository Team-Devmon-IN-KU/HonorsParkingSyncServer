package org.example.honorsparkingsyncserver.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PostParkingDataService {

  public boolean callApi(List<Map<String, Object>> dataList) {
    // API 호출 로직
    try {
      // API 호출
      // 성공하면 true 반환
      return true;
    } catch (Exception e) {
      // 실패하면 false 반환
      e.printStackTrace();
      return false;
    }
  }
}
