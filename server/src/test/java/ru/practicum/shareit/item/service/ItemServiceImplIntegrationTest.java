package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithComment;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private BookingStorage bookingStorage;

    private User savedUser;
    private ItemDto testItemDto;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        savedUser = userStorage.save(user);

        // Создаем тестовый ItemDto
        testItemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
    }

    @Test
    void create_shouldSaveItemSuccessfully() {
        // When
        ItemDto result = itemService.create(testItemDto, savedUser.getId());

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void create_shouldSetAvailableToFalseWhenNull() {
        // Given
        ItemDto itemWithNullAvailable = ItemDto.builder()
                .name("Item with null available")
                .description("Description")
                .available(null)
                .build();

        // When
        ItemDto result = itemService.create(itemWithNullAvailable, savedUser.getId());

        // Then
        assertNotNull(result);
        assertFalse(result.getAvailable());
    }

    @Test
    void create_shouldThrowExceptionWhenUserNotFound() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemService.create(testItemDto, nonExistentUserId));
    }

    @Test
    void getItemById_shouldReturnItemWithComments() {
        // Given
        ItemDto savedItem = itemService.create(testItemDto, savedUser.getId());

        // When
        ItemWithComment result = itemService.getItemById(savedUser.getId(), savedItem.getId());

        // Then
        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getId());
        assertEquals("Test Item", result.getName());
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void update_shouldUpdateItemFields() {
        // Given
        ItemDto savedItem = itemService.create(testItemDto, savedUser.getId());

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        // When
        ItemDto result = itemService.update(savedUser.getId(), updateDto, savedItem.getId());

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void search_shouldFindItemsByText() {
        // Given
        itemService.create(testItemDto, savedUser.getId());

        // When
        Collection<ItemDto> results = itemService.search("test");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void search_shouldReturnEmptyListForEmptyText() {
        // When
        Collection<ItemDto> results = itemService.search("");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getAllItems_shouldReturnUserItems() {
        // Given
        itemService.create(testItemDto, savedUser.getId());

        // When
        Collection<ItemWithComment> results = itemService.getAllItems(savedUser.getId());

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }
}