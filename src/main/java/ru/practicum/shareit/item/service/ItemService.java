package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(ItemDto item, Long owner);

    void delete(Long id, Long userid);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    ItemDto getItemById(Long id);

    Collection<ItemDto> getAllItems(Long userId);

    Collection<ItemDto> search(String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}
