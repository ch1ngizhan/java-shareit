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
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.*;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
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
}
