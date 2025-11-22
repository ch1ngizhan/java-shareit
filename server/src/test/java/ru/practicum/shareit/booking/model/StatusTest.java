package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StatusTest {

    @Test
    void statusValues_shouldContainAllExpectedValues() {
        // When & Then
        assertNotNull(Status.WAITING);
        assertNotNull(Status.APPROVED);
        assertNotNull(Status.REJECTED);
        assertNotNull(Status.CANCELED);

        assertEquals(4, Status.values().length);
    }

    @Test
    void statusNames_shouldMatchExpected() {
        // When & Then
        assertEquals("WAITING", Status.WAITING.name());
        assertEquals("APPROVED", Status.APPROVED.name());
        assertEquals("REJECTED", Status.REJECTED.name());
        assertEquals("CANCELED", Status.CANCELED.name());
    }
}