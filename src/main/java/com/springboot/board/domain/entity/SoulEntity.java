package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "soul")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SoulEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ========== 시즌 연결 ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    // ========== 기본 정보 (기존 유지) ==========
    @Column(length = 255, nullable = false)
    private String seasonName; // 호환성을 위해 유지

    @Column(length = 255, nullable = false)
    private String name;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum; // 전체 유랑 순서

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "int default 0")
    private Integer rerunCount; // 호환성 유지 (자동 계산으로 변경 예정)

    // ========== 검색용 키워드 ==========
    @ElementCollection
    @CollectionTable(name = "soul_keywords", joinColumns = @JoinColumn(name = "soul_id"))
    @Column(name = "keyword", length = 50)
    @Size(max = 15)
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    // ========== 메타 정보 ==========
    @Column(length = 255)
    private String creator;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ========== 시즌 가이드 여부 ==========
    @Column(nullable = false)
    @Builder.Default
    private boolean isSeasonGuide = false;

    // ========== 관계 ==========
    @OneToMany(mappedBy = "soul", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "soul", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitNumber ASC")
    @Builder.Default
    private List<TravelingVisitEntity> travelingVisits = new ArrayList<>();

    // ========== 헬퍼 메소드 ==========
    public boolean hasVisitedAsTS() {
        return travelingVisits != null && !travelingVisits.isEmpty();
    }

    public int getTotalVisitCount() {
        return hasVisitedAsTS() ? travelingVisits.size() : 0;
    }

    public LocalDate getLastTravelingVisitDate() {
        if (!hasVisitedAsTS()) return null;
        return travelingVisits.stream()
                .map(TravelingVisitEntity::getEndDate)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    // rerunCount 자동 동기화 (선택적)
    @PostLoad
    @PostPersist
    @PostUpdate
    private void syncRerunCount() {
        this.rerunCount = getTotalVisitCount();
    }
}