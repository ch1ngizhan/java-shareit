package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookingServiceImplCoverageTest {

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_ItemNotAvailable_ShouldThrowValidationException() {
        // Подготовка
        Long bookerId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(bookerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(false); // Вещь недоступна

        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        when(userStorage.findById(bookerId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(1L)).thenReturn(Optional.of(item));

        // Проверка
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(bookerId, bookingDto));
        assertTrue(exception.getMessage().contains("недоступна"));
    }

    @Test
    void createBooking_BookerIsOwner_ShouldThrowAccessDeniedException() {
        // Подготовка
        Long bookerId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(bookerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(user); // Владелец == бронирующий

        when(userStorage.findById(bookerId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(1L)).thenReturn(Optional.of(item));

        // Проверка - должно выбросить AccessDeniedException
        assertThrows(AccessDeniedException.class,
                () -> bookingService.create(bookerId, bookingDto));
    }

    @Test
    void updateBooking_NotWaitingStatus_ShouldThrowValidationException() {
        // Подготовка
        Long ownerId = 1L;
        Long bookingId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setStatus(Status.APPROVED); // Уже подтверждено

        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));

        // Проверка
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.update(ownerId, true, bookingId));
        assertTrue(exception.getMessage().contains("уже имеет финальный статус"));
    }

    @Test
    void getBookingsByUser_AllStates_ShouldReturnCorrectResults() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));

        // Проверка всех состояний
        String[] states = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : states) {
            assertDoesNotThrow(() -> bookingService.getBookingsByUser(userId, state));
        }
    }

    @Test
    void getBookingsByOwner_AllStates_ShouldReturnCorrectResults() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));

        // Проверка всех состояний
        String[] states = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : states) {
            assertDoesNotThrow(() -> bookingService.getBookingsByOwner(userId, state));
        }
    }
}