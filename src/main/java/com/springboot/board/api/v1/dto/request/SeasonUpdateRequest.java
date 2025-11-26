package com.springboot.board.api.v1.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SeasonUpdateRequest {
    
    private String name;
    private Integer orderNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private String color;
    private Boolean isCollaboration;
}