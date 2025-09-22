package ru.yandex.practicum.item.service;

import ru.yandex.practicum.item.model.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(ItemDto item, Long owner);

    void delete(Long id);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    ItemDto getItemById(Long id);

    Collection<ItemDto> getAllItems(Long userId);

    Collection<ItemDto> search(String text);
}
