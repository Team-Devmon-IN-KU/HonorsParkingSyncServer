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

        // 더미데이터
        return SyncNonMemberResponse.builder()
                .vehicleNumber(request.getVehicleNumber())
                .parkingLotLocation("세종시 조치원읍")
                .entryTime(LocalDateTime.now().minusMinutes(60))
                .totalParkingMinutes(60)
                .currentFee(3000)
                .entryPhotoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/2023_Hyundai_Avante_N_1.jpg/330px-2023_Hyundai_Avante_N_1.jpg")
                .build();
    }

}