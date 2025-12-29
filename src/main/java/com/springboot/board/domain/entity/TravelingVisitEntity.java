package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "traveling_visit")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TravelingVisitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soul_id", nullable = false)
    private SoulEntity soul;

    @Column(nullable = false)
    private Integer visitNumber; // 0=시즌당시, 1=1차, 2=2차... (해당 영혼의 방문 차수)

    @Column(name = "global_order")
    private Integer globalOrder; // 전체 유랑 순서 (1번째, 2번째, 3번째... 전체 유랑 중 몇 번째인지)

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean isWarbandVisit = false; // 유랑단 여부
}