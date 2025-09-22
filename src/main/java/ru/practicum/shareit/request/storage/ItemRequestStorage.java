package ru.yandex.practicum.request.storage;

import ru.yandex.practicum.request.ItemRequest;

import java.util.Collection;

public interface ItemRequestStorage {
    ItemRequest create(ItemRequest itemRequest);

    void delete(Long id);

    void update(ItemRequest itemRequest);

    ItemRequest getItemRequestById(Long id);

    Collection<ItemRequest> getAllItemRequests();
}
