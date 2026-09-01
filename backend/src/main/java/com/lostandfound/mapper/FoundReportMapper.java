package com.lostandfound.mapper;

import com.lostandfound.dto.foundreport.FoundReportDto;
import com.lostandfound.entity.FoundReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FoundReportMapper {

    private final LocationMapper locationMapper;

    public FoundReportDto toDto(FoundReport report, java.util.UUID linkedFoundItemId) {
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
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
