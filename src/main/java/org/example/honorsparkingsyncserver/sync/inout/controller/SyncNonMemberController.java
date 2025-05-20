package org.example.honorsparkingsyncserver.sync.inout.controller;

import lombok.RequiredArgsConstructor;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberListResponse;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberRequest;
import org.example.honorsparkingsyncserver.sync.inout.service.SyncNonMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncNonMemberController {

    private final SyncNonMemberService syncNonMemberService;

    @PostMapping("/nonmember")
    public ResponseEntity<SyncNonMemberListResponse> getNonMemberParkingInfo(
            @RequestBody SyncNonMemberRequest request) {
        return ResponseEntity.ok(syncNonMemberService.getParkingInfo(request));
    }
}
