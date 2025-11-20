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

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserServiceImplExceptionTest {

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
    void create_shouldThrowNotUniqueEmailException() {
        // Given
        userService.create(testUserDto);

        UserDto duplicateUser = UserDto.builder()
                .name("Another User")
                .email("test@example.com")
                .build();

        // When & Then
        assertThrows(NotUniqueEmailException.class, () ->
                userService.create(duplicateUser));
    }

    @Test
    void update_shouldThrowNotUniqueEmailException() {
        // Given
        UserDto savedUser = userService.create(testUserDto);

        UserDto anotherUser = UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build();
        userService.create(anotherUser);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("another@example.com") // Используем email другого пользователя
                .build();

        // When & Then
        assertThrows(NotUniqueEmailException.class, () ->
                userService.update(savedUser.getId(), updateDto));
    }

    @Test
    void getUserById_shouldThrowNotFoundException() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                userService.getUserById(999L));
    }

    @Test
    void update_shouldThrowNotFoundException() {
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
    void delete_shouldThrowNotFoundException() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                userService.delete(999L));
    }

    @Test
    void create_shouldThrowExceptionForInvalidEmail() {
        // Given
        UserDto invalidUser = UserDto.builder()
                .name("Test User")
                .email("invalid-email") // Неправильный формат email
                .build();

        // When & Then
        assertThrows(NotUniqueEmailException.class, () ->
                userService.create(invalidUser));
    }
}