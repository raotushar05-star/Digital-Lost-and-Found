package com.lostandfound.mapper;

import com.lostandfound.dto.lostitem.LostItemDetailDto;
import com.lostandfound.dto.lostitem.LostItemSummaryDto;
import com.lostandfound.entity.Case;
import com.lostandfound.entity.LostItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LostItemMapper {

    private final LocationMapper locationMapper;
    private final PhotoMapper photoMapper;

    public LostItemSummaryDto toSummaryDto(LostItem item, Case linkedCase) {
        if (item == null) return null;
        return LostItemSummaryDto.builder()
                .lostItemId(item.getLostItemId())
                .category(item.getCategory().getCategoryName())
                .description(item.getDescription())
                .color(item.getColor())
                .brand(item.getBrand())
                .lostDate(item.getLostDate())
                .city(item.getLocation().getCity())
                .status(item.getStatus().name())
                .caseId(linkedCase != null ? linkedCase.getCaseId() : null)
                .caseNumber(linkedCase != null ? linkedCase.getCaseNumber() : null)
                .createdAt(item.getCreatedAt())
                .build();
    }

    public LostItemDetailDto toDetailDto(LostItem item, Case linkedCase) {
        if (item == null) return null;
        return LostItemDetailDto.builder()
                .lostItemId(item.getLostItemId())
                .ownerId(item.getOwner().getUserId())
                .ownerName(item.getOwner().getName())
                .categoryId(item.getCategory().getCategoryId())
                .category(item.getCategory().getCategoryName())
                .description(item.getDescription())
                .brand(item.getBrand())
                .color(item.getColor())
                .identifyingDetails(item.getIdentifyingDetails())
                .lostDate(item.getLostDate())
                .location(locationMapper.toDto(item.getLocation()))
                .status(item.getStatus().name())
                .caseId(linkedCase != null ? linkedCase.getCaseId() : null)
                .caseNumber(linkedCase != null ? linkedCase.getCaseNumber() : null)
                .photos(photoMapper.toDtoList(item.getPhotos()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
