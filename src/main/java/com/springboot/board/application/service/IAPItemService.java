package com.springboot.board.application.service;

import com.springboot.board.api.v1.dto.request.IAPItemCreateRequest;
import com.springboot.board.api.v1.dto.response.IAPItemResponse;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.IAPItemEntity;
import com.springboot.board.domain.entity.SeasonEntity;
import com.springboot.board.domain.repository.IAPItemRepository;
import com.springboot.board.domain.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IAPItemService {

    private final IAPItemRepository iapItemRepository;
    private final SeasonRepository seasonRepository;

    public List<IAPItemResponse> getAllItems() {
        return iapItemRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<IAPItemResponse> getItemsBySeason(Integer seasonId) {
        return iapItemRepository.findBySeasonId(seasonId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<IAPItemResponse> getItemsByPurchaseType(String purchaseType) {
        return iapItemRepository.findByPurchaseType(purchaseType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<IAPItemResponse> searchItems(String query) {
        return iapItemRepository.searchItems(query).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IAPItemResponse createItem(IAPItemCreateRequest request) {
        SeasonEntity season = seasonRepository.findById(request.getSeasonId())
                .orElseThrow(() -> new DataNotFoundException("시즌을 찾을 수 없습니다. id=" + request.getSeasonId()));

        IAPItemEntity entity = IAPItemEntity.builder()
                .season(season)
                .name(request.getName())
                .category(request.getCategory())
                .purchaseType(request.getPurchaseType())
                .priceInfo(request.getPriceInfo())
                .keywords(request.getKeywords())
                .imageUrl(request.getImageUrl())
                .build();

        IAPItemEntity saved = iapItemRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!iapItemRepository.existsById(id)) {
            throw new DataNotFoundException("IAP 아이템을 찾을 수 없습니다. id=" + id);
        }
        iapItemRepository.deleteById(id);
    }

    private IAPItemResponse toResponse(IAPItemEntity entity) {
        return IAPItemResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .purchaseType(entity.getPurchaseType())
                .priceInfo(entity.getPriceInfo())
                .seasonId(entity.getSeason().getId())
                .seasonName(entity.getSeason().getName())
                .keywords(entity.getKeywords())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}