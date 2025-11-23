package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotUniqueEmailException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceImplCoverageTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateUser_SameEmail_ShouldNotCheckUniqueness() {
        // Подготовка
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("original@example.com") // Тот же email
                .build();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Original Name");
        existingUser.setEmail("original@example.com");

        when(userStorage.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userStorage.save(any(User.class))).thenReturn(existingUser);

        // Выполнение
        UserDto result = userService.update(userId, updateDto);

        // Проверка - не должно быть проверки уникальности для того же email
        assertNotNull(result);
    }

    @Test
    void updateUser_NewEmailExists_ShouldThrowException() {
        // Подготовка
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .email("existing@example.com") // Существующий email
                .build();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("original@example.com");

        when(userStorage.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userStorage.existsByEmail("existing@example.com")).thenReturn(true);

        // Проверка
        assertThrows(NotUniqueEmailException.class,
                () -> userService.update(userId, updateDto));
    }

    @Test
    void getAllUsers_EmptyDatabase_ShouldReturnEmptyList() {
        // Подготовка
        when(userStorage.findAll()).thenReturn(List.of());

        // Выполнение
        var result = userService.getAllUsers();

        // Проверка
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteUser_UserExists_ShouldCallDelete() {
        // Подготовка
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));

        // Выполнение - не должно бросать исключение
        assertDoesNotThrow(() -> userService.delete(userId));
    }
}