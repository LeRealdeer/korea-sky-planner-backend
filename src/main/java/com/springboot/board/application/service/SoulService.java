package com.springboot.board.application.service;

import com.springboot.board.api.v1.dto.request.SoulCreateRequest;
import com.springboot.board.api.v1.dto.request.SoulUpdateRequest;
import com.springboot.board.api.v1.dto.response.SoulResponse;
import com.springboot.board.application.mapper.SoulMapper;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.entity.SeasonEntity;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.entity.TravelingVisitEntity;
import com.springboot.board.domain.repository.SeasonRepository;
import com.springboot.board.domain.repository.SoulRepository;
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
 * - 유랑 이력(visitNumber > 0)이 있는 영혼만 표시
 * - 각 영혼 이름별로 가장 최근 유랑 방문 기록 기준
 * - 기간 제한 없음 (몇 년이 지났든 모두 표시)
 */
public Page<Map<String, Object>> getOldestSpirits(int page, int size) {
    // 1. 유랑 온 적 있는 영혼만 조회
    List<SoulEntity> allSouls = soulRepository.findAllWithTravelingVisits();
    
    if (allSouls.isEmpty()) {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }
    
    // 2. 이름별로 그룹화
    Map<String, List<SoulEntity>> groupedByName = allSouls.stream()
        .collect(Collectors.groupingBy(SoulEntity::getName));
    
    // 3. 각 그룹에서 가장 최근 유랑 방문 날짜 계산
    List<Map<String, Object>> results = new ArrayList<>();
    
    for (Map.Entry<String, List<SoulEntity>> entry : groupedByName.entrySet()) {
        List<SoulEntity> souls = entry.getValue();
        
        // 모든 유랑 이력 중 가장 최근 endDate 찾기 (visitNumber > 0만)
        Optional<LocalDate> lastVisitDateOpt = souls.stream()
            .flatMap(soul -> soul.getTravelingVisits().stream())
            .filter(visit -> visit.getVisitNumber() > 0) // visitNumber 0 제외 (시즌 당시)
            .map(TravelingVisitEntity::getEndDate)
            .max(LocalDate::compareTo);
        
        if (!lastVisitDateOpt.isPresent()) {
            continue; // 유랑 이력 없으면 스킵
        }
        
        LocalDate lastVisitDate = lastVisitDateOpt.get();
        
        // 해당 lastVisitDate를 가진 영혼 찾기
        SoulEntity representativeSoul = souls.stream()
            .filter(soul -> soul.getTravelingVisits().stream()
                .anyMatch(visit -> visit.getEndDate().equals(lastVisitDate)))
            .findFirst()
            .orElse(souls.get(0));
        
        Map<String, Object> result = new HashMap<>();
        result.put("soul", mapper.toResponse(representativeSoul));
        result.put("lastVisitDate", lastVisitDate);
        
        results.add(result);
    }
    
    // 4. lastVisitDate 기준으로 정렬 (오래된 순)
    results.sort((a, b) -> {
        LocalDate dateA = (LocalDate) a.get("lastVisitDate");
        LocalDate dateB = (LocalDate) b.get("lastVisitDate");
        return dateA.compareTo(dateB);
    });
    
    // 5. 페이징 처리
    int totalElements = results.size();
    int startIndex = page * size;
    
    if (startIndex >= totalElements) {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), totalElements);
    }
    
    int endIndex = Math.min(startIndex + size, totalElements);
    List<Map<String, Object>> pagedResults = results.subList(startIndex, endIndex);
    
    Pageable pageable = PageRequest.of(page, size);
    return new PageImpl<>(pagedResults, pageable, totalElements);
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