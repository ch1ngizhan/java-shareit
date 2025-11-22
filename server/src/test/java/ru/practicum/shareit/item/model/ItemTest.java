package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testNoArgsConstructor() {
        // When
        Item item = new Item();

        // Then
        assertNotNull(item);
        assertNull(item.getId());
        assertNull(item.getName());
        assertNull(item.getDescription());
        assertFalse(item.getAvailable());
        assertNull(item.getOwner());
        assertNull(item.getRequest());
        assertNull(item.getBookings());
        assertNull(item.getComments());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        User owner = new User();
        owner.setId(1L);

        // When
        Item item = new Item(1L, "Test Item", "Test Description", true, owner, null, null, null);

        // Then
        assertNotNull(item);
        assertEquals(1L, item.getId());
        assertEquals("Test Item", item.getName());
        assertEquals("Test Description", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
    }

    @Test
    void setAvailable_shouldHandleNull() {
        // Given
        Item item = new Item();

        // When
        item.setAvailable(null);

        // Then
        assertFalse(item.getAvailable());
    }

    @Test
    void setAvailable_shouldHandleTrue() {
        // Given
        Item item = new Item();

        // When
        item.setAvailable(true);

        // Then
        assertTrue(item.getAvailable());
    }

    @Test
    void setAvailable_shouldHandleFalse() {
        // Given
        Item item = new Item();

        // When
        item.setAvailable(false);

        // Then
        assertFalse(item.getAvailable());
    }
}