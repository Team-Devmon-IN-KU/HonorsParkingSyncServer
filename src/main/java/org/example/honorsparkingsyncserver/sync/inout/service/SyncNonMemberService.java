package org.example.honorsparkingsyncserver.sync.inout.service;

import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberListResponse;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberRequest;

public interface SyncNonMemberService {
    SyncNonMemberListResponse getParkingInfo(SyncNonMemberRequest request);
}