package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_shouldConvertUserToDto() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        // When
        UserDto result = UserMapper.toUserDto(user);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void toUser_shouldConvertDtoToUser() {
        // Given
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        // When
        User result = UserMapper.toUser(userDto);

        // Then
        assertNotNull(result);
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        // Note: ID is not set in toUser method
    }

    @Test
    void toUser_shouldHandleNullValues() {
        // Given
        UserDto userDto = UserDto.builder()
                .name(null)
                .email(null)
                .build();

        // When
        User result = UserMapper.toUser(userDto);

        // Then
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getEmail());
    }
}