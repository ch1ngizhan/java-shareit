package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
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
class BookingServiceImplEdgeCasesTest {

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
        owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = userService.create(UserDto.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        item = itemService.create(ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build(), owner.getId());

        bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_shouldThrowExceptionWhenStartEqualsEnd() {
        // Given
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        bookingDto.setStart(sameTime);
        bookingDto.setEnd(sameTime);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowExceptionWhenEndBeforeStart() {
        // Given
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
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
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void getBookingsByUser_shouldReturnEmptyListForUserWithoutBookings() {
        // Given
        UserDto newUser = userService.create(UserDto.builder()
                .name("New User")
                .email("newuser@example.com")
                .build());

        // When
        List<BookingOut> results = bookingService.getBookingsByUser(newUser.getId(), "ALL");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getBookingsByOwner_shouldReturnEmptyListForOwnerWithoutBookings() {
        // Given
        UserDto newOwner = userService.create(UserDto.builder()
                .name("New Owner")
                .email("newowner@example.com")
                .build());

        // When
        List<BookingOut> results = bookingService.getBookingsByOwner(newOwner.getId(), "ALL");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getBookingsByUser_shouldThrowExceptionForInvalidState() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getBookingsByUser(booker.getId(), "INVALID_STATE"));
    }

    @Test
    void getBookingsByOwner_shouldThrowExceptionForInvalidState() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getBookingsByOwner(owner.getId(), "INVALID_STATE"));
    }

    @Test
    void getBooking_shouldThrowExceptionForNonExistentBooking() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.getBooking(owner.getId(), 999L));
    }

    @Test
    void update_shouldThrowExceptionForNonExistentBooking() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.update(owner.getId(), true, 999L));
    }

    @Test
    void update_shouldThrowExceptionWhenNotOwner() {
        // Given
        BookingOut createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                bookingService.update(booker.getId(), true, createdBooking.getId()));
    }
}