package org.example.honorsparkingsyncserver.sync.inout.service;

import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberListResponse;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberRequest;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberResponse;
import org.example.honorsparkingsyncserver.sync.inout.domain.entity.EntryMainRecordsWithLotEntity;

import org.example.honorsparkingsyncserver.sync.inout.domain.entity.FeeStructureCoreEntity;
import org.example.honorsparkingsyncserver.sync.inout.repository.EntryWithLotRepository;
import org.example.honorsparkingsyncserver.sync.inout.repository.FeeStructureCoreRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncNonMemberServiceImpl implements SyncNonMemberService {

    private final EntryWithLotRepository entryWithLotRepository;
    private final FeeStructureCoreRepository feeRepository;

    @Override
    public SyncNonMemberListResponse getParkingInfo(SyncNonMemberRequest request) {

        List<EntryMainRecordsWithLotEntity> entryEntities =
                entryWithLotRepository.findActiveWithJoin(request.getVehicleNumber());

        FeeStructureCoreEntity feePolicy = feeRepository.findByFeeCode("001");

        int baseTime = (feePolicy != null && feePolicy.getBaseTime() != null) ? feePolicy.getBaseTime() : 60;
        int baseFee = (feePolicy != null && feePolicy.getBaseFee() != null) ? feePolicy.getBaseFee() : 0;
        int unitTime = (feePolicy != null && feePolicy.getUnitTime() != null) ? feePolicy.getUnitTime() : 30;
        int unitFee = (feePolicy != null && feePolicy.getUnitFee() != null) ? feePolicy.getUnitFee() : 1000;
        int dailyMax = (feePolicy != null && feePolicy.getDailyMaxFee() != null) ? feePolicy.getDailyMaxFee() : 50000;

        List<SyncNonMemberResponse> entries = entryEntities.stream()

                .map(entry -> {

                    // ec2 서버하고 실제 시각하고 9시간 차이
                    // ec2가 시스템 시간을 utc로 사용하는것으로 예상
                    LocalDateTime entryTime = entry.getEntryTime();
                    int totalMinutes = (entryTime != null)
                            ? (int) Duration.between(entryTime, LocalDateTime.now()).toMinutes()
                            : 0;

                    // 주차 요금 계산 로직
                    int currentFee;
                    if (totalMinutes <= baseTime) {
                        currentFee = baseFee;
                    } else {
                        int additionalMinutes = totalMinutes - baseTime;
                        int units = (int) Math.ceil(additionalMinutes / (double) unitTime);
                        currentFee = baseFee + units * unitFee;
                        if (currentFee > dailyMax) currentFee = dailyMax;
                    }

                    String photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/2023_Hyundai_Avante_N_1.jpg/330px-2023_Hyundai_Avante_N_1.jpg";
//                    String photo = entry.getEntryPhoto();
//                    String photoUrl = (photo != null && !photo.trim().isEmpty())
//                            ? "https://ftp-server.com/photos/" + photo
//                            : null;

                    String location = (entry.getParkingLot() != null)
                            ? entry.getParkingLot().getLocation()
                            : "위치 정보 없음";

                    return SyncNonMemberResponse.builder()
                            .vehicleNumber(entry.getVehicleNumber())
                            .parkingLotLocation(location)
                            .entryTime(entry.getEntryTime())
                            .totalParkingMinutes(totalMinutes)
                            .currentFee(currentFee)
                            .entryPhotoUrl(photoUrl)
                            .build();
                })
                .collect(Collectors.toList());

        // 테스트 대비 더미데이터 주석처리
//        List<SyncNonMemberResponse> entries = new ArrayList<>();

//        // 더미데이터 1
//        entries.add(SyncNonMemberResponse.builder()
//                .vehicleNumber(request.getVehicleNumber())
//                .parkingLotLocation("서울 강남구 테스트주차장")
//                .entryTime(LocalDateTime.now().minusMinutes(42))
//                .totalParkingMinutes(42)
//                .currentFee(1500)
//                .entryPhotoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/2023_Hyundai_Avante_N_1.jpg/330px-2023_Hyundai_Avante_N_1.jpg")
//                .build());
//
//        // 더미데이터 2
//        entries.add(SyncNonMemberResponse.builder()
//                .vehicleNumber(request.getVehicleNumber())
//                .parkingLotLocation("세종시 조치원읍")
//                .entryTime(LocalDateTime.now().minusMinutes(95))
//                .totalParkingMinutes(95)
//                .currentFee(4200)
//                .entryPhotoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/2023_Hyundai_Avante_N_1.jpg/330px-2023_Hyundai_Avante_N_1.jpg")
//                .build());

        return SyncNonMemberListResponse.builder()
                .parkingEntries(entries)
                .build();
    }

}