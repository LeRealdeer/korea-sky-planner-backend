package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "iap_item")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class IAPItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    @Column(nullable = false, length = 100)
    private String name; // "빨간 뿔", "꼬리"

    @Column(length = 50)
    private String category; // "뿔", "꼬리", "가면", "케이프"

    /**
     * 구매 방식
     * PAID - 유료 (현금)
     * CANDLE - 양초
     * BOTH - 둘 다 가능
     */
    @Column(length = 20, nullable = false)
    @Builder.Default
    private String purchaseType = "PAID";

    @Column(length = 50)
    private String priceInfo; // "$9.99" 또는 "75 양초"

    @ElementCollection
    @CollectionTable(name = "iap_item_keywords", joinColumns = @JoinColumn(name = "iap_item_id"))
    @Column(name = "keyword", length = 50)
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @Column(length = 512)
    private String imageUrl; // 아이템 이미지 URL
}