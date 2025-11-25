package com.springboot.board.application.service;

import com.springboot.board.api.v1.dto.request.SoulCreateRequest;
import com.springboot.board.api.v1.dto.request.SoulUpdateRequest;
import com.springboot.board.api.v1.dto.response.SoulResponse;
import com.springboot.board.application.mapper.SoulMapper;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.repository.SoulRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
// SoulService.java 상단에 추가할 import

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoulService {

    private final SoulRepository soulRepository;
    private final SoulMapper mapper;

    @Transactional
    public SoulResponse createSoul(SoulCreateRequest req) {
        SoulEntity entity = mapper.toEntity(req);

        if (req.getImages() != null) {
            for (ImageEntity img : req.getImages()) {
                img.setSoul(entity); // 연관관계 연결
            }
            entity.setImages(req.getImages());
        }

        SoulEntity saved = soulRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Transactional
    public SoulResponse updateSoul(Integer id, SoulUpdateRequest req) {
        SoulEntity entity = soulRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("영혼 노드를 찾을 수 없습니다. id=" + id));
        mapper.updateEntity(entity, req);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void deleteSoul(Integer id) {
        if (!soulRepository.existsById(id)) {
            throw new DataNotFoundException("영혼 노드를 찾을 수 없습니다. id=" + id);
        }
        soulRepository.deleteById(id);
    }

    /** 단건 조회 */
    public SoulResponse getSoul(Integer id) {
        SoulEntity soul = soulRepository
                .findWithImagesById(id)
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + id));
        return mapper.toResponse(soul);
    }

    /** 페이지 목록 */
    public Page<SoulResponse> getSouls(int page) {
        Pageable pageable = PageRequest.of(page, 15,
                Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("name")));
        return soulRepository.findAll(pageable)
                             .map(mapper::toResponse);
    }

    /** 전체 목록 (내림차순) */
    public List<SoulResponse> getAllSouls() {
        return soulRepository.findAll(
                Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("name")))
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /** 전체 목록 (오름차순) */
    public List<SoulResponse> getAllSoulsReversed() {
        return soulRepository.findAll(
                Sort.by(Sort.Order.asc("startDate"), Sort.Order.asc("name")))
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /** 검색 */
    public List<SoulResponse> searchSouls(String q) {
        return soulRepository.searchSouls(q)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /** 이전/다음 이웃 조회 */
    public Map<String, List<SoulResponse>> getNeighbors(Integer id) {
        SoulEntity current = soulRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("영혼 노드를 찾을 수 없습니다. id=" + id));

        // 동일 정렬 기준으로 전체 목록 조회
        List<SoulEntity> all = soulRepository.findAll(
                Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("name")));

        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(id)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            throw new DataNotFoundException("영혼 노드를 찾을 수 없습니다. id=" + id);
        }

        // 이전 최대 2개
        List<SoulResponse> prev = new ArrayList<>();
        for (int i = Math.max(0, idx - 2); i < idx; i++) {
            prev.add(mapper.toResponse(all.get(i)));
        }
        Collections.reverse(prev);

        // 다음 최대 2개
        List<SoulResponse> next = new ArrayList<>();
        for (int i = idx + 1; i <= Math.min(all.size() - 1, idx + 2); i++) {
            next.add(mapper.toResponse(all.get(i)));
        }

        Map<String, List<SoulResponse>> result = new HashMap<>();
        result.put("prev", prev);
        result.put("next", next);
        return result;
    }

    // SoulService.java에 추가할 메소드

/** 가장 오랫동안 안 온 영혼들 조회 */
public List<Map<String, Object>> getOldestSpirits() {
    // 1. 모든 영혼 조회
    List<SoulEntity> allSouls = soulRepository.findAll();
    
    // 2. 이름별로 그룹화하여 가장 최근 영혼만 선택
    Map<String, SoulEntity> latestByName = allSouls.stream()
        .collect(Collectors.toMap(
            SoulEntity::getName,
            soul -> soul,
            (existing, replacement) -> {
                // 동일 이름일 경우 더 최근 startDate를 가진 것 선택
                if (replacement.getStartDate().isAfter(existing.getStartDate())) {
                    return replacement;
                } else if (replacement.getStartDate().equals(existing.getStartDate())) {
                    // 시작일이 같으면 endDate가 더 최근인 것 선택
                    return replacement.getEndDate().isAfter(existing.getEndDate()) ? replacement : existing;
                }
                return existing;
            }
        ));
    
    // 3. 현재 날짜 기준으로 안 온 기간 계산 및 정렬
    LocalDate today = LocalDate.now();
    
    return latestByName.values().stream()
        .map(soul -> {
            // 마지막으로 온 날짜는 endDate
            LocalDate lastVisitDate = soul.getEndDate();
            long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisitDate, today);
            
            // 음수면 아직 진행 중이거나 미래 일정
            if (daysSinceLastVisit < 0) {
                daysSinceLastVisit = 0;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("soul", mapper.toResponse(soul));
            result.put("daysSinceLastVisit", daysSinceLastVisit);
            result.put("lastVisitDate", lastVisitDate);
            result.put("isActive", daysSinceLastVisit == 0); // 현재 활성 상태인지
            
            return result;
        })
        // 안 온 기간 내림차순 정렬 (가장 오래 안 온 순서)
        .sorted((a, b) -> Long.compare(
            (Long) b.get("daysSinceLastVisit"), 
            (Long) a.get("daysSinceLastVisit")
        ))
        .collect(Collectors.toList());
}
// SoulService.java에 추가할 메소드

/** 가장 오랫동안 안 온 영혼들 조회 (페이징 지원) */
public Page<Map<String, Object>> getOldestSpirits(int page, int size) {
    // 1. 모든 영혼 조회
    List<SoulEntity> allSouls = soulRepository.findAll();
    
    // 2. 이름별로 그룹화하여 가장 최근 영혼만 선택
    Map<String, SoulEntity> latestByName = allSouls.stream()
        .collect(Collectors.toMap(
            SoulEntity::getName,
            soul -> soul,
            (existing, replacement) -> {
                // 동일 이름일 경우 더 최근 startDate를 가진 것 선택
                if (replacement.getStartDate().isAfter(existing.getStartDate())) {
                    return replacement;
                } else if (replacement.getStartDate().equals(existing.getStartDate())) {
                    // 시작일이 같으면 endDate가 더 최근인 것 선택
                    return replacement.getEndDate().isAfter(existing.getEndDate()) ? replacement : existing;
                }
                return existing;
            }
        ));
    
    // 3. 현재 날짜 기준으로 안 온 기간 계산 및 정렬
    LocalDate today = LocalDate.now();
    
    List<Map<String, Object>> allResults = latestByName.values().stream()
        .map(soul -> {
            // 마지막으로 온 날짜는 endDate
            LocalDate lastVisitDate = soul.getEndDate();
            long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisitDate, today);
            
            // 음수면 아직 진행 중이거나 미래 일정
            if (daysSinceLastVisit < 0) {
                daysSinceLastVisit = 0;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("soul", mapper.toResponse(soul));
            result.put("daysSinceLastVisit", daysSinceLastVisit);
            result.put("lastVisitDate", lastVisitDate);
            result.put("isActive", daysSinceLastVisit == 0); // 현재 활성 상태인지
            
            return result;
        })
        // 안 온 기간 내림차순 정렬 (가장 오래 안 온 순서)
        .sorted((a, b) -> Long.compare(
            (Long) b.get("daysSinceLastVisit"), 
            (Long) a.get("daysSinceLastVisit")
        ))
        .collect(Collectors.toList());
    
    // 4. 페이징 처리
    int totalElements = allResults.size();
    int startIndex = page * size;
    int endIndex = Math.min(startIndex + size, totalElements);
    
    List<Map<String, Object>> pagedResults = allResults.subList(startIndex, endIndex);
    
    Pageable pageable = PageRequest.of(page, size);
    return new PageImpl<>(pagedResults, pageable, totalElements);
}

}
