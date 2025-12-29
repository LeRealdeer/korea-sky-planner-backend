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
    @Transactional
    public SoulResponse createSoul(SoulCreateRequest req) {
        // 1. 시즌 조회
        SeasonEntity season = seasonRepository.findById(req.getSeasonId())
                .orElseThrow(() -> new DataNotFoundException("시즌을 찾을 수 없습니다. id=" + req.getSeasonId()));

        // 2. Entity 변환
        SoulEntity entity = mapper.toEntity(req);
        entity.setSeason(season);

        // 3. 저장
        SoulEntity saved = soulRepository.save(entity);
        return mapper.toResponse(saved);
    }

    /**
     * 영혼 수정 - 500 에러 해결
     */
    @Transactional
    public SoulResponse updateSoul(Integer id, SoulUpdateRequest req) {
        SoulEntity entity = soulRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + id));

        // MapStruct 대신 수동으로 필드 업데이트
        if (req.getName() != null && !req.getName().isBlank()) {
            entity.setName(req.getName());
        }
        
        if (req.getSeasonName() != null && !req.getSeasonName().isBlank()) {
            entity.setSeasonName(req.getSeasonName());
        }
        
        if (req.getOrderNum() != null) {
            entity.setOrderNum(req.getOrderNum());
        }
        
        if (req.getStartDate() != null) {
            entity.setStartDate(req.getStartDate());
        }
        
        if (req.getEndDate() != null) {
            entity.setEndDate(req.getEndDate());
        }
        
        if (req.getRerunCount() != null) {
            entity.setRerunCount(req.getRerunCount());
        }
        
        // 키워드 리스트 업데이트
        if (req.getKeywords() != null) {
            entity.setKeywords(req.getKeywords());
        }
        
        if (req.getCreator() != null) {
            entity.setCreator(req.getCreator());
        }
        
        if (req.getDescription() != null) {
            entity.setDescription(req.getDescription());
        }
        
        if (req.getIsSeasonGuide() != null) {
            entity.setSeasonGuide(req.getIsSeasonGuide());
        }

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

    public Page<SoulResponse> getSouls(int page, int size, String seasonName, String query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderNum").ascending());
        Page<SoulEntity> soulPage;

        // 시즌 + 검색 둘 다 있는 경우
        if (seasonName != null && !seasonName.isEmpty() && query != null && !query.isEmpty()) {
            soulPage = soulRepository.findBySeasonNameAndQuery(seasonName, query, pageable);
        }
        // 시즌만 있는 경우
        else if (seasonName != null && !seasonName.isEmpty()) {
            soulPage = soulRepository.findBySeasonName(seasonName, pageable);
        }
        // 검색만 있는 경우
        else if (query != null && !query.isEmpty()) {
            soulPage = soulRepository.findByNameOrKeywordsContaining(query, pageable);
        }
        // 둘 다 없는 경우 (전체)
        else {
            soulPage = soulRepository.findAll(pageable);
        }

        return soulPage.map(mapper::toResponse);
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
     * 시즌별 영혼 조회
     */
    public List<SoulResponse> getSoulsBySeason(Integer seasonId) {
        return soulRepository.findBySeasonId(seasonId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 모든 유랑 이력 조회 (페이징) - ✅ globalOrder 추가
     */
    public Page<Map<String, Object>> getAllTravelingVisits(int page, int size) {
        List<TravelingVisitEntity> allVisits = travelingVisitRepository
                .findAllValidVisitsWithSoul();

        if (allVisits.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (TravelingVisitEntity visit : allVisits) {
            SoulEntity soul = visit.getSoul();

            boolean isActive = !today.isBefore(visit.getStartDate()) &&
                    !today.isAfter(visit.getEndDate());

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

            result.put("visitNumber", visit.getVisitNumber());
            result.put("globalOrder", visit.getGlobalOrder()); // ✅ 추가!
            result.put("isWarbandVisit", visit.isWarbandVisit());
            result.put("isActive", isActive);
            result.put("__travelingVisitId", visit.getId());

            results.add(result);
        }

        results.sort((a, b) -> {
            LocalDate dateA = (LocalDate) a.get("startDate");
            LocalDate dateB = (LocalDate) b.get("startDate");

            int dateCompare = dateB.compareTo(dateA);
            if (dateCompare != 0) {
                return dateCompare;
            }

            String nameA = (String) a.get("name");
            String nameB = (String) b.get("name");
            return nameA.compareTo(nameB);
        });

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
     */
    public Page<Map<String, Object>> getTravelingEncyclopedia(int page, int size) {
        List<TravelingVisitEntity> allVisits = travelingVisitRepository
                .findAllValidVisitsWithSoul();

        if (allVisits.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (TravelingVisitEntity visit : allVisits) {
            SoulEntity soul = visit.getSoul();

            boolean isActive = !today.isBefore(visit.getStartDate()) &&
                    !today.isAfter(visit.getEndDate());

            Map<String, Object> result = new HashMap<>();
            result.put("soul", mapper.toResponse(soul));
            result.put("visitNumber", visit.getVisitNumber());
            result.put("globalOrder", visit.getGlobalOrder()); // ✅ 추가!
            result.put("startDate", visit.getStartDate());
            result.put("endDate", visit.getEndDate());
            result.put("isWarbandVisit", visit.isWarbandVisit());
            result.put("isActive", isActive);

            results.add(result);
        }

        results.sort((a, b) -> {
            LocalDate dateA = (LocalDate) a.get("startDate");
            LocalDate dateB = (LocalDate) b.get("startDate");

            int dateCompare = dateB.compareTo(dateA);
            if (dateCompare != 0) {
                return dateCompare;
            }

            String nameA = ((SoulResponse) a.get("soul")).getName();
            String nameB = ((SoulResponse) b.get("soul")).getName();
            return nameA.compareTo(nameB);
        });

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

        List<SoulResponse> prev = new ArrayList<>();
        for (int i = Math.max(0, idx - 2); i < idx; i++) {
            prev.add(mapper.toResponse(all.get(i)));
        }
        Collections.reverse(prev);

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
     */
    public Page<Map<String, Object>> getOldestSpirits(int page, int size) {
        LocalDate today = LocalDate.now();

        List<TravelingVisitEntity> allVisits = travelingVisitRepository
                .findAllValidVisitsWithSoul();

        if (allVisits.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        }

        Map<String, TravelingVisitEntity> latestVisitPerSoul = new HashMap<>();

        for (TravelingVisitEntity visit : allVisits) {
            String soulName = visit.getSoul().getName();

            if (!latestVisitPerSoul.containsKey(soulName)) {
                latestVisitPerSoul.put(soulName, visit);
            } else {
                TravelingVisitEntity existing = latestVisitPerSoul.get(soulName);
                if (visit.getEndDate().isAfter(existing.getEndDate())) {
                    latestVisitPerSoul.put(soulName, visit);
                }
            }
        }

        List<Map<String, Object>> results = new ArrayList<>();

        for (TravelingVisitEntity visit : latestVisitPerSoul.values()) {
            SoulEntity soul = visit.getSoul();
            LocalDate lastVisitDate = visit.getEndDate();
            long daysSince = ChronoUnit.DAYS.between(lastVisitDate, today);

            boolean isActive = !today.isBefore(visit.getStartDate()) &&
                    !today.isAfter(visit.getEndDate());

            Map<String, Object> result = new HashMap<>();
            result.put("soul", mapper.toResponse(soul));
            result.put("lastVisitDate", lastVisitDate);
            result.put("daysSinceLastVisit", daysSince);
            result.put("isActive", isActive);
            result.put("visitNumber", visit.getVisitNumber());
            result.put("globalOrder", visit.getGlobalOrder()); // ✅ 추가!

            results.add(result);
        }

        results.sort((a, b) -> {
            Long daysA = (Long) a.get("daysSinceLastVisit");
            Long daysB = (Long) b.get("daysSinceLastVisit");
            return daysA.compareTo(daysB);
        });

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
     * 대표 이미지 URL 추출
     */
    private String getRepresentativeImageUrl(SoulEntity soul) {
        return soul.getImages() != null ? soul.getImages().stream()
                .filter(img -> "REPRESENTATIVE".equals(img.getImageType()))
                .findFirst()
                .map(ImageEntity::getUrl)
                .orElse(null) : null;
    }
}