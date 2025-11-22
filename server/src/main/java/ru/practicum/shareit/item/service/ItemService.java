package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithComment;

import java.util.Collection;

public interface ItemService {
    ItemDto create(ItemDto item, Long owner);

    void delete(Long id, Long userid);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    ItemWithComment getItemById(Long userId, Long itemId);

    Collection<ItemWithComment> getAllItems(Long userId);

    Collection<ItemDto> search(String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}
