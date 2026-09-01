package com.lostandfound.mapper;

import com.lostandfound.dto.match.MatchDto;
import com.lostandfound.entity.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {
    public MatchDto toDto(Match match) {
        if (match == null) return null;
        return MatchDto.builder()
                .matchId(match.getMatchId())
                .lostItemId(match.getLostItem().getLostItemId())
                .foundItemId(match.getFoundItem().getFoundItemId())
                .foundItemCategory(match.getFoundItem().getCategory().getCategoryName())
                .foundItemDescription(match.getFoundItem().getDescription())
                .foundItemDate(match.getFoundItem().getFoundDate())
                .foundItemCity(match.getFoundItem().getLocation().getCity())
                .matchScore(match.getMatchScore())
                .matchReason(match.getMatchReason())
                .status(match.getStatus().name())
                .createdAt(match.getCreatedAt())
                .build();
    }
}
