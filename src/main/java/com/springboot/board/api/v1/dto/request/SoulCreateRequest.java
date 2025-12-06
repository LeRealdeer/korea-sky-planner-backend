package com.springboot.board.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 영혼 생성 요청 DTO
 */
@Getter
@Setter
public class SoulCreateRequest {

    /** 시즌 ID */
    @NotNull(message = "시즌 ID는 필수입니다.")
    private Integer seasonId;

    /** 시즌명 (중복 저장용) */
    @NotBlank(message = "시즌명은 필수입니다.")
    private String seasonName;

    /** 영혼 이름 */
    @NotBlank(message = "영혼 이름은 필수입니다.")
    private String name;

    /** 시즌 내 순서 */
    @NotNull(message = "순서는 필수입니다.")
    private Integer orderNum;

    /** 시즌 시작일 */
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    /** 시즌 종료일 */
    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    /** 유랑 복각 횟수 (생성 시 기본값 0) */
    private Integer rerunCount = 0;

    /** 검색용 키워드 */
    private List<String> keywords;

    /** 제작자 */
    private String creator;

    /** 영혼 설명 */
    private String description;

    /** 시즌 가이드 여부 */
    private Boolean isSeasonGuide = false;

    /** ✅ 이미지 목록 (추가!) */
    private List<ImageInfo> images;

    /**
     * 이미지 정보 내부 클래스
     */
    @Data
    public static class ImageInfo {
        private String imageType;
        private String url;
    }
}