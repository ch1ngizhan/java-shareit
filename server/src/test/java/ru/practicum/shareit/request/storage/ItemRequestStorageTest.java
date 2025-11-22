package ru.practicum.shareit.request.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemRequestStorageTest {

    @Mock
    private ItemRequestStorage itemRequestStorage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByRequesterIdOrderByCreatedDesc() {
        when(itemRequestStorage.findByRequesterIdOrderByCreatedDesc(1L)).thenReturn(List.of(new ItemRequest()));
        List<ItemRequest> requests = itemRequestStorage.findByRequesterIdOrderByCreatedDesc(1L);
        assertFalse(requests.isEmpty());
        verify(itemRequestStorage).findByRequesterIdOrderByCreatedDesc(1L);
    }

    @Test
    void testFindAllByRequesterIdNot() {
        Pageable pageable = Pageable.unpaged();
        Page<ItemRequest> page = new PageImpl<>(List.of(new ItemRequest()));
        when(itemRequestStorage.findAllByRequesterIdNot(1L, pageable)).thenReturn(page);
        Page<ItemRequest> result = itemRequestStorage.findAllByRequesterIdNot(1L, pageable);
        assertFalse(result.isEmpty());
        verify(itemRequestStorage).findAllByRequesterIdNot(1L, pageable);
    }
}
