package ru.practicum.shareit.user.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserStorageTest {

    @Mock
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExistsByEmail() {
        String email = "test@example.com";
        when(userStorage.existsByEmail(email)).thenReturn(true);

        boolean exists = userStorage.existsByEmail(email);

        assertTrue(exists);
        verify(userStorage).existsByEmail(email);
    }
}
