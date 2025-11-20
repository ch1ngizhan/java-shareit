package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ItemServiceImplSearchTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto owner;

    @BeforeEach
    void setUp() {
        owner = userService.create(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());
    }

    @Test
    void search_shouldFindItemsByName() {
        // Given
        ItemDto item1 = ItemDto.builder()
                .name("Drill Machine")
                .description("Powerful drill")
                .available(true)
                .build();
        itemService.create(item1, owner.getId());

        ItemDto item2 = ItemDto.builder()
                .name("Hammer")
                .description("Construction hammer")
                .available(true)
                .build();
        itemService.create(item2, owner.getId());

        // When
        Collection<ItemDto> results = itemService.search("drill");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.stream().anyMatch(item -> item.getName().contains("Drill")));
    }

    @Test
    void search_shouldFindItemsByDescription() {
        // Given
        ItemDto item1 = ItemDto.builder()
                .name("Drill")
                .description("Powerful electric drill machine")
                .available(true)
                .build();
        itemService.create(item1, owner.getId());

        ItemDto item2 = ItemDto.builder()
                .name("Hammer")
                .description("Construction tool")
                .available(true)
                .build();
        itemService.create(item2, owner.getId());

        // When
        Collection<ItemDto> results = itemService.search("electric");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.stream().anyMatch(item -> item.getDescription().contains("electric")));
    }

    @Test
    void search_shouldReturnEmptyForUnavailableItems() {
        // Given
        ItemDto item = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(false)
                .build();
        itemService.create(item, owner.getId());

        // When
        Collection<ItemDto> results = itemService.search("drill");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void search_shouldReturnEmptyForNoMatches() {
        // Given
        ItemDto item = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();
        itemService.create(item, owner.getId());

        // When
        Collection<ItemDto> results = itemService.search("nonexistent");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void search_shouldBeCaseInsensitive() {
        // Given
        ItemDto item = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();
        itemService.create(item, owner.getId());

        // When
        Collection<ItemDto> results1 = itemService.search("DRILL");
        Collection<ItemDto> results2 = itemService.search("drill");
        Collection<ItemDto> results3 = itemService.search("DrIlL");

        // Then
        assertNotNull(results1);
        assertFalse(results1.isEmpty());
        assertEquals(results1.size(), results2.size());
        assertEquals(results1.size(), results3.size());
    }

    @Test
    void search_shouldHandleEmptySearchText() {
        // When
        Collection<ItemDto> results = itemService.search("");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void search_shouldHandleNullSearchText() {
        // When
        Collection<ItemDto> results = itemService.search(null);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}