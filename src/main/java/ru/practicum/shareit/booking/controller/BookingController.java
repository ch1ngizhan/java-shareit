package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingOut create(@RequestHeader(USER_ID_HEADER) Long bookerId,
                             @Valid @RequestBody BookingDto bookingDto) {
        log.info("POST /bookings - Создание бронирования: bookerId: {}, bookingDto: {}", bookerId, bookingDto);
        log.debug("Детали бронирования - start: {}, end: {}, itemId: {}",
                bookingDto.getStart(), bookingDto.getEnd(), bookingDto.getItemId());
        BookingOut result = bookingService.create(bookerId, bookingDto);
        log.info("Бронирование успешно создано: {}", result);
        return result;
    }

    @PatchMapping("/{bookingId}")
    public BookingOut update(@PathVariable Long bookingId,
                             @RequestParam(name = "approved", required = true) boolean approved,
                             @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("PATCH /bookings/{} - Обновление статуса бронирования: ownerId: {}, approved: {}",
                bookingId, ownerId, approved);
        BookingOut result = bookingService.update(ownerId, approved, bookingId);
        log.info("Статус бронирования обновлен: {}", result);
        return result;
    }

    @GetMapping("/{bookingId}")
    public BookingOut getBooking(@PathVariable Long bookingId,
                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /bookings/{} - Получение бронирования пользователем: userId: {}", bookingId, userId);
        BookingOut result = bookingService.getBooking(userId, bookingId);
        log.info("Найдено бронирование: {}", result);
        return result;
    }

    @GetMapping("/owner")
    public List<BookingOut> getBookingsByOwner(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings/owner - Получение бронирований владельца: userId: {}, state: {}", userId, state);
        List<BookingOut> result = bookingService.getBookingsByOwner(userId, state);
        log.info("Найдено {} бронирований для владельца", result.size());
        log.debug("Список бронирований владельца: {}", result);
        return result;
    }

    @GetMapping
    public List<BookingOut> getBookingsByUser(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings - Получение бронирований пользователя: userId: {}, state: {}", userId, state);
        List<BookingOut> result = bookingService.getBookingsByUser(userId, state);
        log.info("Найдено {} бронирований для пользователя", result.size());
        log.debug("Список бронирований пользователя: {}", result);
        return result;
    }
}