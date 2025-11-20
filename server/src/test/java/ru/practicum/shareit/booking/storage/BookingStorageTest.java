package ru.practicum.shareit.booking.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class BookingStorageTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingStorage bookingStorage;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        entityManager.persist(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        entityManager.persist(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        entityManager.persist(item);

        booking = Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();
        entityManager.persist(booking);

        entityManager.flush();
    }

    @Test
    void findByBookerIdOrderByStartDesc_shouldReturnUserBookings() {
        // When
        List<Booking> results = bookingStorage.findByBookerIdOrderByStartDesc(booker.getId());

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(booking.getId(), results.get(0).getId());
    }

    @Test
    void findByBookerIdOrderByStartDesc_shouldReturnEmptyForNonExistentUser() {
        // When
        List<Booking> results = bookingStorage.findByBookerIdOrderByStartDesc(999L);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void findAllByItemOwnerIdOrderByStartDesc_shouldReturnOwnerBookings() {
        // When
        List<Booking> results = bookingStorage.findAllByItemOwnerIdOrderByStartDesc(owner.getId());

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(booking.getId(), results.get(0).getId());
    }

    @Test
    void findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc_shouldReturnLastBooking() {
        // When
        Optional<Booking> result = bookingStorage.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                item.getId(), LocalDateTime.now().plusDays(3), Status.WAITING);

        // Then
        assertTrue(result.isPresent());
        assertEquals(booking.getId(), result.get().getId());
    }

    @Test
    void existsApprovedBooking_shouldReturnTrueForExistingBooking() {
        // Given - Create an approved booking
        Booking approvedBooking = Booking.builder()
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .build();
        entityManager.persist(approvedBooking);
        entityManager.flush();

        // When
        boolean exists = bookingStorage.existsApprovedBooking(
                item.getId(), booker.getId(), Status.APPROVED);

        // Then
        assertTrue(exists);
    }

    @Test
    void existsApprovedBooking_shouldReturnFalseForNonExistentBooking() {
        // When
        boolean exists = bookingStorage.existsApprovedBooking(
                999L, 999L, Status.APPROVED);

        // Then
        assertFalse(exists);
    }
}