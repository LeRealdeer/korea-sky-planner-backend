package com.springboot.board.api.v1.controller;

import com.springboot.board.api.v1.dto.request.IAPItemCreateRequest;
import com.springboot.board.api.v1.dto.request.IAPItemUpdateRequest;
import com.springboot.board.api.v1.dto.response.IAPItemResponse;
import com.springboot.board.application.service.IAPItemService;
import com.springboot.board.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "IAPItem", description = "IAP 아이템 관련 API")
@RestController
@RequestMapping("/api/v1/iap-items")
@RequiredArgsConstructor
public class IAPItemController {

    private final IAPItemService iapItemService;

    @Operation(summary = "모든 IAP 아이템 조회")
    @GetMapping
    public ApiResponse<List<IAPItemResponse>> getAllItems() {
        return ApiResponse.success(iapItemService.getAllItems());
    }

    @Operation(summary = "시즌별 IAP 아이템 조회")
    @GetMapping("/season/{seasonId}")
    public ApiResponse<List<IAPItemResponse>> getItemsBySeason(@PathVariable Integer seasonId) {
        return ApiResponse.success(iapItemService.getItemsBySeason(seasonId));
    }

    @Operation(summary = "구매 방식별 조회")
    @GetMapping("/purchase-type/{purchaseType}")
    public ApiResponse<List<IAPItemResponse>> getItemsByPurchaseType(@PathVariable String purchaseType) {
        return ApiResponse.success(iapItemService.getItemsByPurchaseType(purchaseType));
    }

    @Operation(summary = "IAP 아이템 검색")
    @GetMapping("/search")
    public ApiResponse<List<IAPItemResponse>> searchItems(@RequestParam String query) {
        return ApiResponse.success(iapItemService.searchItems(query));
    }

    @Operation(summary = "IAP 아이템 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IAPItemResponse> createItem(@Valid @RequestBody IAPItemCreateRequest request) {
        return ApiResponse.success(iapItemService.createItem(request));
    }

    @Operation(summary = "IAP 아이템 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        iapItemService.deleteItem(id);
    }

    @Operation(summary = "IAP 아이템 수정")
    @PutMapping("/{id}")
    public ApiResponse<IAPItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody IAPItemUpdateRequest request) {
        return ApiResponse.success(iapItemService.updateItem(id, request));
    }
}