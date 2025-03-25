package org.example.honorsparkingsyncserver.sync.inout.util;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class HashUtil {

  // 특정 필드를 제외할 수 있도록 리스트 정의
  private static final List<String> EXCLUDED_FIELDS = Arrays.asList("entryId");

  // 엔티티의 모든 필드를 해시값으로 변환 (제네릭 적용)
  public static <T> String generateHash(T entity) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      StringJoiner joiner = new StringJoiner("|");

      // 리플렉션을 사용하여 모든 필드 값을 가져옴
      for (Field field : entity.getClass().getDeclaredFields()) {
        field.setAccessible(true); // private 필드 접근 가능하게 설정

        if (EXCLUDED_FIELDS.contains(field.getName())) {
          continue; // 제외할 필드 무시
        }

        Object value = field.get(entity);
        joiner.add(value != null ? value.toString() : "null");
      }

      // SHA-256 해시 생성
      byte[] hashBytes = digest.digest(joiner.toString().getBytes(StandardCharsets.UTF_8));

      // 바이트 배열을 HEX 문자열로 변환
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        hexString.append(String.format("%02x", b));
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException | IllegalAccessException e) {
      throw new RuntimeException("해시 생성 실패", e);
    }
  }
}
