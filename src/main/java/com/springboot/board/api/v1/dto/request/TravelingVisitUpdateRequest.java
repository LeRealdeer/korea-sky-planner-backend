package com.springboot.board.api.v1.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TravelingVisitUpdateRequest {

    private Integer visitNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isWarbandVisit;
}