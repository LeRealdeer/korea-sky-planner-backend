package com.springboot.board.application.mapper;

import com.springboot.board.api.v1.dto.request.SoulCreateRequest;
import com.springboot.board.api.v1.dto.request.SoulUpdateRequest;
import com.springboot.board.api.v1.dto.response.ImageResponse;
import com.springboot.board.api.v1.dto.response.SoulResponse;
import com.springboot.board.domain.entity.SoulEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SoulMapper {

    SoulEntity toEntity(SoulCreateRequest request);

    void updateEntity(@MappingTarget SoulEntity entity, SoulUpdateRequest request);

    default SoulResponse toResponse(SoulEntity entity) {
        return SoulResponse.builder()
                .id(entity.getId())
                .seasonName(entity.getSeasonName())
                .name(entity.getName())
                .orderNum(entity.getOrderNum())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .rerunCount(entity.getRerunCount())
                .keywords(entity.getKeywords())
                .creator(entity.getCreator())
                .description(entity.getDescription())
                .images(
                        entity.getImages().stream()
                                .map(img -> ImageResponse.builder()
                                        .id(img.getId()) // ⚡️추가
                                        .imageType(img.getImageType())
                                        .url(img.getUrl())
                                        .build())
                                .toList())
                .build();
    }

}
