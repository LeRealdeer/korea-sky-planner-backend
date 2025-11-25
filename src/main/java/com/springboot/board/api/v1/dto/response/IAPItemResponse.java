package com.springboot.board.api.v1.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class IAPItemResponse {
    private Long id;
    private String name;
    private String category;
    private String purchaseType; // "PAID", "CANDLE", "BOTH"
    private String priceInfo;
    private Integer seasonId;
    private String seasonName;
    private List<String> keywords;
    private String imageUrl;
}