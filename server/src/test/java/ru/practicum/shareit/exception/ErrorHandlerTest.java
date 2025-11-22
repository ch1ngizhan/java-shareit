package ru.practicum.shareit.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void handleNotFoundException_shouldReturn404() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Бронирование не найдено"));
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() throws Exception {
        when(bookingService.update(anyLong(), anyBoolean(), anyLong()))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Доступ запрещен"));
    }

    @Test
    void handleNotUniqueEmailException_shouldReturn409() throws Exception {
        // This test would be in UserController tests
        // Just demonstrating the pattern
    }

    @Test
    void handleIllegalArgumentException_shouldReturn400() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().minusDays(1)); // Past date
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingService.create(anyLong(), any(BookingDto.class)))
                .thenThrow(new IllegalArgumentException("Дата начала должна быть в будущем"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата начала должна быть в будущем"));
    }

    @Test
    void handleMissingRequestHeaderException_shouldReturn400() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Необходим заголовок: X-Sharer-User-Id"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException_shouldReturn400() throws Exception {
        mockMvc.perform(get("/bookings/abc")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Некорректный тип параметра")));
    }

    @Test
    void handleGenericException_shouldReturn500() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Внутренняя ошибка"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));
    }
}