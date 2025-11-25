package com.springboot.board.application.service;

import com.springboot.board.api.v1.dto.request.SeasonCreateRequest;
import com.springboot.board.api.v1.dto.response.SeasonResponse;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.SeasonEntity;
import com.springboot.board.domain.repository.IAPItemRepository;
import com.springboot.board.domain.repository.SeasonRepository;
import com.springboot.board.domain.repository.SoulRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final SoulRepository soulRepository;
    private final IAPItemRepository iapItemRepository;

    public List<SeasonResponse> getAllSeasons() {
        List<SeasonEntity> seasons = seasonRepository.findAllByOrderByOrderNumAsc();
        
        // 영혼 개수 조회
        List<Map<String, Object>> soulCounts = soulRepository.countBySeasonGrouped();
        Map<Integer, Long> soulCountMap = new HashMap<>();
        for (Map<String, Object> row : soulCounts) {
            soulCountMap.put((Integer) row.get("seasonId"), (Long) row.get("count"));
        }

        return seasons.stream()
                .map(season -> toResponse(season, soulCountMap))
                .collect(Collectors.toList());
    }

    public SeasonResponse getSeasonById(Integer id) {
        SeasonEntity season = seasonRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("시즌을 찾을 수 없습니다. id=" + id));
        
        int spiritCount = soulRepository.findBySeasonId(id).size();
        int iapCount = iapItemRepository.findBySeasonId(id).size();
        
        return SeasonResponse.builder()
                .id(season.getId())
                .name(season.getName())
                .orderNum(season.getOrderNum())
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .durationDays(season.getDurationDays())
                .color(season.getColor())
                .isCollaboration(season.isCollaboration())
                .totalSpirits(spiritCount)
                .totalIAPItems(iapCount)
                .build();
    }

    public List<SeasonResponse> getCollaborationSeasons() {
        return seasonRepository.findAllByIsCollaboration(true).stream()
                .map(season -> toResponse(season, new HashMap<>()))
                .collect(Collectors.toList());
    }

    @Transactional
    public SeasonResponse createSeason(SeasonCreateRequest request) {
        if (seasonRepository.existsByOrderNum(request.getOrderNum())) {
            throw new IllegalArgumentException("이미 존재하는 시즌 순서입니다. orderNum=" + request.getOrderNum());
        }

        SeasonEntity entity = SeasonEntity.builder()
                .name(request.getName())
                .orderNum(request.getOrderNum())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .color(request.getColor())
                .isCollaboration(request.isCollaboration())
                .build();

        SeasonEntity saved = seasonRepository.save(entity);
        return toResponse(saved, new HashMap<>());
    }

    @Transactional
    public void deleteSeason(Integer id) {
        if (!seasonRepository.existsById(id)) {
            throw new DataNotFoundException("시즌을 찾을 수 없습니다. id=" + id);
        }
        seasonRepository.deleteById(id);
    }

    private SeasonResponse toResponse(SeasonEntity entity, Map<Integer, Long> soulCountMap) {
        return SeasonResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .orderNum(entity.getOrderNum())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .durationDays(entity.getDurationDays())
                .color(entity.getColor())
                .isCollaboration(entity.isCollaboration())
                .totalSpirits(soulCountMap.getOrDefault(entity.getId(), 0L).intValue())
                .build();
    }
}