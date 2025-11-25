package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "soul_entity")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SoulEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    private String seasonName;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(name = "order_num", nullable = false)
    private int orderNum;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "int default 0")
    private int rerunCount;

    @ElementCollection
    @CollectionTable(name = "soul_keywords", joinColumns = @JoinColumn(name = "soul_id"))
    @Column(name = "keyword")
    @Size(max = 15)
    private List<String> keywords;

    @Column(length = 255)
    private String creator;

    @Column(columnDefinition = "TEXT")
    private String description;

@OneToMany(mappedBy = "soul", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ImageEntity> images = new ArrayList<>();

}
