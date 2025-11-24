package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotUniqueEmailException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        userDto = new UserDto();
        userDto.setName("John");
        userDto.setEmail("john@example.com");
    }

    @Test
    void createUser_success() {
        when(userStorage.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userStorage.save(any(User.class))).thenReturn(user);

        UserDto created = userService.create(userDto);

        assertNotNull(created);
        assertEquals(user.getId(), created.getId());
        verify(userStorage, times(1)).save(any(User.class));
    }

    @Test
    void createUser_emailAlreadyExists_throwsException() {
        when(userStorage.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(NotUniqueEmailException.class, () -> userService.create(userDto));
        verify(userStorage, never()).save(any());
    }

    @Test
    void getUserById_success() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        UserDto found = userService.getUserById(1L);

        assertNotNull(found);
        assertEquals(user.getEmail(), found.getEmail());
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getAllUsers_success() {
        when(userStorage.findAll()).thenReturn(List.of(user));

        List<UserDto> users = (List<UserDto>) userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals(user.getEmail(), users.get(0).getEmail());
    }

    @Test
    void updateUser_success() {
        UserDto updateDto = new UserDto();
        updateDto.setName("Updated");
        updateDto.setEmail("updated@example.com");

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.existsByEmail("updated@example.com")).thenReturn(false);
        when(userStorage.save(any(User.class))).thenReturn(user);

        UserDto updated = userService.update(1L, updateDto);

        assertNotNull(updated);
        assertEquals("Updated", updated.getName());
        assertEquals("updated@example.com", updated.getEmail());
    }

    @Test
    void updateUser_emailExists_throwsException() {
        UserDto updateDto = new UserDto();
        updateDto.setEmail("exists@example.com");

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.existsByEmail("exists@example.com")).thenReturn(true);

        assertThrows(NotUniqueEmailException.class, () -> userService.update(1L, updateDto));
    }

    @Test
    void deleteUser_success() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userStorage, times(1)).delete(user);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(1L));
    }

    @Test
    void updateUser_WithEmptyName_ShouldKeepOriginalName() {
        // Подготовка
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Original Name");
        existingUser.setEmail("original@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setName("   "); // Пустое имя с пробелами
        updateDto.setEmail("updated@example.com");

        when(userStorage.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userStorage.existsByEmail("updated@example.com")).thenReturn(false);
        when(userStorage.save(any(User.class))).thenReturn(existingUser);

        // Выполнение
        UserDto result = userService.update(userId, updateDto);

        // Проверка - имя должно остаться оригинальным
        assertEquals("Original Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void updateUser_WithSameEmail_ShouldNotCheckUniqueness() {
        // Подготовка
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Test User");
        existingUser.setEmail("test@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("test@example.com"); // Тот же email

        when(userStorage.findById(userId)).thenReturn(Optional.of(existingUser));
        // existsByEmail не должен вызываться для того же email
        when(userStorage.save(any(User.class))).thenReturn(existingUser);

        // Выполнение
        UserDto result = userService.update(userId, updateDto);

        // Проверка
        assertEquals("Updated Name", result.getName());
        verify(userStorage, never()).existsByEmail(anyString());
    }

}
