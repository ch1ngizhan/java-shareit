package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

@Slf4j
@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingDto bookingDto) {
        log.info("POST /bookings - Создание бронирования пользователем ID: {}", userId);


        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            return ResponseEntity.badRequest().body("Start date must be before end date");
        }

        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            return ResponseEntity.badRequest().body("Start and end dates cannot be equal");
        }

        return bookingClient.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH /bookings/{} - Обновление статуса бронирования: approved={}, userId={}",
                bookingId, approved, userId);
        return bookingClient.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /bookings/{} - Получение бронирования пользователем ID: {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings - Получение бронирований пользователя ID: {}, state: {}", userId, state);
        return bookingClient.getBookingsByUser(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("GET /bookings/owner - Получение бронирований владельца ID: {}, state: {}", userId, state);
        return bookingClient.getBookingsByOwner(userId, state);
    }
}