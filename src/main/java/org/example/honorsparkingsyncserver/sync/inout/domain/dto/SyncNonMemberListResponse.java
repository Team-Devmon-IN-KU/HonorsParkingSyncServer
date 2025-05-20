package org.example.honorsparkingsyncserver.sync.inout.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncNonMemberListResponse {
    private List<SyncNonMemberResponse> parkingEntries;
}
