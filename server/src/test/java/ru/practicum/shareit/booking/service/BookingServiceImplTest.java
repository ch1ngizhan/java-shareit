package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setOwner(new User() {{
            setId(2L);
        }}); // владелец другой
        item.setAvailable(true);

        bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        booking = new Booking();
        booking.setId(100L);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(Status.WAITING);
    }


    @Test
    void createBooking_itemNotAvailable_throwsValidationException() {
        item.setAvailable(false);
        when(userStorage.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void createBooking_bookerIsOwner_throwsAccessDeniedException() {
        item.setOwner(user); // теперь booker == owner
        when(userStorage.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class, () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void updateBooking_approve_success() {
        item.setOwner(user); // делаем booker владельцем для update
        booking.setItem(item);
        when(bookingStorage.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingOut result = bookingService.update(user.getId(), true, booking.getId());

        assertEquals(Status.APPROVED, booking.getStatus());
        assertEquals(booking.getId(), result.getId());
        verify(bookingStorage, times(1)).save(booking);
    }

    @Test
    void updateBooking_notOwner_throwsAccessDeniedException() {
        when(bookingStorage.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(AccessDeniedException.class, () -> bookingService.update(user.getId(), true, booking.getId()));
    }

    @Test
    void getBooking_success() {
        booking.setItem(new Item() {{
            setOwner(user);
        }}); // user является владельцем
        when(bookingStorage.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingOut result = bookingService.getBooking(user.getId(), booking.getId());

        assertNotNull(result);
    }

    @Test
    void getBooking_notAuthorized_throwsAccessDeniedException() {
        when(bookingStorage.findById(booking.getId())).thenReturn(Optional.of(booking));
        User other = new User();
        other.setId(99L);

        assertThrows(AccessDeniedException.class, () -> bookingService.getBooking(other.getId(), booking.getId()));
    }

    @Test
    void getBookingsByUser_all_success() {
        when(userStorage.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingStorage.findByBookerIdOrderByStartDesc(user.getId())).thenReturn(List.of(booking));

        List<BookingOut> list = bookingService.getBookingsByUser(user.getId(), "ALL");

        assertEquals(1, list.size());
    }

    @Test
    void getBookingsByOwner_all_success() {
        item.setOwner(user);
        booking.setItem(item);
        when(userStorage.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingStorage.findAllByItemOwnerIdOrderByStartDesc(user.getId())).thenReturn(List.of(booking));

        List<BookingOut> list = bookingService.getBookingsByOwner(user.getId(), "ALL");

        assertEquals(1, list.size());
    }
}
