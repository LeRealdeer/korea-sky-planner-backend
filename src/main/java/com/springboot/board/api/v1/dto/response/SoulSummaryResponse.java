package com.springboot.board.api.v1.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SoulSummaryResponse {
    private Integer id;
    private String name;
    private String representativeImageUrl;
    private Integer totalVisits;
    private boolean isSeasonGuide;
}