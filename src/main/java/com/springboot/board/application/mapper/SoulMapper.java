package com.springboot.board.application.mapper;

import com.springboot.board.api.v1.dto.request.SoulCreateRequest;
import com.springboot.board.api.v1.dto.request.SoulUpdateRequest;
import com.springboot.board.api.v1.dto.response.*;
import com.springboot.board.domain.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SoulMapper {

    @Mapping(target = "season", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "travelingVisits", ignore = true)
    SoulEntity toEntity(SoulCreateRequest request);

    @Mapping(target = "season", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "travelingVisits", ignore = true)
    void updateEntity(@MappingTarget SoulEntity entity, SoulUpdateRequest request);

    default SoulResponse toResponse(SoulEntity entity) {
        return SoulResponse.builder()
                .id(entity.getId())
                .seasonId(entity.getSeason() != null ? entity.getSeason().getId() : null)
                .seasonName(entity.getSeasonName())
                .seasonColor(entity.getSeason() != null ? entity.getSeason().getColor() : null)
                .name(entity.getName())
                .orderNum(entity.getOrderNum())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .rerunCount(entity.getRerunCount())
                .keywords(entity.getKeywords())
                .creator(entity.getCreator())
                .description(entity.getDescription())
                .isSeasonGuide(entity.isSeasonGuide())
                // ✅ fromEntities 사용
                .images(ImageResponse.fromEntities(entity.getImages()))
                .travelingVisits(entity.getTravelingVisits() != null ?
                        entity.getTravelingVisits().stream()
                                .map(this::visitToResponse)
                                .collect(Collectors.toList()) : null)
                .totalVisits(entity.getTotalVisitCount())
                .hasVisitedAsTS(entity.hasVisitedAsTS())
                .lastVisitDate(entity.getLastTravelingVisitDate())
                .build();
    }

    default SoulSummaryResponse toSummaryResponse(SoulEntity entity) {
        String repImageUrl = entity.getImages() != null ?
                entity.getImages().stream()
                        .filter(img -> "REPRESENTATIVE".equals(img.getImageType()))
                        .findFirst()
                        .map(ImageEntity::getUrl)
                        .orElse(null) : null;

        return SoulSummaryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .representativeImageUrl(repImageUrl)
                .totalVisits(entity.getTotalVisitCount())
                .isSeasonGuide(entity.isSeasonGuide())
                .build();
    }

    default TravelingVisitResponse visitToResponse(TravelingVisitEntity entity) {
        LocalDate today = LocalDate.now();
        long daysSince = ChronoUnit.DAYS.between(entity.getEndDate(), today);

        return TravelingVisitResponse.builder()
                .id(entity.getId())
                .soulId(entity.getSoul().getId())
                .soulName(entity.getSoul().getName())
                .visitNumber(entity.getVisitNumber())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isWarbandVisit(entity.isWarbandVisit())
                .notes(entity.getNotes())
                .daysSinceEnd(Math.max(0, daysSince))
                // ✅ fromEntities 사용
                .visitImages(ImageResponse.fromEntities(entity.getVisitImages()))
                .build();
    }

    // imageToResponse 메소드는 이제 필요 없음 (삭제 가능)
}