package com.lostandfound.service;

import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.dto.founditem.FoundItemSearchResultDto;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.mapper.FoundItemMapper;
import com.lostandfound.repository.FoundItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Public Search + Map/Location Module. Only VERIFIED, IN_CUSTODY found items
 * are ever returned here, and privateIdentifyingDetails is never exposed
 * (enforced by FoundItemMapper#toSearchResultDto, which never reads that field).
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final FoundItemRepository foundItemRepository;
    private final FoundItemMapper foundItemMapper;

    public PagedResponse<FoundItemSearchResultDto> search(UUID categoryId, String city, Double latitude, Double longitude,
                                                            Double radiusKm, LocalDate dateFrom, LocalDate dateTo,
                                                            String color, String brand, String keyword,
                                                            int page, int size) {
        Specification<FoundItem> spec = FoundItemSpecifications.combine(
                FoundItemSpecifications.hasCategory(categoryId),
                FoundItemSpecifications.cityLike(city),
                FoundItemSpecifications.foundDateFrom(dateFrom),
                FoundItemSpecifications.foundDateTo(dateTo),
                FoundItemSpecifications.hasColor(color),
                FoundItemSpecifications.hasBrand(brand),
                FoundItemSpecifications.descriptionContains(keyword)
        );

        List<FoundItem> candidates = foundItemRepository.findAll(spec);

        List<FoundItemSearchResultDto> results;
        if (latitude != null && longitude != null && radiusKm != null) {
            results = candidates.stream()
                    .map(item -> {
                        double distance = LocationService.distanceKm(latitude, longitude,
                                item.getLocation().getLatitude().doubleValue(), item.getLocation().getLongitude().doubleValue());
                        return Map.entry(item, distance);
                    })
                    .filter(e -> e.getValue() <= radiusKm)
                    .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(e -> foundItemMapper.toSearchResultDto(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        } else {
            results = candidates.stream()
                    .sorted(Comparator.comparing(FoundItem::getCreatedAt).reversed())
                    .map(item -> foundItemMapper.toSearchResultDto(item, null))
                    .collect(Collectors.toList());
        }

        int totalElements = results.size();
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<FoundItemSearchResultDto> pageContent = results.subList(fromIndex, toIndex);

        return PagedResponse.of(pageContent, page, size, totalElements);
    }

    public List<FoundItemSearchResultDto> nearby(double latitude, double longitude, double radiusKm) {
        Specification<FoundItem> spec = FoundItemSpecifications.combine();
        return foundItemRepository.findAll(spec).stream()
                .map(item -> Map.entry(item, LocationService.distanceKm(latitude, longitude,
                        item.getLocation().getLatitude().doubleValue(), item.getLocation().getLongitude().doubleValue())))
                .filter(e -> e.getValue() <= radiusKm)
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .map(e -> foundItemMapper.toSearchResultDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
