package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long bookerId, BookingDto bookingDto);

    BookingDto update(Long bookerId, Boolean approved, Long bookingId);

    BookingDto getBooking(Long userId, Long bookingId);

    List<BookingDto> getBookingsByUser(Long userId, String state);

    List<BookingDto> getBookingsByOwner(Long userId, String state);
}
