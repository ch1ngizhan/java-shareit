package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class BookingServiceImplExceptionTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private UserDto anotherUser;
    private ItemDto availableItem;
    private ItemDto unavailableItem;
    private BookingDto bookingDto;

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

        anotherUser = userService.create(UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build());

        // Create items
        availableItem = itemService.create(ItemDto.builder()
                .name("Available Item")
                .description("Available Description")
                .available(true)
                .build(), owner.getId());

        unavailableItem = itemService.create(ItemDto.builder()
                .name("Unavailable Item")
                .description("Unavailable Description")
                .available(false)
                .build(), owner.getId());

        // Create booking DTO
        bookingDto = new BookingDto();
        bookingDto.setItemId(availableItem.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_shouldThrowNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.create(999L, bookingDto));
    }

    @Test
    void create_shouldThrowNotFoundExceptionForNonExistentItem() {
        // Given
        bookingDto.setItemId(999L);

        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowIllegalArgumentExceptionForUnavailableItem() {
        // Given
        bookingDto.setItemId(unavailableItem.getId());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowAccessDeniedExceptionForOwnItem() {
        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                bookingService.create(owner.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowIllegalArgumentExceptionForInvalidDates() {
        // Given - end before start
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowIllegalArgumentExceptionForEqualDates() {
        // Given - start equals end
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        bookingDto.setStart(sameTime);
        bookingDto.setEnd(sameTime);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void update_shouldThrowNotFoundExceptionForNonExistentBooking() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.update(owner.getId(), true, 999L));
    }

    @Test
    void update_shouldThrowAccessDeniedExceptionForNonOwner() {
        // Given
        var createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                bookingService.update(anotherUser.getId(), true, createdBooking.getId()));
    }

    @Test
    void update_shouldThrowIllegalArgumentExceptionForAlreadyApproved() {
        // Given
        var createdBooking = bookingService.create(booker.getId(), bookingDto);
        bookingService.update(owner.getId(), true, createdBooking.getId()); // Approve first

        // When & Then - Try to approve again
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.update(owner.getId(), true, createdBooking.getId()));
    }

    @Test
    void getBooking_shouldThrowNotFoundExceptionForNonExistentBooking() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.getBooking(owner.getId(), 999L));
    }

    @Test
    void getBooking_shouldThrowAccessDeniedExceptionForUnauthorizedUser() {
        // Given
        var createdBooking = bookingService.create(booker.getId(), bookingDto);

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                bookingService.getBooking(anotherUser.getId(), createdBooking.getId()));
    }

    @Test
    void getBookingsByUser_shouldThrowNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.getBookingsByUser(999L, "ALL"));
    }

    @Test
    void getBookingsByUser_shouldThrowIllegalArgumentExceptionForInvalidState() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getBookingsByUser(booker.getId(), "INVALID_STATE"));
    }

    @Test
    void getBookingsByOwner_shouldThrowNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                bookingService.getBookingsByOwner(999L, "ALL"));
    }

    @Test
    void getBookingsByOwner_shouldThrowIllegalArgumentExceptionForInvalidState() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getBookingsByOwner(owner.getId(), "INVALID_STATE"));
    }
}