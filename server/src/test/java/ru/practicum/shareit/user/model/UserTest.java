package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testNoArgsConstructor() {
        // When
        User user = new User();

        // Then
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getItems());
        assertNull(user.getBookings());
        assertNull(user.getComment());
    }

    @Test
    void testAllArgsConstructor() {
        // When
        User user = new User(1L, "Test User", "test@example.com", null, null, null);

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        User user = new User();

        // When
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        // Then
        assertEquals(1L, user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
    }
}