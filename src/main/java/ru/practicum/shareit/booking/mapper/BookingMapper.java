package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    // Преобразуем Booking -> BookingDto
    public static BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBooker(booking.getBooker()); // весь объект User
        bookingDto.setItem(booking.getItem());     // весь объект Item
        bookingDto.setStatus(booking.getStatus());
        return bookingDto;
    }

    // Преобразуем BookingDto -> Booking
    public static Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        return booking;
    }
}
