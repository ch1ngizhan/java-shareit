package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestHeader(USER_HEADER) Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        ItemDto item = itemService.create(itemDto, userId);
        log.info("ItemController: добавлен новая вещ.");
        return ResponseEntity.ok(item);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader(USER_HEADER) Long userId,
                                          @RequestBody ItemDto itemDto,
                                          @PathVariable("itemId") Long itemId) {
        ItemDto updatedItem = itemService.update(userId, itemDto, itemId);
        log.info("ItemController: данные вещи обновлены: {}", itemId);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@RequestHeader(USER_HEADER) Long userId,
                                       @PathVariable Long itemId) {
        itemService.delete(itemId, userId);
        log.info("ItemController: вещ с id: {} удалёна", itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<Collection<ItemDto>> allItems(@RequestHeader(USER_HEADER) Long userId) {
        Collection<ItemDto> allItems = itemService.getAllItems(userId);
        log.info("ItemController: количество всех вещей: {}", allItems.size());
        return ResponseEntity.ok(allItems);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getUserById(@RequestHeader(USER_HEADER) Long userId,
                                               @PathVariable("itemId")
                                               Long itemId) {
        log.info("ItemController: запрошена вещ с id: {}", itemId);
        return ResponseEntity.ok(itemService.getItemById(itemId));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> searchItems(@RequestHeader(USER_HEADER) Long userId,
                                                           @RequestParam(name = "text") String text) {
        log.info("GET Запрос на поиск предметов");
        return ResponseEntity.ok(itemService.search(text));
    }
    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }
}
