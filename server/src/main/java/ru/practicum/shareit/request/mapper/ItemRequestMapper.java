package ru.practicum.shareit.request.mapper;


import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        if (request == null) {
            return null;
        }

        List<ItemDto> items = request.getItems() != null ?
                request.getItems().stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList()) :
                List.of();

        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items
        );
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        ItemRequest request = new ItemRequest();
        request.setId(requestDto.getId());
        request.setDescription(requestDto.getDescription());
        request.setCreated(requestDto.getCreated());
        return request;
    }
}