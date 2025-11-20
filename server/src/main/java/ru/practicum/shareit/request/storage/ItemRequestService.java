package ru.practicum.shareit.request.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestService {
    private final ItemRequestStorage itemRequestRepository;
    private final UserStorage userStorage;

    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto requestDto) {
        log.info("Создание запроса вещи пользователем ID: {}", userId);

        User requestor = getUserOrThrow(userId);

        ItemRequest itemRequest = ItemRequest.builder()
                .description(requestDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос вещи создан с ID: {}", savedRequest.getId());

        return convertToResponseDto(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        log.info("Получение запросов вещей пользователя ID: {}", userId);

        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов вещей (кроме пользователя ID: {}), from: {}, size: {}",
                userId, from, size);

        getUserOrThrow(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);

        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса вещи ID: {} пользователем ID: {}", requestId, userId);

        getUserOrThrow(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос вещи с id " + requestId + " не найден"));

        return convertToResponseDto(itemRequest);
    }

    private ItemRequestResponseDto convertToResponseDto(ItemRequest itemRequest) {
        List<ItemDto> items = itemRequest.getItems() != null ?
                itemRequest.getItems().stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return new ItemRequestResponseDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items
        );
    }

    private User getUserOrThrow(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}