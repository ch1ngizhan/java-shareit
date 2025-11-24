package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceImplValidationTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;


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