package com.lostandfound.service;

import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.*;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.repository.LostItemRepository;
import com.lostandfound.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Smart Matching Module: generates POTENTIAL matches only, using category,
 * description, date, location, color, and brand. Never auto-confirms ownership -
 * confirmation always requires a claim plus an explicit police verification decision.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final MatchRepository matchRepository;
    private final CaseService caseService;
    private final NotificationService notificationService;

    @Value("${app.matching.minimum-score:40.0}")
    private double minimumScore;

    @Transactional
    public int generateForLostItem(LostItem lostItem) {
        List<FoundItem> candidates = foundItemRepository.findByVerificationStatus(FoundItemVerificationStatus.VERIFIED)
                .stream()
                .filter(fi -> fi.getCustodyStatus() == CustodyStatus.IN_CUSTODY)
                .filter(fi -> fi.getCategory().getCategoryId().equals(lostItem.getCategory().getCategoryId()))
                .collect(Collectors.toList());

        int created = 0;
        for (FoundItem foundItem : candidates) {
            if (matchRepository.existsByLostItem_LostItemIdAndFoundItem_FoundItemId(lostItem.getLostItemId(), foundItem.getFoundItemId())) {
                continue;
            }
            ScoreResult result = score(lostItem, foundItem);
            if (result.score >= minimumScore) {
                persistMatch(lostItem, foundItem, result);
                created++;
            }
        }
        return created;
    }

    @Transactional
    public int generateForFoundItem(FoundItem foundItem) {
        List<LostItem> candidates = Arrays.stream(new LostItemStatus[]{LostItemStatus.REPORTED, LostItemStatus.POTENTIAL_MATCH})
                .flatMap(status -> lostItemRepository.findByStatus(status).stream())
                .filter(li -> li.getCategory().getCategoryId().equals(foundItem.getCategory().getCategoryId()))
                .collect(Collectors.toList());

        int created = 0;
        for (LostItem lostItem : candidates) {
            if (matchRepository.existsByLostItem_LostItemIdAndFoundItem_FoundItemId(lostItem.getLostItemId(), foundItem.getFoundItemId())) {
                continue;
            }
            ScoreResult result = score(lostItem, foundItem);
            if (result.score >= minimumScore) {
                persistMatch(lostItem, foundItem, result);
                created++;
            }
        }
        return created;
    }

    @Transactional
    public int generateGlobal() {
        int total = 0;
        for (LostItemStatus status : new LostItemStatus[]{LostItemStatus.REPORTED, LostItemStatus.POTENTIAL_MATCH}) {
            for (LostItem lostItem : lostItemRepository.findByStatus(status)) {
                total += generateForLostItem(lostItem);
            }
        }
        return total;
    }

    private void persistMatch(LostItem lostItem, FoundItem foundItem, ScoreResult result) {
        Match match = Match.builder()
                .lostItem(lostItem)
                .foundItem(foundItem)
                .matchScore(BigDecimal.valueOf(result.score).setScale(2, RoundingMode.HALF_UP))
                .matchReason(result.reason)
                .status(MatchStatus.GENERATED)
                .build();
        matchRepository.save(match);

        User owner = lostItem.getOwner();
        notificationService.notify(owner, NotificationType.MATCH_FOUND,
                "Possible match found for your lost item",
                "A found item matching your \"" + lostItem.getDescription() + "\" report may have been located. Review the potential match and submit a claim if it is yours.",
                null, match);
        match.setStatus(MatchStatus.NOTIFIED);
        matchRepository.save(match);

        if (lostItem.getStatus() == LostItemStatus.REPORTED) {
            lostItem.setStatus(LostItemStatus.POTENTIAL_MATCH);
            lostItemRepository.save(lostItem);
            caseService.findAndTransition(lostItem, CaseStatus.POTENTIAL_MATCH, owner, "Potential match identified by the system");
        }
    }

    private ScoreResult score(LostItem lostItem, FoundItem foundItem) {
        StringBuilder reason = new StringBuilder();
        double score = 25.0; // category already matched as a precondition
        reason.append("Category matches (" + lostItem.getCategory().getCategoryName() + ").");

        long daysBetween = Math.abs(ChronoUnit.DAYS.between(lostItem.getLostDate(), foundItem.getFoundDate()));
        double dateScore;
        if (daysBetween <= 2) {
            dateScore = 20;
            reason.append(" Dates are within 2 days.");
        } else if (daysBetween <= 7) {
            dateScore = 14;
            reason.append(" Dates are within a week.");
        } else if (daysBetween <= 30) {
            dateScore = 8;
            reason.append(" Dates are within a month.");
        } else {
            dateScore = 2;
        }
        score += dateScore;

        double distanceKm = LocationService.distanceKm(
                lostItem.getLocation().getLatitude().doubleValue(), lostItem.getLocation().getLongitude().doubleValue(),
                foundItem.getLocation().getLatitude().doubleValue(), foundItem.getLocation().getLongitude().doubleValue());
        double locationScore;
        if (distanceKm <= 1) {
            locationScore = 25;
            reason.append(" Locations are within 1km.");
        } else if (distanceKm <= 5) {
            locationScore = 18;
            reason.append(" Locations are within 5km.");
        } else if (distanceKm <= 15) {
            locationScore = 10;
            reason.append(" Locations are within 15km.");
        } else {
            locationScore = 2;
        }
        score += locationScore;

        if (equalsIgnoreCaseSafe(lostItem.getColor(), foundItem.getColor())) {
            score += 10;
            reason.append(" Color matches.");
        }
        if (equalsIgnoreCaseSafe(lostItem.getBrand(), foundItem.getBrand())) {
            score += 10;
            reason.append(" Brand matches.");
        }

        double keywordOverlap = keywordOverlapScore(lostItem.getDescription(), foundItem.getDescription());
        score += keywordOverlap * 10;
        if (keywordOverlap > 0.3) {
            reason.append(" Description text overlaps significantly.");
        }

        score = Math.min(score, 100.0);
        return new ScoreResult(score, reason.toString());
    }

    private boolean equalsIgnoreCaseSafe(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private double keywordOverlapScore(String a, String b) {
        if (a == null || b == null) return 0;
        Set<String> wordsA = new HashSet<>(Arrays.asList(a.toLowerCase().split("\\W+")));
        Set<String> wordsB = new HashSet<>(Arrays.asList(b.toLowerCase().split("\\W+")));
        wordsA.removeIf(w -> w.length() < 3);
        wordsB.removeIf(w -> w.length() < 3);
        if (wordsA.isEmpty() || wordsB.isEmpty()) return 0;
        Set<String> intersection = new HashSet<>(wordsA);
        intersection.retainAll(wordsB);
        Set<String> union = new HashSet<>(wordsA);
        union.addAll(wordsB);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private static class ScoreResult {
        final double score;
        final String reason;
        ScoreResult(double score, String reason) {
            this.score = score;
            this.reason = reason;
        }
    }
}
