package org.example.honorsparkingsyncserver.sync.inout.service;

import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberRequest;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SyncNonMemberServiceImpl implements SyncNonMemberService {

    @Override
    public SyncNonMemberResponse getParkingInfo(SyncNonMemberRequest request) {
        return SyncNonMemberResponse.builder()
                .vehicleNumber(request.getVehicleNumber())
                .parkingLotLocation("세종시 조치원읍")
                .entryTime(LocalDateTime.now().minusMinutes(60))
                .totalParkingMinutes(60)
                .currentFee(3000)
                .entryPhotoUrl("https://example.com/images/test.jpg")
                .build();
    }

}