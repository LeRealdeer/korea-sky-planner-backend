package com.springboot.board.api.v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelingVisitWithSoulResponse {
    // TravelingVisit 정보
    private Long visitId;
    private Integer visitNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isWarbandVisit;
    private Integer globalOrder;
    // Soul 정보
    private Long soulId;
    private String soulName;
    private String seasonName;
    private Integer orderNum;
    private Integer rerunCount;
    
    // 이미지 (대표 이미지만)
    private String representativeImageUrl;
}