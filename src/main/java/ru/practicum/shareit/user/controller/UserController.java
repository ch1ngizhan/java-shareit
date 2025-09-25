package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        UserDto user = userService.create(userDto);
        log.info("UserController: добавлен новый пользователь.");
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@PathVariable("userId") Long userId, @RequestBody UserDto userDto) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        log.info("UserController: данные пользователя обновлены: {}", userId);
        return ResponseEntity.ok(userService.update(userId, userDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        userService.delete(userId);
        log.info("UserController: пользователь с id: {} удалён", userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<Collection<UserDto>> allUsers() {
        Collection<UserDto> allUsers = userService.getAllUsers();
        log.info("UserController: количество всех пользователей: {}", allUsers.size());
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable(value = "userId") Long userId) {
        log.info("UserController: запрошен пользователь с id: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }
}
