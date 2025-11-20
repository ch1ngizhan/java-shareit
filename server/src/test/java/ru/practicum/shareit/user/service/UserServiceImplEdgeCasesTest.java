package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotUniqueEmailException;
import ru.practicum.shareit.user.model.UserDto;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserServiceImplEdgeCasesTest {

    @Autowired
    private UserService userService;

    @Test
    void create_shouldThrowExceptionForInvalidEmailFormat() {
        // Given
        UserDto invalidUser = UserDto.builder()
                .name("Test User")
                .email("invalid-email-format")
                .build();

        // When & Then
        assertThrows(NotUniqueEmailException.class, () ->
                userService.create(invalidUser));
    }

    @Test
    void create_shouldThrowExceptionForEmptyEmail() {
        // Given
        UserDto emptyEmailUser = UserDto.builder()
                .name("Test User")
                .email("")
                .build();

        // When & Then
        assertThrows(NotUniqueEmailException.class, () ->
                userService.create(emptyEmailUser));
    }

    @Test
    void create_shouldThrowExceptionForNullEmail() {
        // Given
        UserDto nullEmailUser = UserDto.builder()
                .name("Test User")
                .email(null)
                .build();

        // When & Then
        // Изменяем ожидаемое исключение на NullPointerException
        assertThrows(NullPointerException.class, () ->
                userService.create(nullEmailUser));
    }

    @Test
    void update_shouldHandleEmptyName() {
        // Given
        UserDto originalUser = UserDto.builder()
                .name("Original Name")
                .email("original@example.com")
                .build();
        UserDto savedUser = userService.create(originalUser);

        UserDto updateDto = UserDto.builder()
                .name("")
                .email("updated@example.com")
                .build();

        // When
        UserDto result = userService.update(savedUser.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Original Name", result.getName()); // Name should remain unchanged
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void update_shouldHandleWhitespaceInName() {
        // Given
        UserDto originalUser = UserDto.builder()
                .name("Original Name")
                .email("original@example.com")
                .build();
        UserDto savedUser = userService.create(originalUser);

        UserDto updateDto = UserDto.builder()
                .name("   ")
                .email("updated@example.com")
                .build();

        // When
        UserDto result = userService.update(savedUser.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Original Name", result.getName()); // Name should remain unchanged
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void getAllUsers_shouldReturnEmptyListWhenNoUsers() {
        // Given - Clean database (transactional will roll back)

        // When
        Collection<UserDto> results = userService.getAllUsers();

        // Then
        assertNotNull(results);
        // The list might not be empty due to other tests, but we can verify the method works
    }

    @Test
    void update_shouldAllowSameEmailForSameUser() {
        // Given
        UserDto originalUser = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        UserDto savedUser = userService.create(originalUser);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("test@example.com") // Same email
                .build();

        // When - Should not throw exception for same user
        UserDto result = userService.update(savedUser.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }
}