package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestStorage itemRequestStorage;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setName("John");

        item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("Electric drill");
        item.setAvailable(true);
        item.setOwner(user);

        itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Electric drill");
        itemDto.setAvailable(true);
    }

    @Test
    void createItem_success() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(itemStorage.save(any(Item.class))).thenAnswer(invocation -> {
            Item i = invocation.getArgument(0);
            i.setId(10L); // имитация базы
            return i;
        });

        ItemDto created = itemService.create(itemDto, 1L);

        assertNotNull(created);
        assertEquals(10L, created.getId());
        assertEquals("Drill", created.getName());
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_userNotFound_throwsException() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.create(itemDto, 1L));
    }

    @Test
    void deleteItem_success() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(itemStorage.findById(10L)).thenReturn(Optional.of(item));

        itemService.delete(10L, 1L);

        verify(itemStorage, times(1)).delete(item);
    }

    @Test
    void deleteItem_notOwner_throwsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(userStorage.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemStorage.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class, () -> itemService.delete(10L, 2L));
    }


    @Test
    void search_emptyText_returnsEmptyList() {
        assertTrue(itemService.search(" ").isEmpty());
    }

    @Test
    void createComment_noBooking_throwsException() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(itemStorage.findById(10L)).thenReturn(Optional.of(item));
        when(bookingStorage.findFirstByItemIdAndBookerIdAndEndBeforeAndStatusOrderByEndDesc(
                eq(10L), eq(1L), any(LocalDateTime.class), eq(Status.APPROVED)
        )).thenReturn(Optional.empty());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Good");

        assertThrows(ValidationException.class, () -> itemService.createComment(1L, 10L, commentDto));
    }

    @Test
    void createItem_WithNullAvailable_ShouldSetDefaultValue() {
        // Подготовка
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test");
        itemDto.setDescription("Test");
        itemDto.setAvailable(null); // null значение

        User user = new User();
        user.setId(1L);

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(itemStorage.save(any(Item.class))).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            savedItem.setId(1L);
            return savedItem;
        });

        // Выполнение
        ItemDto result = itemService.create(itemDto, 1L);

        // Проверка - должно установиться значение по умолчанию
        assertNotNull(result.getAvailable());
    }

    @Test
    void getAllItems_WithMultipleItems_ShouldGroupBookingsAndComments() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setOwner(user);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setOwner(user);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findByOwnerIdOrderByIdDesc(userId)).thenReturn(List.of(item1, item2));
        when(commentRepository.findAllByItemIdIn(any())).thenReturn(List.of());
        when(bookingStorage.findByItemIdInAndEndBeforeAndStatusOrderByEndDesc(any(), any(), any())).thenReturn(List.of());
        when(bookingStorage.findByItemIdInAndStartAfterAndStatusOrderByStartAsc(any(), any(), any())).thenReturn(List.of());

        // Выполнение
        var result = itemService.getAllItems(userId);

        // Проверка
        assertEquals(2, result.size());
    }

    @Test
    void createComment_WithValidData_ShouldSaveComment() {
        // Подготовка
        Long userId = 1L;
        Long itemId = 1L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        Item item = new Item();
        item.setId(itemId);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Status.APPROVED);
        booking.setEnd(LocalDateTime.now().minusDays(1));

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText("Great item!");
        savedComment.setAuthor(user);
        savedComment.setItem(item);
        savedComment.setCreated(LocalDateTime.now());

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingStorage.findFirstByItemIdAndBookerIdAndEndBeforeAndStatusOrderByEndDesc(
                eq(itemId), eq(userId), any(LocalDateTime.class), eq(Status.APPROVED)))
                .thenReturn(Optional.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Выполнение
        CommentDto result = itemService.createComment(userId, itemId, commentDto);

        // Проверка
        assertNotNull(result);
        assertEquals("Great item!", result.getText());
    }

    @Test
    void createItem_nullDto_throwsException() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(NotFoundException.class, () -> itemService.create(null, 1L));
    }

    @Test
    void createItem_withRequestId_shouldAttachRequest() {
        ItemDto dto = new ItemDto();
        dto.setName("Test");
        dto.setDescription("Test");
        dto.setAvailable(true);
        dto.setRequestId(5L);

        ItemRequest req = new ItemRequest();
        req.setId(5L);

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestStorage.findById(5L)).thenReturn(Optional.of(req));
        when(itemStorage.save(any())).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ItemDto result = itemService.create(dto, 1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void updateItem_success() {
        when(itemStorage.findById(10L)).thenReturn(Optional.of(item));
        when(itemStorage.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemDto update = new ItemDto();
        update.setName("NewName");
        update.setDescription("NewDesc");
        update.setAvailable(false);

        ItemDto result = itemService.update(1L, update, 10L);

        assertEquals("NewName", result.getName());
        assertEquals("NewDesc", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_notOwner_throwsException() {
        User other = new User();
        other.setId(2L);

        when(itemStorage.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class,
                () -> itemService.update(2L, itemDto, 10L));
    }

    @Test
    void updateItem_itemNotFound() {
        when(itemStorage.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> itemService.update(1L, itemDto, 10L));
    }
}
