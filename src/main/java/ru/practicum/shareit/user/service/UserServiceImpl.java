package ru.practicum.shareit.user.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotUniqueEmailException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        log.info("Создание нового пользователя: email={}, name={}",
                userDto.getEmail(), userDto.getName());

        validateEmailFormat(userDto.getEmail());
        validateEmailUniqueness(userDto.getEmail());

        User user = UserMapper.toUser(userDto);
        User createdUser = userStorage.save(user);

        log.debug("Пользователь успешно создан: ID={}, email={}",
                createdUser.getId(), createdUser.getEmail());
        log.info("Пользователь создан с ID: {}", createdUser.getId());

        return UserMapper.toUserDto(createdUser);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        User user = getUserOrThrow(id);
        userStorage.delete(user);
        log.debug("Пользователь с ID: {} успешно удален", id);
        log.info("Удаление пользователя с ID: {} завершено", id);
    }

    @Transactional
    @Override
    public UserDto update(Long id, UserDto userDto) {
        log.info("Запрос на обновление пользователя с ID: {}", id);
        log.debug("Данные для обновления: name={}, email={}",
                userDto.getName(), userDto.getEmail());

        User user = getUserOrThrow(id);
        log.debug("Найден пользователь для обновления: ID={}, текущий email={}",
                id, user.getEmail());

        // Проверяем и обновляем email
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            log.debug("Обновление email пользователя ID: {} с {} на {}",
                    id, user.getEmail(), userDto.getEmail());
            validateEmailFormat(userDto.getEmail());
            validateEmailUniqueness(userDto.getEmail());
        }

        // Обновляем имя, если предоставлено и не пустое
        if (userDto.getName() != null && !userDto.getName().trim().isEmpty()) {
            log.debug("Обновление имени пользователя ID: {} с '{}' на '{}'",
                    id, user.getName(), userDto.getName());
            user.setName(userDto.getName().trim());
        } else if (userDto.getName() != null) {
            log.warn("Попытка установить пустое имя для пользователя ID: {}", id);
        }

        // Обновляем email, если предоставлен и не пустой
        if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
            user.setEmail(userDto.getEmail().trim());
        } else if (userDto.getEmail() != null) {
            log.warn("Попытка установить пустой email для пользователя ID: {}", id);
        }

        User updatedUser = userStorage.save(user);
        log.debug("Пользователь с ID: {} успешно обновлен", id);
        log.info("Обновление пользователя с ID: {} завершено", id);

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Запрос пользователя с ID: {}", id);
        User user = getUserOrThrow(id);
        log.debug("Пользователь найден: ID={}, email={}, name={}",
                user.getId(), user.getEmail(), user.getName());
        log.info("Пользователь с ID: {} успешно получен", id);
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public Collection<UserDto> getAllUsers() {
        log.info("Запрос всех пользователей");
        Collection<User> users = userStorage.findAll();
        log.debug("Найдено {} пользователей", users.size());
        log.info("Список всех пользователей успешно получен (количество: {})", users.size());
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void validateEmailUniqueness(String email) {
        log.debug("Проверка уникальности email: {}", email);
        if (userStorage.existsByEmail(email)) {
            log.warn("Попытка использовать уже существующий email.");
            throw new NotUniqueEmailException("Пользователь с электронной почтой " + email + " уже существует");
        }
        log.debug("Email {} уникален", email);
    }

    private User getUserOrThrow(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID: {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найдена");
                });
    }

    private void validateEmailFormat(String email) {
        log.debug("Проверка формата email: {}", email);
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            log.warn("Неверный формат email: {}", email);
            throw new ValidationException("Неверный формат электронной почты: " + email);
        }
        log.debug("Формат email {} корректен", email);
    }
}