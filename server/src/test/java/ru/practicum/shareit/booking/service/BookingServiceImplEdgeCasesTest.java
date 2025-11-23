package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookingServiceImplEdgeCasesTest {

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_InvalidDates_ShouldThrowValidationException() {
        // Подготовка
        Long bookerId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(2)); // Начало позже конца
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        User user = new User();
        user.setId(bookerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);

        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        when(userStorage.findById(bookerId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(1L)).thenReturn(Optional.of(item));

        // Проверка
        assertThrows(ValidationException.class, () -> bookingService.create(bookerId, bookingDto));
    }

    @Test
    void createBooking_NullDates_ShouldThrowValidationException() {
        // Подготовка
        Long bookerId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(null); // null даты
        bookingDto.setEnd(null);

        User user = new User();
        user.setId(bookerId);

        when(userStorage.findById(bookerId)).thenReturn(Optional.of(user));

        // Проверка
        assertThrows(ValidationException.class, () -> bookingService.create(bookerId, bookingDto));
    }

    @Test
    void getBookingsByUser_InvalidState_ShouldThrowValidationException() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));

        // Проверка
        assertThrows(ValidationException.class, () ->
                bookingService.getBookingsByUser(userId, "INVALID_STATE"));
    }
}