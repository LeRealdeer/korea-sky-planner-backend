package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "season")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // "감사", "빛추", "친밀"

    @Column(nullable = false, unique = true)
    private Integer orderNum; // 1, 2, 3...

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 50)
    private String color; // "#FFD700"

    @Column(nullable = false)
    @Builder.Default
    private boolean isCollaboration = false;

    // 계산 메소드
    public int getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}