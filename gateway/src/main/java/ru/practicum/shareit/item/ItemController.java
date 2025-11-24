package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Slf4j
@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Creating item {} for user {}", itemDto, userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemDto itemDto,
                                         @PathVariable("itemId") Long itemId) {
        log.info("Updating item {} for user {}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId) {
        log.info("Deleting item {} for user {}", itemId, userId);
        return itemClient.deleteItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get all items for user {}", userId);
        return itemClient.getAllItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable("itemId") Long itemId) {
        log.info("Get item {} for user {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(name = "text") String text) {
        log.info("Search items with text: {}", text);
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Пустой поисковый запрос");
        }
        return itemClient.searchItems(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId,
                                                @Valid @RequestBody CommentDto commentDto) {
        log.info("Creating comment for item {} by user {}", itemId, userId);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}