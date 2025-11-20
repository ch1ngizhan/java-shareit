package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithComment;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ItemServiceImplEdgeCasesTest {

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
    void create_shouldHandleNullAvailableField() {
        // Given
        ItemDto itemWithNullAvailable = ItemDto.builder()
                .name("Item with null available")
                .description("Description")
                .available(null)
                .build();

        // When
        ItemDto result = itemService.create(itemWithNullAvailable, owner.getId());

        // Then
        assertNotNull(result);
        assertFalse(result.getAvailable()); // Should default to false
    }

    @Test
    void update_shouldHandlePartialUpdates() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        // Update only name
        ItemDto nameUpdate = ItemDto.builder()
                .name("Updated Name Only")
                .build();

        // When
        ItemDto result = itemService.update(owner.getId(), nameUpdate, savedItem.getId());

        // Then
        assertNotNull(result);
        assertEquals("Updated Name Only", result.getName());
        assertEquals("Test Description", result.getDescription()); // Should remain unchanged
        assertFalse(result.getAvailable());
    }

    @Test
    void update_shouldHandleEmptyStrings() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        // Try to update with empty strings
        ItemDto emptyUpdate = ItemDto.builder()
                .name("")
                .description("")
                .build();

        // When
        ItemDto result = itemService.update(owner.getId(), emptyUpdate, savedItem.getId());

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.getName()); // Should remain unchanged
        assertEquals("Test Description", result.getDescription()); // Should remain unchanged
    }

    @Test
    void getItemById_shouldNotShowBookingsForNonOwner() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        // When - non-owner requests the item
        ItemWithComment result = itemService.getItemById(anotherUser.getId(), savedItem.getId());

        // Then
        assertNotNull(result);
        assertNull(result.getLastBooking()); // Non-owner shouldn't see bookings
        assertNull(result.getNextBooking()); // Non-owner shouldn't see bookings
    }

    @Test
    void getAllItems_shouldReturnEmptyForUserWithoutItems() {
        // Given - anotherUser has no items

        // When
        Collection<ItemWithComment> results = itemService.getAllItems(anotherUser.getId());

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void search_shouldHandleSpecialCharacters() {
        // Given
        ItemDto specialItem = ItemDto.builder()
                .name("Item with special chars !@#$%")
                .description("Description with special chars !@#$%")
                .available(true)
                .build();
        itemService.create(specialItem, owner.getId());

        // When
        Collection<ItemDto> results = itemService.search("special");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void createComment_shouldThrowExceptionWhenUserNeverBookedItem() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        // When & Then - anotherUser never booked this item
        assertThrows(IllegalArgumentException.class, () ->
                itemService.createComment(anotherUser.getId(), savedItem.getId(), commentDto));
    }

    @Test
    void delete_shouldThrowExceptionForNonOwner() {
        // Given
        ItemDto savedItem = itemService.create(itemDto, owner.getId());

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
                itemService.delete(savedItem.getId(), anotherUser.getId()));
    }
}