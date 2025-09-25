package ru.practicum.shareit.request.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemRequestRepository implements ItemRequestStorage {

    private final Map<Long, ItemRequest> itemRequests = new HashMap<>();

    @Override
    public ItemRequest create(ItemRequest itemRequest) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public void update(ItemRequest itemRequest) {

    }

    @Override
    public ItemRequest getItemRequestById(Long id) {

        return itemRequests.get(id);
    }

    @Override
    public Collection<ItemRequest> getAllItemRequests() {
        return List.of();
    }
}
