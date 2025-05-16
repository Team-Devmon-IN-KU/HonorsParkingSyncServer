package org.example.honorsparkingsyncserver.sync.inout.domain.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncInoutDataDTO {

  private String vehicleNumber;

  private LocalDateTime entryTime;
  private Long entryId;

  private LocalDateTime exitTime;
  private Integer fee;

  private LocalDateTime paidAt;
  private Long parkinglotId;
}
