package com.example.multi_tanent.production.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProProcessResponse {

    private Long id;
    private String name;

    private Long locationId;
    private String locationName;

    private List<ProProcessWorkGroupResponse> workGroups;

    @Data
    @Builder
    public static class ProProcessWorkGroupResponse {
        private Long workGroupId;
        private String workGroupName;
        private String workGroupNumber;
        private Integer sequenceIndex;
    }
}