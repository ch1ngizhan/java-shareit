package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class BookingServiceImplStateTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;
    private BookingDto currentBookingDto;
    private BookingDto pastBookingDto;
    private BookingDto futureBookingDto;

    @BeforeEach
    void setUp() {
        // Create users
        owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = userService.create(UserDto.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        // Create item
        item = itemService.create(ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build(), owner.getId());

        // Create bookings in different time periods
        currentBookingDto = new BookingDto();
        currentBookingDto.setItemId(item.getId());
        currentBookingDto.setStart(LocalDateTime.now().minusDays(1));
        currentBookingDto.setEnd(LocalDateTime.now().plusDays(1));

        pastBookingDto = new BookingDto();
        pastBookingDto.setItemId(item.getId());
        pastBookingDto.setStart(LocalDateTime.now().minusDays(3));
        pastBookingDto.setEnd(LocalDateTime.now().minusDays(2));

        futureBookingDto = new BookingDto();
        futureBookingDto.setItemId(item.getId());
        futureBookingDto.setStart(LocalDateTime.now().plusDays(2));
        futureBookingDto.setEnd(LocalDateTime.now().plusDays(3));
    }

    @Test
    void getBookingsByUser_shouldReturnCurrentBookings() {
        // Given
        bookingService.create(booker.getId(), currentBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByUser(booker.getId(), "CURRENT");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void getBookingsByUser_shouldReturnPastBookings() {
        // Given
        bookingService.create(booker.getId(), pastBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByUser(booker.getId(), "PAST");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void getBookingsByUser_shouldReturnFutureBookings() {
        // Given
        bookingService.create(booker.getId(), futureBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByUser(booker.getId(), "FUTURE");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void getBookingsByUser_shouldReturnWaitingBookings() {
        // Given
        bookingService.create(booker.getId(), futureBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByUser(booker.getId(), "WAITING");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(Status.WAITING, results.get(0).getStatus());
    }

    @Test
    void getBookingsByUser_shouldReturnRejectedBookings() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), futureBookingDto);
        bookingService.update(owner.getId(), false, createdBooking.getId());

        // When
        List<BookingOut> results = bookingService.getBookingsByUser(booker.getId(), "REJECTED");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(Status.REJECTED, results.get(0).getStatus());
    }

    @Test
    void getBookingsByOwner_shouldReturnCurrentBookings() {
        // Given
        bookingService.create(booker.getId(), currentBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByOwner(owner.getId(), "CURRENT");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void getBookingsByOwner_shouldReturnPastBookings() {
        // Given
        bookingService.create(booker.getId(), pastBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByOwner(owner.getId(), "PAST");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void getBookingsByOwner_shouldReturnFutureBookings() {
        // Given
        bookingService.create(booker.getId(), futureBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByOwner(owner.getId(), "FUTURE");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void getBookingsByOwner_shouldReturnWaitingBookings() {
        // Given
        bookingService.create(booker.getId(), futureBookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByOwner(owner.getId(), "WAITING");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(Status.WAITING, results.get(0).getStatus());
    }

    @Test
    void getBookingsByOwner_shouldReturnRejectedBookings() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), futureBookingDto);
        bookingService.update(owner.getId(), false, createdBooking.getId());

        // When
        List<BookingOut> results = bookingService.getBookingsByOwner(owner.getId(), "REJECTED");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(Status.REJECTED, results.get(0).getStatus());
    }
}