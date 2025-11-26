package com.springboot.board.api.v1.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TravelingVisitResponse {
    private Long id;
    private Integer visitNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isWarbandVisit;
}