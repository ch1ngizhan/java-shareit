package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.exception.NotFoundException;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestStorage requestStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto requestDto) {

        User requester = getUserOrThrow(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestStorage.save(request);

        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getAllByRequester(Long userId) {

        getUserOrThrow(userId);

        List<ItemRequest> requests = requestStorage.findByRequesterIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, Integer from, Integer size) {
        getUserOrThrow(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = requestStorage.findAllByRequesterIdNot(userId, pageable).getContent();

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }



    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);

        ItemRequest request = requestStorage.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<Item> items = itemStorage.findByRequestId(requestId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        dto.setItems(itemDtos);

        return dto;
    }
    private User getUserOrThrow(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID: {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найдена");
                });
    }
}