package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void toBookingDto_shouldConvertBookingToDto() {
        // Given
        User booker = new User();
        booker.setId(1L);

        Item item = new Item();
        item.setId(2L);

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        // When
        BookingDto result = BookingMapper.toBookingDto(booking);

        // Then
        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getBooker().getId(), result.getBooker());
        assertEquals(booking.getItem().getId(), result.getItemId());
        assertEquals(booking.getStatus(), result.getStatus());
    }

    @Test
    void toBooking_shouldConvertDtoToBooking() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Item item = new Item();
        item.setId(2L);

        User booker = new User();
        booker.setId(3L);

        // When
        Booking result = BookingMapper.toBooking(bookingDto, item, booker);

        // Then
        assertNotNull(result);
        assertEquals(bookingDto.getId(), result.getId());
        assertEquals(bookingDto.getStart(), result.getStart());
        assertEquals(bookingDto.getEnd(), result.getEnd());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
        assertEquals(Status.WAITING, result.getStatus());
    }

    @Test
    void toBookingOut_shouldConvertBookingToOut() {
        // Given
        User booker = new User();
        booker.setId(1L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        Item item = new Item();
        item.setId(2L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        // When
        BookingOut result = BookingMapper.toBookingOut(booking);

        // Then
        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getStatus(), result.getStatus());
        assertNotNull(result.getBooker());
        assertNotNull(result.getItem());
    }

    @Test
    void toBookingOut_shouldReturnNullForNullInput() {
        // When
        BookingOut result = BookingMapper.toBookingOut(null);

        // Then
        assertNull(result);
    }
}