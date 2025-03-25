package org.example.honorsparkingsyncserver.sync.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsEntity;
import org.example.honorsparkingsyncserver.sync.inout.util.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class HashUtilTest {

  @Test
  @DisplayName("같은 값은 같은 해시값이 나오는지 검증")
  void givenSameValues_whenGenerateHash_thenSameHash() {
    // Given
    EntryMainRecordsEntity entity1 = createSampleEntity();
    EntryMainRecordsEntity entity2 = createSampleEntity(); // 동일한 값

    // When
    String hash1 = HashUtil.generateHash(entity1);
    String hash2 = HashUtil.generateHash(entity2);

    // Then
    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  @DisplayName("다른 값은 다른 해시값이 나오는지 검증")
  void givenDifferentValues_whenGenerateHash_thenDifferentHash() {
    // Given
    EntryMainRecordsEntity entity1 = createSampleEntity();
    EntryMainRecordsEntity entity2 = createSampleEntity();
    entity2.setVehicleNumber("99가9999"); // 다른 값

    // When
    String hash1 = HashUtil.generateHash(entity1);
    String hash2 = HashUtil.generateHash(entity2);

    // Then
    assertThat(hash1).isNotEqualTo(hash2);
  }

  @Test
  @DisplayName("Exclude Field 값만 다를 때 같은 해시값이 나오는지 검증")
  void givenDifferentValuesInExcludeFields_whenGenerateHash_thenSameHash() {
    // Given
    EntryMainRecordsEntity entity1 = createSampleEntity();
    EntryMainRecordsEntity entity2 = createSampleEntity();
    entity2.setEntryId(999L); // 다른 값

    // When
    String hash1 = HashUtil.generateHash(entity1);
    String hash2 = HashUtil.generateHash(entity2);

    // Then
    assertThat(hash1).isEqualTo(hash2);
  }

  // ✅ 테스트용 샘플 엔티티 생성
  private EntryMainRecordsEntity createSampleEntity() {
    return EntryMainRecordsEntity.builder()
        .entryId(1L)
        .vehicleNumber("12가3456")
        .rfidNumber("RFID-001")
        .entryTime(LocalDateTime.of(2024, 3, 23, 10, 0, 0))
        .entryPhoto("photo_url")
        .entryLotId("1")
        .entryDeviceCode("DEVICE-123")
        .entryType("Regular")
        .entryTypeId(1)
        .entryResultType("Success")
        .entryFeeCode("FEE-001")
        .parkingStatus("IN")
        .build();
  }
}
