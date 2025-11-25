package com.springboot.board.application.service;

import com.springboot.board.api.v1.dto.request.TravelingVisitCreateRequest;
import com.springboot.board.api.v1.dto.response.TravelingVisitResponse;
import com.springboot.board.application.mapper.SoulMapper;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.entity.TravelingVisitEntity;
import com.springboot.board.domain.repository.SoulRepository;
import com.springboot.board.domain.repository.TravelingVisitRepository;
import lombok.RequiredArgsConstructor;
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
                .notes(request.getNotes())
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
}