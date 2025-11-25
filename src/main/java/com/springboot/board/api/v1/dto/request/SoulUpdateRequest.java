package com.springboot.board.api.v1.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 영혼 수정 요청 DTO
 */
@Getter
@Setter
public class SoulUpdateRequest {

    /** 시즌명 */
    private String seasonName;

    /** 영혼 이름 */
    private String name;

    /** 시즌 내 순서 */
    private Integer orderNum;

    /** 시즌 시작일 */
    private LocalDate startDate;

    /** 시즌 종료일 */
    private LocalDate endDate;

    /** 유랑 복각 횟수 */
    private Integer rerunCount;

    /** 검색용 키워드 */
    private List<String> keywords;

    /** 제작자 */
    private String creator;

    /** 영혼 설명 */
    private String description;

    /** ✅ 시즌 가이드 여부 (추가!) */
    private Boolean isSeasonGuide;
}