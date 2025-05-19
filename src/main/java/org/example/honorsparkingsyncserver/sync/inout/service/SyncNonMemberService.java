package org.example.honorsparkingsyncserver.sync.inout.service;

import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberRequest;
import org.example.honorsparkingsyncserver.sync.inout.domain.dto.SyncNonMemberResponse;

public interface SyncNonMemberService {
    SyncNonMemberResponse getParkingInfo(SyncNonMemberRequest request);
}