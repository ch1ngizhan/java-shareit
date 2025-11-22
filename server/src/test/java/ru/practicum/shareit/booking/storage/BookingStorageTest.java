package ru.practicum.shareit.booking.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingStorageTest {

    @Mock
    private BookingStorage bookingStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByBookerIdOrderByStartDesc() {
        Long bookerId = 1L;
        when(bookingStorage.findByBookerIdOrderByStartDesc(bookerId)).thenReturn(List.of(new Booking()));
        List<Booking> bookings = bookingStorage.findByBookerIdOrderByStartDesc(bookerId);
        assertFalse(bookings.isEmpty());
        verify(bookingStorage).findByBookerIdOrderByStartDesc(bookerId);
    }

    @Test
    void testExistsApprovedBooking() {
        when(bookingStorage.existsApprovedBooking(1L, 2L, Status.APPROVED)).thenReturn(true);
        boolean exists = bookingStorage.existsApprovedBooking(1L, 2L, Status.APPROVED);
        assertTrue(exists);
        verify(bookingStorage).existsApprovedBooking(1L, 2L, Status.APPROVED);
    }

    @Test
    void testFindFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc() {
        LocalDateTime now = LocalDateTime.now();
        when(bookingStorage.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(1L, now, Status.APPROVED))
                .thenReturn(Optional.of(new Booking()));
        Optional<Booking> booking = bookingStorage.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(1L, now, Status.APPROVED);
        assertTrue(booking.isPresent());
        verify(bookingStorage).findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(1L, now, Status.APPROVED);
    }
}