package ru.practicum.shareit.user.service;


import ru.practicum.shareit.user.model.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto user);

    void delete(Long id);

    UserDto update(Long id, UserDto userDto);

    UserDto getUserById(Long id);

    Collection<UserDto> getAllUsers();
}
