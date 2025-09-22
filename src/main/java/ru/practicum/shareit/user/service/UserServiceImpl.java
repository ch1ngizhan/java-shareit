package ru.yandex.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.NotUniqueEmailException;
import ru.yandex.practicum.user.mapper.UserMapper;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.model.UserDto;
import ru.yandex.practicum.user.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        Optional<User> existingUser = userStorage.getUserByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            throw new NotUniqueEmailException("Пользователь с такой электронной почтой уже существует");
        }

        User user = UserMapper.toUser(userDto);
        User createdUser = userStorage.create(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public void delete(Long id) {
        if (userStorage.getUserById(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        userStorage.delete(id);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        Optional<User> existingUser = userStorage.getUserById(id);
        if (existingUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        User user = existingUser.get();

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            Optional<User> userWithSameEmail = userStorage.getUserByEmail(userDto.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
                throw new NotUniqueEmailException("Пользователь с такой электронной почтой уже существует");
            }
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        User updatedUser = userStorage.update(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.debug("Looking for user with ID: {}", id);
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        Collection<User> users = userStorage.getAllUsers();
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }


}
