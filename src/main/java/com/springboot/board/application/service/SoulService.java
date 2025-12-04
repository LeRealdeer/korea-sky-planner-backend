package com.springboot.board.application.service;

import com.springboot.board.api.v1.dto.request.SoulCreateRequest;
import com.springboot.board.api.v1.dto.request.SoulUpdateRequest;
import com.springboot.board.api.v1.dto.response.ImageResponse;
import com.springboot.board.api.v1.dto.response.SoulResponse;
import com.springboot.board.application.mapper.SoulMapper;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.entity.SeasonEntity;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.entity.TravelingVisitEntity;
import com.springboot.board.domain.repository.SeasonRepository;
import com.springboot.board.domain.repository.SoulRepository;
import com.springboot.board.domain.repository.TravelingVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoulService {
private final TravelingVisitRepository travelingVisitRepository;
    private final SoulRepository soulRepository;
    private final SeasonRepository seasonRepository;
    private final SoulMapper mapper;

    /**
     * 영혼 생성
     */
/**
 * 영혼 생성
 */
@Transactional
public SoulResponse createSoul(SoulCreateRequest req) {
    // 1. 시즌 조회
    SeasonEntity season = seasonRepository.findById(req.getSeasonId())
            .orElseThrow(() -> new DataNotFoundException("시즌을 찾을 수 없습니다. id=" + req.getSeasonId()));

    // 2. Entity 변환
    SoulEntity entity = mapper.toEntity(req);
    entity.setSeason(season);

    // 3. 저장 (이미지는 별도의 ImageController를 통해 업로드)
    SoulEntity saved = soulRepository.save(entity);
    return mapper.toResponse(saved);
}

    /**
     * 영혼 수정
     */
    @Transactional
    public SoulResponse updateSoul(Integer id, SoulUpdateRequest req) {
        SoulEntity entity = soulRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + id));
        
        // MapStruct를 사용한 필드 업데이트
        mapper.updateEntity(entity, req);
        
        return mapper.toResponse(entity);
    }

    /**
     * 영혼 삭제
     */
    @Transactional
    public void deleteSoul(Integer id) {
        if (!soulRepository.existsById(id)) {
            throw new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + id);
        }
        soulRepository.deleteById(id);
    }

    /**
     * 영혼 단건 조회
     */
    public SoulResponse getSoul(Integer id) {
        SoulEntity soul = soulRepository
                .findWithImagesById(id)
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + id));
        return mapper.toResponse(soul);
    }

    /**
     * 영혼 목록 조회 (페이징)
     */
    public Page<SoulResponse> getSouls(int page) {
        Pageable pageable = PageRequest.of(page, 15,
                Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("name")));
        return soulRepository.findAll(pageable)
                .map(mapper::toResponse);
    }

    /**
     * 모든 영혼 조회 (내림차순)
     */
    public List<SoulResponse> getAllSouls() {
        return soulRepository.findAll(
                Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("name")))
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 모든 영혼 조회 (오름차순)
     */
    public List<SoulResponse> getAllSoulsReversed() {
        return soulRepository.findAll(
                Sort.by(Sort.Order.asc("startDate"), Sort.Order.asc("name")))
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 시즌별 영혼 조회 (추가!)
     */
    public List<SoulResponse> getSoulsBySeason(Integer seasonId) {
        return soulRepository.findBySeasonId(seasonId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
// src/main/java/com/springboot/board/application/service/SoulService.java
// src/main/java/com/springboot/board/application/service/SoulService.java

/**
 * 모든 유랑 이력 조회 (페이징)
 * - TravelingVisit 기반으로 모든 유랑 표시
 * - visitNumber > 0만 (시즌 당시 제외)
 * - 정렬: 시작일 내림차순 → 이름 오름차순
 */
public Page<Map<String, Object>> getAllTravelingVisits(int page, int size) {
    // 1. 모든 유랑 이력 조회
    List<TravelingVisitEntity> allVisits = travelingVisitRepository
        .findAllValidVisitsWithSoul();
    
    if (allVisits.isEmpty()) {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }
    
    // 2. 결과 리스트 생성
    List<Map<String, Object>> results = new ArrayList<>();
    LocalDate today = LocalDate.now();
    
    for (TravelingVisitEntity visit : allVisits) {
        SoulEntity soul = visit.getSoul();
        
        // 현재 진행중인지 체크
        boolean isActive = !today.isBefore(visit.getStartDate()) && 
                          !today.isAfter(visit.getEndDate());
        
        // SoulResponse 생성 (기존 mapper 활용)
        SoulResponse soulResponse = mapper.toResponse(soul);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", soul.getId());
        result.put("seasonId", soul.getSeason() != null ? soul.getSeason().getId() : null);
        result.put("seasonName", soul.getSeasonName());
        result.put("seasonColor", soul.getSeason() != null ? soul.getSeason().getColor() : null);
        result.put("name", soul.getName());
        result.put("orderNum", soul.getOrderNum());
        result.put("startDate", visit.getStartDate());
        result.put("endDate", visit.getEndDate());
        result.put("rerunCount", soul.getRerunCount());
        result.put("keywords", soul.getKeywords());
        result.put("creator", soul.getCreator());
        result.put("description", soul.getDescription());
        result.put("isSeasonGuide", soul.isSeasonGuide());
        result.put("images", ImageResponse.fromEntities(soul.getImages()));
        
        // TravelingVisit 관련 정보
        result.put("visitNumber", visit.getVisitNumber());
        result.put("isWarbandVisit", visit.isWarbandVisit());
        result.put("isActive", isActive);
        result.put("__travelingVisitId", visit.getId()); // 고유 식별자
        
        results.add(result);
    }
    
    // 3. 정렬: 시작일 내림차순 → 이름 오름차순
    results.sort((a, b) -> {
        LocalDate dateA = (LocalDate) a.get("startDate");
        LocalDate dateB = (LocalDate) b.get("startDate");
        
        int dateCompare = dateB.compareTo(dateA); // 최신순
        if (dateCompare != 0) {
            return dateCompare;
        }
        
        // 시작일이 같으면 이름 순
        String nameA = (String) a.get("name");
        String nameB = (String) b.get("name");
        return nameA.compareTo(nameB);
    });
    
    // 4. 페이징 처리
    int totalElements = results.size();
    int startIndex = page * size;
    
    if (startIndex >= totalElements) {
        return new PageImpl<>(Collections.emptyList(), 
                             PageRequest.of(page, size), totalElements);
    }
    
    int endIndex = Math.min(startIndex + size, totalElements);
    List<Map<String, Object>> pagedResults = results.subList(startIndex, endIndex);
    
    return new PageImpl<>(pagedResults, PageRequest.of(page, size), totalElements);
}
/**
 * 일반 유랑 대백과 조회
 * - 모든 유랑 이력 표시 (visitNumber > 0만)
 * - 같은 영혼이 여러 번 와도 각각 표시
 * - 정렬: 유랑 시작일 기준 내림차순 → 같으면 영혼 이름 오름차순
 */
public Page<Map<String, Object>> getTravelingEncyclopedia(int page, int size) {
    // 1. 모든 유랑 이력 조회 (visitNumber > 0만)
    List<TravelingVisitEntity> allVisits = travelingVisitRepository
        .findAllValidVisitsWithSoul();
    
    if (allVisits.isEmpty()) {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }
    
    // 2. 결과 리스트 생성
    List<Map<String, Object>> results = new ArrayList<>();
    LocalDate today = LocalDate.now();
    
    for (TravelingVisitEntity visit : allVisits) {
        SoulEntity soul = visit.getSoul();
        
        // 현재 진행중인지 체크
        boolean isActive = !today.isBefore(visit.getStartDate()) && 
                          !today.isAfter(visit.getEndDate());
        
        Map<String, Object> result = new HashMap<>();
        result.put("soul", mapper.toResponse(soul));
        result.put("visitNumber", visit.getVisitNumber());
        result.put("startDate", visit.getStartDate());
        result.put("endDate", visit.getEndDate());
        result.put("isWarbandVisit", visit.isWarbandVisit());
        result.put("isActive", isActive);
        
        results.add(result);
    }
    
    // 3. 정렬: 시작일 내림차순 → 이름 오름차순
    results.sort((a, b) -> {
        LocalDate dateA = (LocalDate) a.get("startDate");
        LocalDate dateB = (LocalDate) b.get("startDate");
        
        int dateCompare = dateB.compareTo(dateA); // 최신순
        if (dateCompare != 0) {
            return dateCompare;
        }
        
        // 시작일이 같으면 이름 순
        String nameA = ((SoulResponse) a.get("soul")).getName();
        String nameB = ((SoulResponse) b.get("soul")).getName();
        return nameA.compareTo(nameB);
    });
    
    // 4. 페이징 처리
    int totalElements = results.size();
    int startIndex = page * size;
    
    if (startIndex >= totalElements) {
        return new PageImpl<>(Collections.emptyList(), 
                             PageRequest.of(page, size), totalElements);
    }
    
    int endIndex = Math.min(startIndex + size, totalElements);
    List<Map<String, Object>> pagedResults = results.subList(startIndex, endIndex);
    
    return new PageImpl<>(pagedResults, PageRequest.of(page, size), totalElements);
}
    /**
     * 영혼 검색
     */
    public List<SoulResponse> searchSouls(String query) {
        return soulRepository.searchSouls(query)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 이전/다음 이웃 조회
     */
    public Map<String, List<SoulResponse>> getNeighbors(Integer id) {
        SoulEntity current = soulRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + id));

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
            throw new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + id);
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
/**
 * 가장 오랫동안 안 온 영혼들 조회 (페이징)
 * - TravelingVisit 기반으로 같은 영혼은 가장 최근 유랑만 표시
 * - visitNumber > 0인 유랑만 대상 (시즌 당시 제외)
 * - 마지막 방문일 기준 오래된 순 정렬
 */
public Page<Map<String, Object>> getOldestSpirits(int page, int size) {
    LocalDate today = LocalDate.now();
    
    // 1. 모든 유랑 이력 조회 (visitNumber > 0만, Soul과 함께)
    List<TravelingVisitEntity> allVisits = travelingVisitRepository
        .findAllValidVisitsWithSoul();
    
    if (allVisits.isEmpty()) {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }
    
    // 2. 영혼 이름별로 그룹화하고 가장 최근 방문만 선택
    Map<String, TravelingVisitEntity> latestVisitPerSoul = new HashMap<>();
    
    for (TravelingVisitEntity visit : allVisits) {
        String soulName = visit.getSoul().getName();
        
        if (!latestVisitPerSoul.containsKey(soulName)) {
            latestVisitPerSoul.put(soulName, visit);
        } else {
            TravelingVisitEntity existing = latestVisitPerSoul.get(soulName);
            // 더 최근 방문으로 교체
            if (visit.getEndDate().isAfter(existing.getEndDate())) {
                latestVisitPerSoul.put(soulName, visit);
            }
        }
    }
    
    // 3. 결과 리스트 생성
    List<Map<String, Object>> results = new ArrayList<>();
    
    for (TravelingVisitEntity visit : latestVisitPerSoul.values()) {
        SoulEntity soul = visit.getSoul();
        LocalDate lastVisitDate = visit.getEndDate();
        long daysSince = ChronoUnit.DAYS.between(lastVisitDate, today);
        
        // 현재 진행중인지 체크
        boolean isActive = !today.isBefore(visit.getStartDate()) && 
                          !today.isAfter(visit.getEndDate());
        
        Map<String, Object> result = new HashMap<>();
        result.put("soul", mapper.toResponse(soul));
        result.put("lastVisitDate", lastVisitDate);
        result.put("daysSinceLastVisit", daysSince);
        result.put("isActive", isActive);
        result.put("visitNumber", visit.getVisitNumber());
        
        results.add(result);
    }
    
    // 4. daysSince 기준 오름차순 정렬 (오래된 순)
    results.sort((a, b) -> {
        Long daysA = (Long) a.get("daysSinceLastVisit");
        Long daysB = (Long) b.get("daysSinceLastVisit");
        return daysA.compareTo(daysB);
    });
    
    // 5. 페이징 처리
    int totalElements = results.size();
    int startIndex = page * size;
    
    if (startIndex >= totalElements) {
        return new PageImpl<>(Collections.emptyList(), 
                             PageRequest.of(page, size), totalElements);
    }
    
    int endIndex = Math.min(startIndex + size, totalElements);
    List<Map<String, Object>> pagedResults = results.subList(startIndex, endIndex);
    
    return new PageImpl<>(pagedResults, PageRequest.of(page, size), totalElements);
}
    /**
     * 🎯 TODO: TravelingVisit을 활용한 정확한 오래된 유랑 계산 (미래 구현)
     * 
     * 이 메소드는 나중에 TravelingVisit 데이터가 충분히 쌓이면
     * 위의 getOldestSpirits()를 대체할 예정입니다.
     */
    /*
    public Page<SoulSummaryResponse> getOldestSpiritsV2(int page, int size) {
        List<SoulEntity> allSouls = soulRepository.findAllWithVisits();
        LocalDate today = LocalDate.now();

        Map<String, List<SoulEntity>> groupedByName = allSouls.stream()
                .collect(Collectors.groupingBy(SoulEntity::getName));

        List<SoulSummaryResponse> results = new ArrayList<>();

        for (Map.Entry<String, List<SoulEntity>> entry : groupedByName.entrySet()) {
            List<SoulEntity> souls = entry.getValue();

            Optional<LocalDate> lastVisitDate = souls.stream()
                    .flatMap(soul -> soul.getTravelingVisits().stream())
                    .filter(visit -> visit.getVisitNumber() > 0)
                    .map(TravelingVisitEntity::getEndDate)
                    .max(LocalDate::compareTo);

            if (lastVisitDate.isPresent()) {
                long daysSince = ChronoUnit.DAYS.between(lastVisitDate.get(), today);
                SoulEntity representative = souls.get(0);

                SoulSummaryResponse summary = SoulSummaryResponse.builder()
                        .id(representative.getId())
                        .name(representative.getName())
                        .representativeImageUrl(getRepresentativeImageUrl(representative))
                        .totalVisits(representative.getTotalVisitCount())
                        .daysSinceLastVisit(daysSince)
                        .lastVisitDate(lastVisitDate.get())
                        .isSeasonGuide(representative.isSeasonGuide())
                        .build();

                results.add(summary);
            }
        }

        results.sort(Comparator.comparing(SoulSummaryResponse::getDaysSinceLastVisit).reversed());

        int start = page * size;
        int end = Math.min(start + size, results.size());

        if (start >= results.size()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), results.size());
        }

        return new PageImpl<>(results.subList(start, end), PageRequest.of(page, size), results.size());
    }
    */

    /**
     * 대표 이미지 URL 추출
     */
    private String getRepresentativeImageUrl(SoulEntity soul) {
        return soul.getImages() != null ?
                soul.getImages().stream()
                        .filter(img -> "REPRESENTATIVE".equals(img.getImageType()))
                        .findFirst()
                        .map(ImageEntity::getUrl)
                        .orElse(null) : null;
    }
}