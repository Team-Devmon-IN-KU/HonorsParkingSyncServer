package org.example.honorsparkingsyncserver.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class FileService {


  private static final String FILE_PATH = System.getProperty("user.home") + "/data/lastReadIndex.txt";

  public FileService() {
    createFileIfNotExists();
  }

  private void createFileIfNotExists() {
    File file = new File(FILE_PATH);
    if (!file.exists()) {
      try {
        file.getParentFile().mkdirs(); // 부모 디렉토리 생성
        file.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
          writer.write("0"); // 기본값 저장
        }
        System.out.println("✅ 파일 생성 완료: " + FILE_PATH);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public long readLastReadIndex() {
    System.out.println("Current Working Directory: " + FILE_PATH);
    createFileIfNotExists(); // 파일이 없으면 생성

    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
      return Long.parseLong(reader.readLine());
    } catch (IOException e) {
      e.printStackTrace();
      return 0; // 기본값
    }
  }

  public void updateLastReadIndex(long lastReadIndex) {
    // 파일에 lastReadIndex 값 업데이트
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
      writer.write(String.valueOf(lastReadIndex));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
