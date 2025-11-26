package com.springboot.board.api.v1.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class IAPItemUpdateRequest {

    private Integer seasonId;
    private String name;
    private String category;
    private String purchaseType;
    private String priceInfo;
    private List<String> keywords;
    private String imageUrl;
}