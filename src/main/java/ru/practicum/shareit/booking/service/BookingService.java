package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingOut;

import java.util.List;

public interface BookingService {
    BookingOut create(Long bookerId, BookingDto bookingDto);

    BookingOut update(Long bookerId, Boolean approved, Long bookingId);

    BookingOut getBooking(Long userId, Long bookingId);

    List<BookingOut> getBookingsByUser(Long userId, String state);

    List<BookingOut> getBookingsByOwner(Long userId, String state);
}
