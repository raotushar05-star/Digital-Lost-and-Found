package com.lostandfound.service;

import com.lostandfound.dto.duplicate.DuplicateCheckRequest;
import com.lostandfound.dto.duplicate.DuplicateHitDto;
import com.lostandfound.entity.Location;
import com.lostandfound.entity.enums.FoundReportStatus;
import com.lostandfound.entity.enums.LostItemStatus;
import com.lostandfound.repository.FoundReportRepository;
import com.lostandfound.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DuplicateCheckService {

    private static final Set<LostItemStatus> CLOSED_LOST =
            EnumSet.of(LostItemStatus.WITHDRAWN, LostItemStatus.RETURNED, LostItemStatus.RESOLVED);
    private static final Set<FoundReportStatus> CLOSED_FOUND =
            EnumSet.of(FoundReportStatus.RETURNED, FoundReportStatus.REJECTED, FoundReportStatus.DUPLICATE);

    private final LostItemRepository lostItemRepository;
    private final FoundReportRepository foundReportRepository;

    @Transactional(readOnly = true)
    public List<DuplicateHitDto> checkLost(UUID userId, DuplicateCheckRequest r) {
        return lostItemRepository.findByOwner_UserIdOrderByCreatedAtDesc(userId).stream()
                .limit(50)
                .filter(i -> !CLOSED_LOST.contains(i.getStatus()))
                .filter(i -> similar(r, i.getCategory().getCategoryId(), i.getLostDate(), i.getLocation(),
                        i.getBrand(), i.getDescription()))
                .map(i -> new DuplicateHitDto(i.getLostItemId(), "LOST", i.getCategory().getCategoryName(),
                        shorten(i.getDescription()), i.getLostDate(), i.getLocation().getCity(),
                        i.getStatus().name(), i.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DuplicateHitDto> checkFound(UUID userId, DuplicateCheckRequest r) {
        return foundReportRepository.findByFinder_UserIdOrderByCreatedAtDesc(userId).stream()
                .limit(50)
                .filter(f -> !CLOSED_FOUND.contains(f.getStatus()))
                .filter(f -> similar(r, f.getCategory().getCategoryId(), f.getFoundDate(), f.getLocation(),
                        f.getBrand(), f.getDescription()))
                .map(f -> new DuplicateHitDto(f.getFoundReportId(), "FOUND", f.getCategory().getCategoryName(),
                        shorten(f.getDescription()), f.getFoundDate(), f.getLocation().getCity(),
                        f.getStatus().name(), f.getCreatedAt()))
                .toList();
    }

    private boolean similar(DuplicateCheckRequest r, UUID categoryId, LocalDate date, Location loc,
                            String brand, String description) {
        if (!r.categoryId().equals(categoryId)) return false;
        if (Math.abs(ChronoUnit.DAYS.between(r.date(), date)) > 7) return false;
        if (loc != null && loc.getLatitude() != null && loc.getLongitude() != null) {
            double km = LocationService.distanceKm(r.latitude().doubleValue(), r.longitude().doubleValue(),
                    loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue());
            if (km > 2) return false;
        }
        boolean sameBrand = brand != null && !brand.isBlank() && r.brand() != null && !r.brand().isBlank()
                && brand.trim().equalsIgnoreCase(r.brand().trim());
        return sameBrand || overlap(r.description(), description) >= 0.4;
    }

    private double overlap(String a, String b) {
        if (a == null || b == null) return 0;
        Set<String> wa = words(a);
        Set<String> wb = words(b);
        if (wa.isEmpty() || wb.isEmpty()) return 0;
        Set<String> inter = new HashSet<>(wa);
        inter.retainAll(wb);
        Set<String> union = new HashSet<>(wa);
        union.addAll(wb);
        return (double) inter.size() / union.size();
    }

    private Set<String> words(String s) {
        Set<String> out = new HashSet<>(Arrays.asList(s.toLowerCase().split("\\W+")));
        out.removeIf(w -> w.length() < 3);
        return out;
    }

    private String shorten(String s) {
        return s == null ? "" : (s.length() > 80 ? s.substring(0, 80) + "…" : s);
    }
}