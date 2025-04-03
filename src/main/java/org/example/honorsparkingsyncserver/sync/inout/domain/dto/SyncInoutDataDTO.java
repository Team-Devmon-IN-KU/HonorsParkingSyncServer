package org.example.honorsparkingsyncserver.sync.inout.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SyncInoutDataDTO {

  private String vehicleNumber;

  private LocalDateTime entryTime;
  private Long entryId;

  private LocalDateTime exitTime;
  private Integer fee;

  private LocalDateTime paidAt;
  private Long parkinglotId;
}
