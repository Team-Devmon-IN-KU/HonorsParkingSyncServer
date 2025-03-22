package org.example.honorsparkingsyncserver.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExternalDBService {
  ㅅㄷㄴㅅ
  @Value("${spring.datasource.url}")
  private String DB_URL = "jdbc:sqlserver://1.237.1.129:50081;database=PARKINGK";
  @Value("${spring.datasource.username}")
  private String USER = "sa";
  @Value("${spring.datasource.password}")
  private String PASSWORD;

  public List<Map<String, Object>> fetchData(long lastReadIndex, int limit) {
    List<Map<String, Object>> dataList = new ArrayList<>();
    String query = "SELECT * FROM dbo.entry_main_records WHERE entry_id > ? ORDER BY entry_id OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";

    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setLong(1, lastReadIndex);
      pstmt.setInt(2, limit);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> row = new HashMap<>();
          row.put("id", rs.getLong("entry_id"));
          row.put("vehicle_number", rs.getString("vehicle_number"));
          dataList.add(row);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return dataList;
  }
}
