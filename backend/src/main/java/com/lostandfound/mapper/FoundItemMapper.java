package com.lostandfound.mapper;

import com.lostandfound.dto.founditem.FoundItemAdminDto;
import com.lostandfound.dto.founditem.FoundItemPublicDto;
import com.lostandfound.dto.founditem.FoundItemSearchResultDto;
import com.lostandfound.entity.Case;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.ItemPhoto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class FoundItemMapper {

    private final LocationMapper locationMapper;
    private final PhotoMapper photoMapper;

    public FoundItemPublicDto toPublicDto(FoundItem item) {
        if (item == null) return null;
        return FoundItemPublicDto.builder()
                .foundItemId(item.getFoundItemId())
                .category(item.getCategory().getCategoryName())
                .description(item.getDescription())
                .color(item.getColor())
                .brand(item.getBrand())
                .foundDate(item.getFoundDate())
                .location(locationMapper.toDto(item.getLocation()))
                .verificationStatus(item.getVerificationStatus().name())
                .photos(photoMapper.toPublicDtoList(item.getPhotos()))
                .build();
    }

    public FoundItemAdminDto toAdminDto(FoundItem item, Case linkedCase) {
        if (item == null) return null;
        return FoundItemAdminDto.builder()
                .foundItemId(item.getFoundItemId())
                .foundReportId(item.getFoundReport() != null ? item.getFoundReport().getFoundReportId() : null)
                .stationId(item.getStation().getStationId())
                .stationName(item.getStation().getStationName())
                .categoryId(item.getCategory().getCategoryId())
                .category(item.getCategory().getCategoryName())
                .description(item.getDescription())
                .brand(item.getBrand())
                .color(item.getColor())
                .privateIdentifyingDetails(item.getPrivateIdentifyingDetails())
                .foundDate(item.getFoundDate())
                .receivedDate(item.getReceivedDate())
                .location(locationMapper.toDto(item.getLocation()))
                .custodyStatus(item.getCustodyStatus().name())
                .verificationStatus(item.getVerificationStatus().name())
                .caseId(linkedCase != null ? linkedCase.getCaseId() : null)
                .caseNumber(linkedCase != null ? linkedCase.getCaseNumber() : null)
                .photos(photoMapper.toDtoList(item.getPhotos()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public FoundItemSearchResultDto toSearchResultDto(FoundItem item, Double distanceKm) {
        if (item == null) return null;
        String primaryPhoto = item.getPhotos().stream()
                .filter(p -> p.getVisibility().name().equals("PUBLIC"))
                .sorted(Comparator.comparing(ItemPhoto::getIsPrimary, Comparator.reverseOrder()))
                .map(ItemPhoto::getFileUrl)
                .findFirst()
                .orElse(null);
        return FoundItemSearchResultDto.builder()
                .foundItemId(item.getFoundItemId())
                .category(item.getCategory().getCategoryName())
                .description(item.getDescription())
                .color(item.getColor())
                .brand(item.getBrand())
                .foundDate(item.getFoundDate())
                .location(locationMapper.toDto(item.getLocation()))
                .distanceKm(distanceKm)
                .primaryPhotoUrl(primaryPhoto)
                .build();
    }
}
