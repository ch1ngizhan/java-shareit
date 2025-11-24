package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookingServiceImplCompleteTest {

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_UserNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long bookerId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        when(userStorage.findById(bookerId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> bookingService.create(bookerId, bookingDto));
    }

    @Test
    void createBooking_ItemNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long bookerId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(bookerId);

        when(userStorage.findById(bookerId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(1L)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> bookingService.create(bookerId, bookingDto));
    }

    @Test
    void updateBooking_BookingNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long ownerId = 1L;
        Long bookingId = 1L;

        when(bookingStorage.findById(bookingId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> bookingService.update(ownerId, true, bookingId));
    }

    @Test
    void getBooking_BookingNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;
        Long bookingId = 1L;

        when(bookingStorage.findById(bookingId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> bookingService.getBooking(userId, bookingId));
    }

    @Test
    void getBookingsByUser_UserNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;

        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByUser(userId, "ALL"));
    }

    @Test
    void getBookingsByOwner_UserNotFound_ShouldThrowNotFoundException() {
        // Подготовка
        Long userId = 1L;

        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByOwner(userId, "ALL"));
    }

    @Test
    void getBookingsByUser_AllStates_ShouldWork() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        User owner = new User();
        owner.setId(2L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStatus(Status.WAITING);
        booking.setBooker(user);
        booking.setItem(item);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(bookingStorage.findByBookerIdOrderByStartDesc(userId)).thenReturn(List.of(booking));
        when(bookingStorage.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));
        when(bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingStorage.findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingStorage.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));


        assertDoesNotThrow(() -> bookingService.getBookingsByUser(userId, "ALL"));
        assertDoesNotThrow(() -> bookingService.getBookingsByUser(userId, "CURRENT"));
        assertDoesNotThrow(() -> bookingService.getBookingsByUser(userId, "PAST"));
        assertDoesNotThrow(() -> bookingService.getBookingsByUser(userId, "FUTURE"));
        assertDoesNotThrow(() -> bookingService.getBookingsByUser(userId, "WAITING"));
        assertDoesNotThrow(() -> bookingService.getBookingsByUser(userId, "REJECTED"));
    }

    @Test
    void getBookingsByOwner_AllStates_ShouldWork() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        User booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStatus(Status.WAITING);
        booking.setBooker(booker);
        booking.setItem(item);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(bookingStorage.findAllByItemOwnerIdOrderByStartDesc(userId)).thenReturn(List.of(booking));
        when(bookingStorage.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(List.of(booking));
        when(bookingStorage.findByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingStorage.findByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));


        assertDoesNotThrow(() -> bookingService.getBookingsByOwner(userId, "ALL"));
        assertDoesNotThrow(() -> bookingService.getBookingsByOwner(userId, "CURRENT"));
        assertDoesNotThrow(() -> bookingService.getBookingsByOwner(userId, "PAST"));
        assertDoesNotThrow(() -> bookingService.getBookingsByOwner(userId, "FUTURE"));
        assertDoesNotThrow(() -> bookingService.getBookingsByOwner(userId, "WAITING"));
        assertDoesNotThrow(() -> bookingService.getBookingsByOwner(userId, "REJECTED"));
    }
}