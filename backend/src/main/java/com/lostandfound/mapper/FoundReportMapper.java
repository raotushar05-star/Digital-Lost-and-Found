package com.lostandfound.mapper;

import com.lostandfound.dto.file.PhotoDto;
import com.lostandfound.dto.foundreport.FoundReportDto;
import com.lostandfound.entity.FoundReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FoundReportMapper {

    private final LocationMapper locationMapper;

    public FoundReportDto toDto(FoundReport report, java.util.UUID linkedFoundItemId, List<PhotoDto> photos) {
        if (report == null) return null;
        return FoundReportDto.builder()
                .foundReportId(report.getFoundReportId())
                .finderId(report.getFinder().getUserId())
                .category(report.getCategory().getCategoryName())
                .description(report.getDescription())
                .brand(report.getBrand())
                .color(report.getColor())
                .foundDate(report.getFoundDate())
                .location(locationMapper.toDto(report.getLocation()))
                .status(report.getStatus().name())
                .linkedFoundItemId(linkedFoundItemId)
                .photos(photos)
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}