package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerErrorTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void update_NotOwner_ShouldReturnForbidden() throws Exception {
        // Подготовка
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Updated Item");

        when(itemService.update(anyLong(), any(ItemDto.class), anyLong()))
                .thenThrow(new AccessDeniedException("Не владелец"));

        // Проверка
        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getItemById_NotFound_ShouldReturnNotFound() throws Exception {
        // Подготовка
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        // Проверка
        mockMvc.perform(get("/items/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Вещь не найдена"));
    }

    @Test
    void createComment_InvalidBooking_ShouldReturnBadRequest() throws Exception {
        // Подготовка
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        when(itemService.createComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new ValidationException("Нельзя оставить комментарий"));

        // Проверка
        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}