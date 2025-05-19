package org.example.honorsparkingsyncserver.sync.inout.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncNonMemberResponse {

    private String vehicleNumber;
    private String parkingLotLocation;
    private LocalDateTime entryTime;
    private Integer totalParkingMinutes;
    private Integer currentFee;
    private String entryPhotoUrl;

}
