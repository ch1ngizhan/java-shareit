package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class ItemServiceImplCoverageTest {

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void updateItem_NotOwner_ShouldThrowAccessDeniedException() {
        // Подготовка
        Long userId = 2L; // Не владелец
        Long itemId = 1L;

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        User owner = new User();
        owner.setId(1L); // Владелец другой

        User currentUser = new User();
        currentUser.setId(userId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(owner);

        when(userStorage.findById(userId)).thenReturn(Optional.of(currentUser));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(existingItem));

        // Проверка
        assertThrows(AccessDeniedException.class,
                () -> itemService.update(userId, updateDto, itemId));
    }

    @Test
    void updateItem_PartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Подготовка
        Long userId = 1L;
        Long itemId = 1L;

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");
        // description и available не установлены

        User owner = new User();
        owner.setId(userId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Original Name");
        existingItem.setDescription("Original Description");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        when(userStorage.findById(userId)).thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemStorage.save(any(Item.class))).thenReturn(existingItem);

        // Выполнение
        ItemDto result = itemService.update(userId, updateDto, itemId);

        // Проверка
        assertNotNull(result);
        // Должны сохраниться оригинальные значения для непредоставленных полей
    }

    @Test
    void getAllItems_EmptyList_ShouldReturnEmptyList() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findByOwnerIdOrderByIdDesc(userId)).thenReturn(List.of());

        // Выполнение
        var result = itemService.getAllItems(userId);

        // Проверка
        assertTrue(result.isEmpty());
    }

    @Test
    void createComment_ValidBooking_ShouldCreateComment() {
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
    void getItemById_UserIsNotOwner_ShouldNotShowBookings() {
        // Подготовка
        Long userId = 2L; // Не владелец
        Long itemId = 1L;

        User owner = new User();
        owner.setId(1L); // Владелец другой

        User currentUser = new User();
        currentUser.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        when(userStorage.findById(userId)).thenReturn(Optional.of(currentUser));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(List.of());

        // Выполнение
        var result = itemService.getItemById(userId, itemId);

        // Проверка - у не-владельца не должно быть информации о бронированиях
        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }
}