package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotUniqueEmailException;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserStorage userStorage;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void create_shouldSaveUserSuccessfully() {
        // When
        UserDto result = userService.create(testUserDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void create_shouldThrowExceptionForDuplicateEmail() {
        // Given
        userService.create(testUserDto);

        UserDto duplicateUserDto = UserDto.builder()
                .name("Another User")
                .email("test@example.com")
                .build();

        // When & Then
        assertThrows(NotUniqueEmailException.class, () ->
                userService.create(duplicateUserDto));
    }

    @Test
    void update_shouldUpdateUserFields() {
        // Given
        UserDto savedUser = userService.create(testUserDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        // When
        UserDto result = userService.update(savedUser.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void update_shouldUpdateOnlyNameWhenEmailIsNull() {
        // Given
        UserDto savedUser = userService.create(testUserDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email(null)
                .build();

        // When
        UserDto result = userService.update(savedUser.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("test@example.com", result.getEmail()); // Email remains unchanged
    }

    @Test
    void update_shouldThrowExceptionWhenUserNotFound() {
        // Given
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        // When & Then
        assertThrows(NotFoundException.class, () ->
                userService.update(999L, updateDto));
    }

    @Test
    void getUserById_shouldReturnUser() {
        // Given
        UserDto savedUser = userService.create(testUserDto);

        // When
        UserDto result = userService.getUserById(savedUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals("Test User", result.getName());
    }

    @Test
    void getUserById_shouldThrowExceptionWhenUserNotFound() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                userService.getUserById(999L));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Given
        userService.create(testUserDto);

        UserDto anotherUser = UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build();
        userService.create(anotherUser);

        // When
        Collection<UserDto> results = userService.getAllUsers();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void delete_shouldDeleteUserSuccessfully() {
        // Given
        UserDto savedUser = userService.create(testUserDto);

        // When
        userService.delete(savedUser.getId());

        // Then
        assertThrows(NotFoundException.class, () ->
                userService.getUserById(savedUser.getId()));
    }
}