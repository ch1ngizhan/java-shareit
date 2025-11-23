package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class ItemRequestServiceImplCompleteTest {

    @Mock
    private ItemRequestStorage requestStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private ItemStorage itemStorage;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void createRequest_UserNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Test request");

        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> itemRequestService.create(userId, requestDto));
    }

    @Test
    void getAllByRequester_UserNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;

        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> itemRequestService.getAllByRequester(userId));
    }

    @Test
    void getAll_UserNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;

        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> itemRequestService.getAll(userId, 0, 10));
    }

    @Test
    void getById_UserNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;
        Long requestId = 1L;

        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> itemRequestService.getById(userId, requestId));
    }

    @Test
    void getById_RequestNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;
        Long requestId = 1L;
        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(requestStorage.findById(requestId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> itemRequestService.getById(userId, requestId));
    }

    @Test
    void getAll_ShouldReturnPagedResults() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test request");
        request.setRequester(user);

        Page<ItemRequest> page = new PageImpl<>(List.of(request));

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(requestStorage.findAllByRequesterIdNot(eq(userId), any(Pageable.class))).thenReturn(page);

        // Выполнение
        List<ItemRequestDto> result = itemRequestService.getAll(userId, 0, 10);

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test request", result.get(0).getDescription());
    }
}