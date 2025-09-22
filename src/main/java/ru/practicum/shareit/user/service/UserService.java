package ru.yandex.practicum.user.service;

import ru.yandex.practicum.user.model.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto user);

    void delete(Long id);

    UserDto update(Long id, UserDto userDto);

    UserDto getUserById(Long id);

    Collection<UserDto> getAllUsers();
}
