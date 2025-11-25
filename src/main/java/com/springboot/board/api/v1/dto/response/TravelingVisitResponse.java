package com.springboot.board.api.v1.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TravelingVisitResponse {
    private Long id;
    private Integer soulId;
    private String soulName;
    private Integer visitNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isWarbandVisit;
    private String notes;
    private Long daysSinceEnd;
    private List<ImageResponse> visitImages;
}