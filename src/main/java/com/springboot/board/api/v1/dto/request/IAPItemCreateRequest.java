package com.springboot.board.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class IAPItemCreateRequest {

    @NotNull
    private Integer seasonId;

    @NotBlank
    private String name;

    private String category;

    @NotBlank
    private String purchaseType; // "PAID", "CANDLE", "BOTH"

    private String priceInfo;

    private List<String> keywords;

    private String imageUrl;
}