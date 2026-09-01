package com.lostandfound.mapper;

import com.lostandfound.dto.file.PhotoDto;
import com.lostandfound.entity.ItemPhoto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PhotoMapper {

    public PhotoDto toDto(ItemPhoto photo) {
        if (photo == null) return null;
        return PhotoDto.builder()
                .photoId(photo.getPhotoId())
                .fileUrl(photo.getFileUrl())
                .isPrimary(photo.getIsPrimary())
                .visibility(photo.getVisibility().name())
                .createdAt(photo.getCreatedAt())
                .build();
    }

    public List<PhotoDto> toDtoList(List<ItemPhoto> photos) {
        if (photos == null) return List.of();
        return photos.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Filters out non-public photos for unauthorized/anonymous viewers. */
    public List<PhotoDto> toPublicDtoList(List<ItemPhoto> photos) {
        if (photos == null) return List.of();
        return photos.stream()
                .filter(p -> p.getVisibility().name().equals("PUBLIC"))
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
