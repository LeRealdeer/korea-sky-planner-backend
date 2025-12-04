package com.springboot.board.application.service;

import com.springboot.board.api.v1.dto.request.TravelingVisitCreateRequest;
import com.springboot.board.api.v1.dto.request.TravelingVisitUpdateRequest;
import com.springboot.board.api.v1.dto.response.TravelingVisitResponse;
import com.springboot.board.api.v1.dto.response.TravelingVisitWithSoulResponse;
import com.springboot.board.application.mapper.SoulMapper;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.entity.TravelingVisitEntity;
import com.springboot.board.domain.repository.SoulRepository;
import com.springboot.board.domain.repository.TravelingVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelingVisitService {

    private final TravelingVisitRepository visitRepository;
    private final SoulRepository soulRepository;
    private final SoulMapper soulMapper;

    public List<TravelingVisitResponse> getVisitsBySoul(Integer soulId) {
        return visitRepository.findBySoulIdOrderByVisitNumberAsc(soulId).stream()
                .map(soulMapper::visitToResponse)
                .collect(Collectors.toList());
    }

    public List<TravelingVisitResponse> getCurrentVisits() {
        return visitRepository.findCurrentVisits(LocalDate.now()).stream()
                .map(soulMapper::visitToResponse)
                .collect(Collectors.toList());
    }

    public TravelingVisitResponse getVisitById(Long id) {
        TravelingVisitEntity visit = visitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("유랑 방문 기록을 찾을 수 없습니다. id=" + id));
        return soulMapper.visitToResponse(visit);
    }

    /**
     * 모든 유랑 이력 조회 (visitNumber > 0만, startDate 내림차순)
     */
    public Page<TravelingVisitWithSoulResponse> getAllVisitsWithSoul(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("visitNumber")));
        
        Page<TravelingVisitEntity> visits = visitRepository.findAllWithSoulAndImages(pageable);
        
        return visits.map(this::toVisitWithSoulResponse);
    }

    /**
     * 키워드로 유랑 이력 검색 (visitNumber > 0만, startDate 내림차순)
     */
    public Page<TravelingVisitWithSoulResponse> searchVisitsWithSoul(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("visitNumber")));
        
        Page<TravelingVisitEntity> visits = visitRepository.searchWithSoulAndImages(query, pageable);
        
        return visits.map(this::toVisitWithSoulResponse);
    }

    /**
     * TravelingVisitEntity -> TravelingVisitWithSoulResponse 변환
     */
    private TravelingVisitWithSoulResponse toVisitWithSoulResponse(TravelingVisitEntity visit) {
        SoulEntity soul = visit.getSoul();
        
        // 대표 이미지 URL 추출
        String representativeImageUrl = soul.getImages() != null 
            ? soul.getImages().stream()
                .filter(img -> "REPRESENTATIVE".equals(img.getImageType()))
                .findFirst()
                .map(ImageEntity::getUrl)
                .orElse(null)
            : null;

        return TravelingVisitWithSoulResponse.builder()
                .visitId(visit.getId())
                .visitNumber(visit.getVisitNumber())
                .startDate(visit.getStartDate())
                .endDate(visit.getEndDate())
                .isWarbandVisit(visit.isWarbandVisit())
                .soulId(soul.getId().longValue())
                .soulName(soul.getName())
                .seasonName(soul.getSeasonName())
                .orderNum(soul.getOrderNum())
                .rerunCount(soul.getRerunCount())
                .representativeImageUrl(representativeImageUrl)
                .build();
    }

    @Transactional
    public TravelingVisitResponse createVisit(TravelingVisitCreateRequest request) {
        SoulEntity soul = soulRepository.findById(request.getSoulId())
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + request.getSoulId()));

        if (visitRepository.findBySoulIdAndVisitNumber(request.getSoulId(), request.getVisitNumber()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 방문 번호입니다. visitNumber=" + request.getVisitNumber());
        }

        TravelingVisitEntity entity = TravelingVisitEntity.builder()
                .soul(soul)
                .visitNumber(request.getVisitNumber())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isWarbandVisit(request.isWarbandVisit())
                .build();

        TravelingVisitEntity saved = visitRepository.save(entity);
        return soulMapper.visitToResponse(saved);
    }

    @Transactional
    public void deleteVisit(Long id) {
        if (!visitRepository.existsById(id)) {
            throw new DataNotFoundException("유랑 방문 기록을 찾을 수 없습니다. id=" + id);
        }
        visitRepository.deleteById(id);
    }

    @Transactional
    public TravelingVisitResponse updateVisit(Long id, TravelingVisitUpdateRequest request) {
        TravelingVisitEntity visit = visitRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("유랑 방문 기록을 찾을 수 없습니다. id=" + id));

        // 방문 번호 중복 체크 (자기 자신은 제외)
        if (request.getVisitNumber() != null && !request.getVisitNumber().equals(visit.getVisitNumber())) {
            visitRepository.findBySoulIdAndVisitNumber(visit.getSoul().getId(), request.getVisitNumber())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new IllegalArgumentException(
                                    "이미 존재하는 방문 번호입니다. visitNumber=" + request.getVisitNumber());
                        }
                    });
        }

        // 필드 업데이트 (null이 아닌 것만)
        if (request.getVisitNumber() != null) {
            visit.setVisitNumber(request.getVisitNumber());
        }
        if (request.getStartDate() != null) {
            visit.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            visit.setEndDate(request.getEndDate());
        }
        if (request.getIsWarbandVisit() != null) {
            visit.setWarbandVisit(request.getIsWarbandVisit());
        }

        return soulMapper.visitToResponse(visit);
    }
}






