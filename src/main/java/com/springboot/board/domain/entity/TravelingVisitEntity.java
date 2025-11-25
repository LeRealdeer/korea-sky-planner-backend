package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private Integer visitNumber; // 0=시즌당시, 1=1차, 2=2차...

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean isWarbandVisit = false; // 유랑단 여부

    @Column(length = 1000)
    private String notes; // 관리자 메모

    @OneToMany(mappedBy = "travelingVisit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageEntity> visitImages = new ArrayList<>();
}