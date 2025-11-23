package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotUniqueEmailException;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceImplValidationTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_InvalidEmailFormat_ShouldThrowException() {
        // Подготовка
        UserDto userDto;
        userDto = UserDto.builder()
                .name("Тестовый пользователь")
                .email("неправильный-email") // Неверный формат
                .build();

        // Проверка
        assertThrows(NotUniqueEmailException.class, () -> userService.create(userDto));
    }

    @Test
    void createUser_EmptyEmail_ShouldThrowException() {
        // Подготовка
        UserDto userDto = UserDto.builder()
                .name("Тестовый пользователь")
                .email("") // Пустой email
                .build();

        // Проверка
        assertThrows(NotUniqueEmailException.class, () -> userService.create(userDto));
    }

    @Test
    void updateUser_EmptyName_ShouldNotUpdate() {
        // Подготовка
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .name("   ") // Пустое имя с пробелами
                .email("test@example.com")
                .build();

        ru.practicum.shareit.user.model.User existingUser = new ru.practicum.shareit.user.model.User();
        existingUser.setId(userId);
        existingUser.setName("Оригинальное имя");
        existingUser.setEmail("original@example.com");

        when(userStorage.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userStorage.existsByEmail("test@example.com")).thenReturn(false);
        when(userStorage.save(any())).thenReturn(existingUser);

        // Выполнение
        UserDto result = userService.update(userId, updateDto);

        // Проверка - имя должно остаться неизменным
        assertEquals("Оригинальное имя", result.getName());
    }
}