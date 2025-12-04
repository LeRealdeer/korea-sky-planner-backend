package com.springboot.board.api.v1.controller;

import com.springboot.board.api.v1.dto.request.SoulCreateRequest;
import com.springboot.board.api.v1.dto.request.SoulUpdateRequest;
import com.springboot.board.api.v1.dto.response.SoulResponse;
import com.springboot.board.application.service.SoulService;
import com.springboot.board.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Soul", description = "영혼 관련 API")
@RestController
@RequestMapping("/api/v1/souls")
@RequiredArgsConstructor
public class SoulController {

    private final SoulService soulService;


    @Operation(summary = "모든 영혼 조회")
    @GetMapping("/all")
    public ApiResponse<List<SoulResponse>> getAllSouls() {
        return ApiResponse.success(soulService.getAllSouls());
    }

    @Operation(summary = "시즌별 영혼 조회")
    @GetMapping("/season/{seasonId}")
    public ApiResponse<List<SoulResponse>> getSoulsBySeason(@PathVariable Integer seasonId) {
        return ApiResponse.success(soulService.getSoulsBySeason(seasonId));
    }

    @Operation(summary = "영혼 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<SoulResponse> getSoul(@PathVariable Integer id) {
        return ApiResponse.success(soulService.getSoul(id));
    }

    @Operation(summary = "영혼 검색")
    @GetMapping("/search")
    public ApiResponse<List<SoulResponse>> searchSouls(@RequestParam String query) {
        return ApiResponse.success(soulService.searchSouls(query));
    }


// ✅ 새로 추가 (유랑 대백과용)
@Operation(summary = "유랑 대백과 - 모든 유랑 이력 조회")
@GetMapping("/traveling-visits")
public ApiResponse<Page<Map<String, Object>>> getTravelingVisits(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size) {
    return ApiResponse.success(soulService.getAllTravelingVisits(page, size));
}

// ✅ 기존 유지 (오래된 영혼용)
@Operation(summary = "오래된 유랑 조회")
@GetMapping("/oldest-spirits")
public ApiResponse<Page<Map<String, Object>>> getOldestSpirits(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.success(soulService.getOldestSpirits(page, size));
}

    @Operation(summary = "영혼 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SoulResponse> createSoul(@Valid @RequestBody SoulCreateRequest request) {
        return ApiResponse.success(soulService.createSoul(request));
    }

    @Operation(summary = "영혼 수정")
    @PutMapping("/{id}")
    public ApiResponse<SoulResponse> updateSoul(
            @PathVariable Integer id,
            @Valid @RequestBody SoulUpdateRequest request) {
        return ApiResponse.success(soulService.updateSoul(id, request));
    }

    @Operation(summary = "영혼 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSoul(@PathVariable Integer id) {
        soulService.deleteSoul(id);
    }

    @Operation(summary = "이웃 영혼 조회")
    @GetMapping("/{id}/neighbors")
    public ApiResponse<Map<String, List<SoulResponse>>> getNeighbors(@PathVariable Integer id) {
        return ApiResponse.success(soulService.getNeighbors(id));
    }

// SoulController.java의 getSouls 메소드 수정

@Operation(summary = "영혼 목록 조회 (페이징)")
@GetMapping
public ApiResponse<Page<SoulResponse>> getSouls(@RequestParam(defaultValue = "0") int page) {
    return ApiResponse.success(soulService.getSouls(page));
}




}