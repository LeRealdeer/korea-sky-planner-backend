package com.springboot.board.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SeasonCreateRequest {
    
    @NotBlank
    private String name;

    @NotNull
    private Integer orderNum;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String color;
    private boolean isCollaboration;
}