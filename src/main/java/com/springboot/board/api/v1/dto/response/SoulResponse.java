package com.springboot.board.api.v1.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class SoulResponse {
    private Integer id;
    
    // 시즌 정보
    private Integer seasonId;
    private String seasonName;
    private String seasonColor;
    
    // 기본 정보
    private String name;
    private Integer orderNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rerunCount;
    
    // 메타 정보
    private List<String> keywords;
    private String creator;
    private String description;
    private boolean isSeasonGuide;
    
    // 이미지
    private List<ImageResponse> images;
    
    // 유랑 이력
    private List<TravelingVisitResponse> travelingVisits;
    private Integer totalVisits;
    private boolean hasVisitedAsTS;
    private LocalDate lastVisitDate;
}