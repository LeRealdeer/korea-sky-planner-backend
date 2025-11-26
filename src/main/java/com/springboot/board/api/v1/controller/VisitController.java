package com.springboot.board.api.v1.controller;

import com.springboot.board.api.v1.dto.request.TravelingVisitCreateRequest;
import com.springboot.board.api.v1.dto.request.TravelingVisitUpdateRequest;
import com.springboot.board.api.v1.dto.response.TravelingVisitResponse;
import com.springboot.board.application.service.TravelingVisitService;
import com.springboot.board.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Visit", description = "유랑 방문 기록 API")
@RestController
@RequestMapping("/api/v1/visits")
@RequiredArgsConstructor
public class VisitController {

    private final TravelingVisitService visitService;

    @Operation(summary = "영혼의 유랑 이력 조회", description = "특정 영혼의 모든 유랑 방문 기록을 조회합니다.")
    @GetMapping("/soul/{soulId}")
    public ApiResponse<List<TravelingVisitResponse>> getVisitsBySoul(@PathVariable Integer soulId) {
        return ApiResponse.success(visitService.getVisitsBySoul(soulId));
    }

    @Operation(summary = "현재 진행중인 유랑 조회", description = "현재 진행중인 모든 유랑을 조회합니다.")
    @GetMapping("/current")
    public ApiResponse<List<TravelingVisitResponse>> getCurrentVisits() {
        return ApiResponse.success(visitService.getCurrentVisits());
    }

    @Operation(summary = "유랑 방문 상세 조회", description = "특정 ID의 유랑 방문 기록을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<TravelingVisitResponse> getVisitById(@PathVariable Long id) {
        return ApiResponse.success(visitService.getVisitById(id));
    }

    @Operation(summary = "유랑 방문 기록 생성", description = "새로운 유랑 방문 기록을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TravelingVisitResponse> createVisit(@Valid @RequestBody TravelingVisitCreateRequest request) {
        return ApiResponse.success(visitService.createVisit(request));
    }

    @Operation(summary = "유랑 방문 기록 삭제", description = "특정 ID의 유랑 방문 기록을 삭제합니다.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
    }

    @Operation(summary = "유랑 방문 기록 수정", description = "기존 유랑 방문 기록을 수정합니다.")
    @PutMapping("/{id}")
    public ApiResponse<TravelingVisitResponse> updateVisit(
            @PathVariable Long id,
            @Valid @RequestBody TravelingVisitUpdateRequest request) {
        return ApiResponse.success(visitService.updateVisit(id, request));
    }
}