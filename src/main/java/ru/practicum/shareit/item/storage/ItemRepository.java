package ru.yandex.practicum.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ItemRepository implements ItemStorage {

    Map<Long, Item> items = new HashMap<Long, Item>();

    @Override
    public Item create(Item item, Long owner) {
        item.setId(getNextId());
        item.setOwner(owner);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void delete(Long id) {
        if (items.containsKey(id)) {
            items.remove(id);
        }
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return items.values().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    @Override
    public Collection<Item> getAllItems(Long userId) {
        List<Item> ownerItem = items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .toList();
        return new ArrayList<>(ownerItem);
    }

    @Override
    public Collection<Item> search(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> {
                    String name = item.getName();
                    String description = item.getDescription();
                    return (name != null && name.toLowerCase().contains(searchText)) ||
                            (description != null && description.toLowerCase().contains(searchText));
                })
                .collect(Collectors.toList());
    }


    private Long getNextId() {
        Long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
