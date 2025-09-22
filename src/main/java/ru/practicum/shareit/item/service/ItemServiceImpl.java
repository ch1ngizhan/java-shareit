package ru.yandex.practicum.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exception.AccessDeniedException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.item.mapper.ItemMapper;
import ru.yandex.practicum.item.model.Item;
import ru.yandex.practicum.item.model.ItemDto;
import ru.yandex.practicum.item.storage.ItemStorage;
import ru.yandex.practicum.user.service.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemDto create(ItemDto newItem, Long owner) {
        userService.getUserById(owner);
        if (newItem == null) {
            throw new ValidationException("Item не должен быть равен null");
        }
        if (newItem.getAvailable() == null) {
            throw new ValidationException("Поле available обязательно");
        }

        Item item = itemStorage.create(ItemMapper.toItem(newItem), owner);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public void delete(Long id) {
        itemStorage.delete(id);
    }

    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        Optional<Item> itemOptional = itemStorage.getItemById(itemId);
        if (itemOptional.isPresent()) {
            if (!itemOptional.get().getOwner().equals(userId)) {
                throw new AccessDeniedException("Пользователь не является владельцем вещи.");
            }
            Item itemOld = itemOptional.get();
            Item item = ItemMapper.toItem(itemDto);
            if (Objects.isNull(item.getAvailable())) {
                item.setAvailable(itemOld.getAvailable());
            }
            if (Objects.isNull(item.getDescription())) {
                item.setDescription(itemOld.getDescription());
            }
            if (Objects.isNull(item.getName())) {
                item.setName(itemOld.getName());
            }
            item.setId(itemOld.getId());
            item.setRequest(itemOld.getRequest());
            item.setOwner(itemOld.getOwner());
            ;
            return ItemMapper.toItemDto(itemStorage.update(item));
        }
        throw new NotFoundException("Item not found");

    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = itemStorage.getItemById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));

        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> getAllItems(Long userId) {
        Collection<Item> items = itemStorage.getAllItems(userId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String searchText = text.toLowerCase();
        Collection<Item> items = itemStorage.search(searchText);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }


}
