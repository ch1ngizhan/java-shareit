package ru.practicum.shareit.user.storage;


import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    void delete(Long id);

    User update(User user);

    Optional<User> getUserById(Long id);

    Collection<User> getAllUsers();

    Optional<User> getUserByEmail(String email);
}
