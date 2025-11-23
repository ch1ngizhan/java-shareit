package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BookingIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void createBooking_ItemNotAvailable_ShouldThrowException() {
        // Подготовка
        UserDto owner = userService.create(UserDto.builder()
                .name("Владелец")
                .email("owner@example.com")
                .build());

        UserDto booker = userService.create(UserDto.builder()
                .name("Бронирующий")
                .email("booker@example.com")
                .build());

        ItemDto item = itemService.create(ItemDto.builder()
                .name("Тестовая вещь")
                .description("Тестовое описание")
                .available(false) // Недоступна для бронирования
                .build(), owner.getId());

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        // Проверка
        assertThrows(ValidationException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }
}