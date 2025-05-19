package org.example.honorsparkingsyncserver.sync.inout.service;

import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberListResponse;
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
    public SyncNonMemberListResponse getParkingInfo(SyncNonMemberRequest request) {
        List<SyncNonMemberResponse> entries = new ArrayList<>();

        // 차량 번호 외 전부 더미데이터
        // 더미데이터 1
        entries.add(SyncNonMemberResponse.builder()
                .vehicleNumber(request.getVehicleNumber())
                .parkingLotLocation("서울 강남구 테스트주차장")
                .entryTime(LocalDateTime.now().minusMinutes(42))
                .totalParkingMinutes(42)
                .currentFee(1500)
                .entryPhotoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/2023_Hyundai_Avante_N_1.jpg/330px-2023_Hyundai_Avante_N_1.jpg")
                .build());

        // 더미데이터 2
        entries.add(SyncNonMemberResponse.builder()
                .vehicleNumber(request.getVehicleNumber())
                .parkingLotLocation("세종시 조치원읍")
                .entryTime(LocalDateTime.now().minusMinutes(95))
                .totalParkingMinutes(95)
                .currentFee(4200)
                .entryPhotoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/2023_Hyundai_Avante_N_1.jpg/330px-2023_Hyundai_Avante_N_1.jpg")
                .build());

        return SyncNonMemberListResponse.builder()
                .parkingEntries(entries)
                .build();
    }

}