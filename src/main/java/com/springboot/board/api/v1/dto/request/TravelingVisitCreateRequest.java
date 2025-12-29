package com.springboot.board.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TravelingVisitCreateRequest {

    @NotNull
    private Integer soulId;

    @NotNull
    private Integer visitNumber;

    private Integer globalOrder; // 전체 유랑 순서 (선택적)

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @JsonProperty("isWarbandVisit")
    private Boolean isWarbandVisit = false;
}