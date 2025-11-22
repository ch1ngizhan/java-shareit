package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemRequestServiceImplTest {

    @InjectMocks
    private ItemRequestServiceImpl service;

    @Mock
    private ItemRequestStorage requestStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private ItemStorage itemStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldReturnSavedRequest() {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        User user = new User();
        user.setId(userId);

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(100L);
        savedRequest.setDescription(requestDto.getDescription());
        savedRequest.setRequester(user);
        savedRequest.setCreated(LocalDateTime.now());

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(requestStorage.save(any(ItemRequest.class))).thenReturn(savedRequest);

        ItemRequestDto result = service.create(userId, requestDto);

        assertNotNull(result);
        assertEquals(savedRequest.getId(), result.getId());
        assertEquals(savedRequest.getDescription(), result.getDescription());
        verify(requestStorage, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void create_ShouldThrowNotFound_WhenUserNotExist() {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();

        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.create(userId, requestDto));
        assertTrue(exception.getMessage().contains("Пользователь"));
    }

    @Test
    void getAllByRequester_ShouldReturnRequests() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setRequester(user);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(requestStorage.findByRequesterIdOrderByCreatedDesc(userId)).thenReturn(List.of(request));

        List<ItemRequestDto> result = service.getAllByRequester(userId);

        assertEquals(1, result.size());
        assertEquals(request.getId(), result.get(0).getId());
    }

    @Test
    void getAll_ShouldReturnPagedRequests() {
        Long userId = 1L;
        int from = 0;
        int size = 2;

        User user = new User();
        user.setId(userId);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);

        Page<ItemRequest> page = new PageImpl<>(List.of(request1, request2));

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(requestStorage.findAllByRequesterIdNot(eq(userId), any(Pageable.class))).thenReturn(page);

        List<ItemRequestDto> result = service.getAll(userId, from, size);

        assertEquals(2, result.size());
        assertEquals(request1.getId(), result.get(0).getId());
        assertEquals(request2.getId(), result.get(1).getId());
    }

    @Test
    void getById_ShouldReturnRequestWithItems() {
        Long userId = 1L;
        Long requestId = 10L;

        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setRequester(user);

        Item item = new Item();
        item.setId(100L);
        item.setRequest(request);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(requestStorage.findById(requestId)).thenReturn(Optional.of(request));
        when(itemStorage.findByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestDto result = service.getById(userId, requestId);

        assertEquals(requestId, result.getId());
        assertEquals(1, result.getItems().size());
        assertEquals(item.getId(), result.getItems().get(0).getId());
    }

    @Test
    void getById_ShouldThrowNotFound_WhenRequestNotExist() {
        Long userId = 1L;
        Long requestId = 10L;

        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(requestStorage.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(userId, requestId));
    }
}
