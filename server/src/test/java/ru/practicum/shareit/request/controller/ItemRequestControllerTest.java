package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestResponseDto;
import ru.practicum.shareit.request.storage.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto requestDto;
    private ItemRequestResponseDto responseDto;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        requestDto = new ItemRequestDto();
        requestDto.setDescription("I need a drill");

        responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("I need a drill");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(List.of());
    }

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createRequest(anyLong(), any(ItemRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("I need a drill"));
    }

    @Test
    void getUserRequests_shouldReturnUserRequests() throws Exception {
        when(itemRequestService.getUserRequests(anyLong())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("I need a drill"));
    }

    @Test
    void getAllRequests_shouldReturnAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("I need a drill"));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("I need a drill"));
    }
}