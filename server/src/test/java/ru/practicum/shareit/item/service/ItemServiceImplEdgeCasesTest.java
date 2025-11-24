package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
class ItemServiceImplEdgeCasesTest {

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
    void getItemById_ItemNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;
        Long itemId = 999L;

        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(itemId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> itemService.getItemById(userId, itemId));
    }

    @Test
    void createComment_UserNotBookedItem_ShouldThrowValidationException() {
        // Подготовка
        Long userId = 1L;
        Long itemId = 1L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная вещь!");

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingStorage.findFirstByItemIdAndBookerIdAndEndBeforeAndStatusOrderByEndDesc(
                anyLong(), anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(Optional.empty()); // Нет завершенного бронирования

        // Проверка
        assertThrows(ValidationException.class, () ->
                itemService.createComment(userId, itemId, commentDto));
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyList() {
        // Проверка
        var result = itemService.search("   ");
        assertTrue(result.isEmpty());
    }

}