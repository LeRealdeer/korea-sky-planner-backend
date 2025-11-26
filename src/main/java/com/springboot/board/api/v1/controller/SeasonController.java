package com.springboot.board.api.v1.controller;

import com.springboot.board.api.v1.dto.request.SeasonCreateRequest;
import com.springboot.board.api.v1.dto.request.SeasonUpdateRequest;
import com.springboot.board.api.v1.dto.response.SeasonResponse;
import com.springboot.board.application.service.SeasonService;
import com.springboot.board.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Season", description = "시즌 관련 API")
@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    @Operation(summary = "모든 시즌 조회")
    @GetMapping
    public ApiResponse<List<SeasonResponse>> getAllSeasons() {
        return ApiResponse.success(seasonService.getAllSeasons());
    }

    @Operation(summary = "시즌 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<SeasonResponse> getSeasonById(@PathVariable Integer id) {
        return ApiResponse.success(seasonService.getSeasonById(id));
    }

    @Operation(summary = "콜라보 시즌 조회")
    @GetMapping("/collaborations")
    public ApiResponse<List<SeasonResponse>> getCollaborationSeasons() {
        return ApiResponse.success(seasonService.getCollaborationSeasons());
    }

    @Operation(summary = "시즌 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SeasonResponse> createSeason(@Valid @RequestBody SeasonCreateRequest request) {
        return ApiResponse.success(seasonService.createSeason(request));
    }

    @Operation(summary = "시즌 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeason(@PathVariable Integer id) {
        seasonService.deleteSeason(id);
    }

    @Operation(summary = "시즌 수정")
    @PutMapping("/{id}")
    public ApiResponse<SeasonResponse> updateSeason(
            @PathVariable Integer id,
            @Valid @RequestBody SeasonUpdateRequest request) {
        return ApiResponse.success(seasonService.updateSeason(id, request));
    }
}