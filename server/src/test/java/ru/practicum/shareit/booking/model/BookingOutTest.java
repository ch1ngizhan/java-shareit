package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.user.model.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingOutTest {

    @Test
    void getItemId_shouldReturnItemId() {
        // Given
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .build();

        BookingOut bookingOut = new BookingOut();
        bookingOut.setItem(item);

        // When
        Long itemId = bookingOut.getItemId();

        // Then
        assertEquals(1L, itemId);
    }

    @Test
    void getBookerId_shouldReturnBookerId() {
        // Given
        UserDto booker = UserDto.builder()
                .id(2L)
                .name("Booker")
                .build();

        BookingOut bookingOut = new BookingOut();
        bookingOut.setBooker(booker);

        // When
        Long bookerId = bookingOut.getBookerId();

        // Then
        assertEquals(2L, bookerId);
    }

    @Test
    void getItemId_shouldReturnNullWhenItemIsNull() {
        // Given
        BookingOut bookingOut = new BookingOut();
        bookingOut.setItem(null);

        // When
        Long itemId = bookingOut.getItemId();

        // Then
        assertNull(itemId);
    }

    @Test
    void getBookerId_shouldReturnNullWhenBookerIsNull() {
        // Given
        BookingOut bookingOut = new BookingOut();
        bookingOut.setBooker(null);

        // When
        Long bookerId = bookingOut.getBookerId();

        // Then
        assertNull(bookerId);
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        UserDto booker = UserDto.builder().id(1L).build();
        ItemDto item = ItemDto.builder().id(2L).build();

        // When
        BookingOut bookingOut = new BookingOut(1L, item, start, end, booker, Status.WAITING);

        // Then
        assertNotNull(bookingOut);
        assertEquals(1L, bookingOut.getId());
        assertEquals(item, bookingOut.getItem());
        assertEquals(start, bookingOut.getStart());
        assertEquals(end, bookingOut.getEnd());
        assertEquals(booker, bookingOut.getBooker());
        assertEquals(Status.WAITING, bookingOut.getStatus());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        BookingOut bookingOut = new BookingOut();

        // Then
        assertNotNull(bookingOut);
        assertNull(bookingOut.getId());
        assertNull(bookingOut.getItem());
        assertNull(bookingOut.getStart());
        assertNull(bookingOut.getEnd());
        assertNull(bookingOut.getBooker());
        assertNull(bookingOut.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        BookingOut bookingOut = new BookingOut();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        UserDto booker = UserDto.builder().id(1L).name("Booker").build();
        ItemDto item = ItemDto.builder().id(2L).name("Item").build();

        // When
        bookingOut.setId(1L);
        bookingOut.setItem(item);
        bookingOut.setStart(start);
        bookingOut.setEnd(end);
        bookingOut.setBooker(booker);
        bookingOut.setStatus(Status.APPROVED);

        // Then
        assertEquals(1L, bookingOut.getId());
        assertEquals(item, bookingOut.getItem());
        assertEquals(start, bookingOut.getStart());
        assertEquals(end, bookingOut.getEnd());
        assertEquals(booker, bookingOut.getBooker());
        assertEquals(Status.APPROVED, bookingOut.getStatus());
    }
}