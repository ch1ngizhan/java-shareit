package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ItemStorageTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemStorage itemStorage;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        entityManager.persist(owner);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        entityManager.persist(item);

        entityManager.flush();
    }

    @Test
    void search_shouldFindItemsByText() {
        // When
        List<Item> results = itemStorage.search("test");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(item.getId(), results.get(0).getId());
    }

    @Test
    void search_shouldReturnEmptyForNonMatchingText() {
        // When
        List<Item> results = itemStorage.search("nonexistent");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void findByOwnerId_shouldReturnUserItems() {
        // When
        List<Item> results = itemStorage.findByOwnerId(owner.getId());

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(item.getId(), results.get(0).getId());
    }

    @Test
    void findByOwnerIdOrderByIdDesc_shouldReturnUserItemsOrdered() {
        // Given - Create another item
        Item anotherItem = new Item();
        anotherItem.setName("Another Item");
        anotherItem.setDescription("Another Description");
        anotherItem.setAvailable(true);
        anotherItem.setOwner(owner);
        entityManager.persist(anotherItem);
        entityManager.flush();

        // When
        List<Item> results = itemStorage.findByOwnerIdOrderByIdDesc(owner.getId());

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        // Should be ordered by ID descending
        assertTrue(results.get(0).getId() > results.get(1).getId());
    }

    @Test
    void findById_shouldReturnItem() {
        // When
        Item result = itemStorage.findById(item.getId()).orElse(null);

        // Then
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
    }

    @Test
    void findById_shouldReturnEmptyForNonExistentItem() {
        // When
        var result = itemStorage.findById(999L);

        // Then
        assertTrue(result.isEmpty());
    }
}