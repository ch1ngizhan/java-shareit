package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {

    @Test
    void testNoArgsConstructor() {
        BookingDto bookingDto = new BookingDto();

        assertNotNull(bookingDto);
        assertNull(bookingDto.getId());
        assertNull(bookingDto.getStart());
        assertNull(bookingDto.getEnd());
        assertNull(bookingDto.getItemId());
        assertNull(bookingDto.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        BookingDto bookingDto = new BookingDto();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItemId(3L);
        bookingDto.setStatus(Status.WAITING);

        assertEquals(1L, bookingDto.getId());
        assertEquals(start, bookingDto.getStart());
        assertEquals(end, bookingDto.getEnd());
        assertEquals(3L, bookingDto.getItemId());
        assertEquals(Status.WAITING, bookingDto.getStatus());
    }

    @Test
    void testDataAnnotation() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItemId(3L);
        bookingDto.setStatus(Status.APPROVED);

        assertNotNull(bookingDto.toString());
        assertNotNull(bookingDto.hashCode());

        BookingDto sameBookingDto = new BookingDto();
        sameBookingDto.setId(1L);
        sameBookingDto.setStart(start);
        sameBookingDto.setEnd(end);
        sameBookingDto.setItemId(3L);
        sameBookingDto.setStatus(Status.APPROVED);

        assertEquals(bookingDto, sameBookingDto);
    }
}
