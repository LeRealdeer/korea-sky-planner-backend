package com.springboot.board.api.v1.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SeasonResponse {
    private Integer id;
    private String name;
    private Integer orderNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationDays; // 계산값
    private String color;
    private boolean isCollaboration;
    private Integer totalSpirits; // 영혼 개수
    private Integer totalIAPItems; // IAP 아이템 개수
}