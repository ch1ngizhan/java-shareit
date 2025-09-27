package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Transactional
    @Override
    public ItemDto create(ItemDto newItem, Long ownerId) {
        log.info("Создание новой вещи пользователем с ID: {}", ownerId);
        User owner = UserMapper.toUser(userService.getUserById(ownerId));

        if (newItem == null) {
            log.warn("Попытка создания вещи с null данными от пользователя: {}", owner);
            throw new ValidationException("Item не должен быть равен null");
        }

        Item item = ItemMapper.toItem(newItem);
        item.setOwner(owner);
        itemStorage.save(item);
        log.debug("Вещь создана: ID={}, название='{}'", item.getId(), item.getName());
        return ItemMapper.toItemDto(item);
    }
    @Transactional
    @Override
    public void delete(Long itemId, Long userId) {
        userService.getUserById(userId);
        Item item = getItemOrThrow(itemId);
        validateItemOwnership(item, userId);
        log.info("Удаление вещи с ID: {}", itemId);
        itemStorage.delete(item);
        log.debug("Вещь с ID: {} успешно удалена", itemId);
    }
    @Transactional
    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        log.info("Обновление вещи с ID: {} пользователем с ID: {}", itemId, userId);

        Item oldItem = getItemOrThrow(itemId);

        validateItemOwnership(oldItem, userId);

        Item item = ItemMapper.toItem(itemDto);
        if (Objects.isNull(item.getAvailable())) {
            item.setAvailable(oldItem.getAvailable());
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getName() == null || item.getName().isBlank()) {
            item.setName(oldItem.getName());
        }
        item.setId(oldItem.getId());
        item.setRequest(oldItem.getRequest());
        item.setOwner(oldItem.getOwner());

        Item updatedItem = itemStorage.save(item);
        log.debug("Вещь с ID: {} успешно обновлена", itemId);
        return ItemMapper.toItemDto(updatedItem);


    }

    @Override
    public ItemDto getItemById(Long id) {
        log.info("Запрос вещи с ID: {}", id);
        Item item = getItemOrThrow(id);
        log.debug("Вещь с ID: {} найдена: {}", id, item.getName());
        return ItemMapper.toItemDto(item);
    }
    @Transactional
    @Override
    public Collection<ItemDto> getAllItems(Long userId) {
        log.info("Запрос всех вещей пользователя с ID: {}", userId);
        userService.getUserById(userId);
        Collection<Item> items = itemStorage.findByOwnerId(userId);
        log.debug("Найдено {} вещей для пользователя с ID: {}", items.size(), userId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        if (text == null || text.trim().isEmpty()) {
            log.debug("Пустой поисковый запрос, возвращен пустой список");
            return Collections.emptyList();
        }
        String searchText = text.toLowerCase();
        Collection<Item> items = itemStorage.search(searchText);
        log.debug("Найдено {} вещей по запросу: '{}'", items.size(), text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private Item getItemOrThrow(Long id) {
        log.debug("Поиск вещи с ID: {}", id);
        return itemStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID: {} не найдена", id);
                    return new NotFoundException("Вещь с id " + id + " не найдена");
                });
    }

    private void validateItemOwnership(Item item, Long userId) {
        if (!item.getOwner().equals(userId)) {
            log.warn("Пользователь с ID: {} не является владельцем вещи с ID: {}", userId, item.getId());
            throw new AccessDeniedException("Пользователь не является владельцем вещи.");
        }
    }
}