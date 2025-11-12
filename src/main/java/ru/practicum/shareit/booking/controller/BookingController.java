package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.BookingDto;
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
        log.info("POST /bookings - bookerId: {}, bookingDto: {}", bookerId, bookingDto);
        log.debug("Booking details - start: {}, end: {}, itemid: {}",
                bookingDto.getStart(), bookingDto.getEnd(), bookingDto.getItemId());
        return bookingService.create(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingOut update(@PathVariable Long bookingId,
                             @RequestParam(name = "approved", required = true) boolean approved,
                             @RequestHeader(USER_ID_HEADER) Long ownerId) {
        return bookingService.update(ownerId, approved, bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingOut getBooking(@PathVariable Long bookingId,
                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping("/owner")
    public List<BookingOut> getBookingsByOwner(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByOwner(userId, state);

    }

    @GetMapping
    public List<BookingOut> getBookingsByUser(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByUser(userId, state);
    }
}
