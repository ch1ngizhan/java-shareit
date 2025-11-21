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
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
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
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        // Create owner
        owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        // Create booker
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

        // Create booking DTO
        bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_shouldCreateBookingSuccessfully() {
        // When
        BookingOut result = bookingService.create(booker.getId(), bookingDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(Status.WAITING, result.getStatus());
    }

    @Test
    void create_shouldThrowExceptionWhenItemNotAvailable() {
        // Given
        ItemDto unavailableItem = itemService.create(ItemDto.builder()
                .name("Unavailable Item")
                .description("Unavailable Description")
                .available(false)
                .build(), owner.getId());

        bookingDto.setItemId(unavailableItem.getId());

        // When & Then
        assertThrows(ValidationException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowExceptionWhenBookingOwnItem() {
        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                bookingService.create(owner.getId(), bookingDto));
    }

    @Test
    void update_shouldApproveBooking() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When
        BookingOut result = bookingService.update(owner.getId(), true, createdBooking.getId());

        // Then
        assertNotNull(result);
        assertEquals(Status.APPROVED, result.getStatus());
    }

    @Test
    void update_shouldRejectBooking() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When
        BookingOut result = bookingService.update(owner.getId(), false, createdBooking.getId());

        // Then
        assertNotNull(result);
        assertEquals(Status.REJECTED, result.getStatus());
    }

    @Test
    void update_shouldThrowExceptionWhenNotOwner() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                bookingService.update(booker.getId(), true, createdBooking.getId()));
    }

    @Test
    void getBooking_shouldReturnBookingForOwner() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When
        BookingOut result = bookingService.getBooking(owner.getId(), createdBooking.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
    }

    @Test
    void getBooking_shouldReturnBookingForBooker() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When
        BookingOut result = bookingService.getBooking(booker.getId(), createdBooking.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
    }

    @Test
    void getBooking_shouldThrowExceptionWhenNotOwnerOrBooker() {
        // Given
        UserDto anotherUser = userService.create(UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build());

        BookingOut createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                bookingService.getBooking(anotherUser.getId(), createdBooking.getId()));
    }

    @Test
    void getBookingsByUser_shouldReturnUserBookings() {
        // Given
        bookingService.create(booker.getId(), bookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByUser(booker.getId(), "ALL");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void getBookingsByOwner_shouldReturnOwnerBookings() {
        // Given
        bookingService.create(booker.getId(), bookingDto);

        // When
        List<BookingOut> results = bookingService.getBookingsByOwner(owner.getId(), "ALL");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }
}