package ru.practicum.shareit.request.mapper;


import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    @Test
    void toItemRequestDto_WithNullInput_ShouldReturnNull() {
        assertNull(ItemRequestMapper.toItemRequestDto(null));
    }

    @Test
    void toItemRequestDto_WithItems_ShouldMapCorrectly() {
        // Подготовка
        User requester = new User();
        requester.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test request");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setRequest(request);

        request.setItems(List.of(item));

        // Выполнение
        ItemRequestDto result = ItemRequestMapper.toItemRequestDto(request);

        // Проверка
        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals(item.getId(), result.getItems().get(0).getId());
    }

    @Test
    void toItemRequest_WithNullInput_ShouldReturnNull() {
        assertNull(ItemRequestMapper.toItemRequest(null));
    }

    @Test
    void toItemRequest_WithDto_ShouldMapCorrectly() {
        // Подготовка
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Test request");
        dto.setCreated(LocalDateTime.now());

        // Выполнение
        ItemRequest result = ItemRequestMapper.toItemRequest(dto);

        // Проверка
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getCreated(), result.getCreated());
    }
}