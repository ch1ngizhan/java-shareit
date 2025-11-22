package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void testBuilder() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User booker = new User();
        booker.setId(1L);

        Item item = new Item();
        item.setId(2L);

        // When
        Booking booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        // Then
        assertNotNull(booking);
        assertEquals(1L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        Booking booking = new Booking();

        // Then
        assertNotNull(booking);
        assertNull(booking.getId());
        assertNull(booking.getStart());
        assertNull(booking.getEnd());
        assertNull(booking.getItem());
        assertNull(booking.getBooker());
        assertNull(booking.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User booker = new User();
        booker.setId(1L);

        Item item = new Item();
        item.setId(2L);

        // When
        Booking booking = new Booking(1L, start, end, item, booker, Status.WAITING);

        // Then
        assertNotNull(booking);
        assertEquals(1L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Booking booking = new Booking();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User booker = new User();
        booker.setId(1L);

        Item item = new Item();
        item.setId(2L);

        // When
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.APPROVED);

        // Then
        assertEquals(1L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(Status.APPROVED, booking.getStatus());
    }
}