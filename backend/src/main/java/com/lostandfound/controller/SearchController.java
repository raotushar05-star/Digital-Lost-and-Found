package com.lostandfound.controller;

import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.dto.founditem.FoundItemPublicDto;
import com.lostandfound.dto.founditem.FoundItemSearchResultDto;
import com.lostandfound.service.FoundItemService;
import com.lostandfound.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Public Search + Map/Location Module. Anonymous access is permitted (see
 * SecurityConfig); only VERIFIED, IN_CUSTODY found items are ever exposed here.
 */
@RestController
@RequestMapping("/api/v1/found-items")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final FoundItemService foundItemService;

    @GetMapping
    public PagedResponse<FoundItemSearchResultDto> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String cityFilter = city != null ? city : location;
        return searchService.search(categoryId, cityFilter, latitude, longitude, radius, dateFrom, dateTo, color, brand, keyword, page, size);
    }

    @GetMapping("/nearby")
    public List<FoundItemSearchResultDto> nearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radius
    ) {
        return searchService.nearby(latitude, longitude, radius);
    }

    @GetMapping("/{foundItemId}/public")
    public FoundItemPublicDto getPublicDetail(@PathVariable UUID foundItemId) {
        return foundItemService.getPublicDetail(foundItemId);
    }
}
