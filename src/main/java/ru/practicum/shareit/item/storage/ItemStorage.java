package ru.practicum.shareit.item.storage;


import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {
    Item create(Item item, Long owner);

    void delete(Long id);

    Item update(Item item);

    Optional<Item> getItemById(Long id);

    Collection<Item> getAllItems(Long userId);

    Collection<Item> search(String text);

}
