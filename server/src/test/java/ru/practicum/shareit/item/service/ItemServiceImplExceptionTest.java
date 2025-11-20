package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ItemServiceImplExceptionTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto owner;
    private UserDto anotherUser;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        anotherUser = userService.create(UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build());

        itemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
    }

    @Test
    void create_shouldThrowNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.create(itemDto, 999L));
    }

    @Test
    void update_shouldThrowNotFoundExceptionForNonExistentItem() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.update(owner.getId(), itemDto, 999L));
    }

    @Test
    void update_shouldThrowAccessDeniedExceptionForNonOwner() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                itemService.update(anotherUser.getId(), itemDto, savedItem.getId()));
    }

    @Test
    void getItemById_shouldThrowNotFoundExceptionForNonExistentItem() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.getItemById(owner.getId(), 999L));
    }

    @Test
    void getItemById_shouldThrowNotFoundExceptionForNonExistentUser() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.getItemById(999L, savedItem.getId()));
    }

    @Test
    void delete_shouldThrowNotFoundExceptionForNonExistentItem() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.delete(999L, owner.getId()));
    }

    @Test
    void delete_shouldThrowAccessDeniedExceptionForNonOwner() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                itemService.delete(savedItem.getId(), anotherUser.getId()));
    }

    @Test
    void createComment_shouldThrowNotFoundExceptionForNonExistentUser() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.createComment(999L, savedItem.getId(), commentDto));
    }

    @Test
    void createComment_shouldThrowNotFoundExceptionForNonExistentItem() {
        // Given
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.createComment(anotherUser.getId(), 999L, commentDto));
    }

    @Test
    void createComment_shouldThrowIllegalArgumentExceptionForNonBooker() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        // When & Then - anotherUser never booked this item
        assertThrows(IllegalArgumentException.class, () ->
                itemService.createComment(anotherUser.getId(), savedItem.getId(), commentDto));
    }
}