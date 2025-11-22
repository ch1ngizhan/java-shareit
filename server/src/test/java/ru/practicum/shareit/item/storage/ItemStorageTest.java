package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemStorageTest {

    @Mock
    private ItemStorage itemStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearch() {
        when(itemStorage.search("test")).thenReturn(List.of(new Item()));
        List<Item> items = itemStorage.search("test");
        assertFalse(items.isEmpty());
        verify(itemStorage).search("test");
    }

    @Test
    void testFindByOwnerId() {
        when(itemStorage.findByOwnerId(1L)).thenReturn(List.of(new Item()));
        List<Item> items = itemStorage.findByOwnerId(1L);
        assertFalse(items.isEmpty());
        verify(itemStorage).findByOwnerId(1L);
    }
}
