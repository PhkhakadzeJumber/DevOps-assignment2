package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.CategoryDto;
import ge.ticketebi.ticketebi_backend.domain.dto.EventRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.EventResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.CategoryEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.EventEntity;
import ge.ticketebi.ticketebi_backend.mappers.ReqResMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapperImpl implements ReqResMapper<EventEntity, EventRequest, EventResponse> {

    private final UserMapperImpl userMapper;
    private final LocationMapperImpl locationMapper;
    private final CategoryMapperImpl categoryMapper;

    @Override
    public EventEntity toEntity(EventRequest eventRequest) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle(eventRequest.getTitle());
        eventEntity.setDescription(eventRequest.getDescription());
        eventEntity.setDate(eventRequest.getDate());

        Set<CategoryEntity> categories = eventRequest.getCategories().stream()
                .map(categoryMapper::mapFrom)
                .collect(Collectors.toSet());
        eventEntity.setCategories(categories);

        eventEntity.setLocation(locationMapper.mapFrom(eventRequest.getLocation()));
        return eventEntity;
    }

    @Override
    public EventResponse toResponseDto(EventEntity eventEntity) {
        EventResponse eventResponse = new EventResponse();
        eventResponse.setId(eventEntity.getId());
        eventResponse.setTitle(eventEntity.getTitle());
        eventResponse.setDescription(eventEntity.getDescription());
        eventResponse.setCreatedAt(eventEntity.getCreatedAt());
        eventResponse.setDate(eventEntity.getDate());
        eventResponse.setOrganizer(userMapper.mapTo(eventEntity.getOrganizer()));

        Set<CategoryDto> categories = eventEntity.getCategories()
                .stream()
                .map(categoryMapper::mapTo)
                .collect(Collectors.toSet());
        eventResponse.setCategories(categories);

        eventResponse.setLocation(locationMapper.mapTo(eventEntity.getLocation()));
        eventResponse.setThumbnailUrl(eventEntity.getThumbnailUrl());
        return eventResponse;
    }
}
