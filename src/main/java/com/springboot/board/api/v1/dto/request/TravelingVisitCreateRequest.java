package com.springboot.board.api.v1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TravelingVisitCreateRequest {

    @NotNull
    private Integer soulId;

    @NotNull
    private Integer visitNumber;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private boolean isWarbandVisit = false;

    private String notes;
}