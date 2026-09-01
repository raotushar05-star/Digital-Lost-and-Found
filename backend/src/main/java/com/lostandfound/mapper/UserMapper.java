package com.lostandfound.mapper;

import com.lostandfound.dto.auth.UserSummaryDto;
import com.lostandfound.dto.user.UserProfileDto;
import com.lostandfound.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserSummaryDto toSummaryDto(User user) {
        if (user == null) return null;
        return UserSummaryDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public UserProfileDto toProfileDto(User user) {
        if (user == null) return null;
        return UserProfileDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole().name())
                .stationId(user.getStation() != null ? user.getStation().getStationId() : null)
                .stationName(user.getStation() != null ? user.getStation().getStationName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
