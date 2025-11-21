package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerValidationTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnBadRequestForInvalidDates() throws Exception {
        // Given
        BookingDto invalidBookingDto = new BookingDto();
        invalidBookingDto.setItemId(1L);
        invalidBookingDto.setStart(LocalDateTime.now().plusDays(2)); // Start after end
        invalidBookingDto.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingService.create(anyLong(), any(BookingDto.class)))
                .thenThrow(new IllegalArgumentException("Start time must be before end time"));

        // When & Then
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_shouldReturnNotFoundForNonExistentItem() throws Exception {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(999L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.create(anyLong(), any(BookingDto.class)))
                .thenThrow(new NotFoundException("Вещь с id 999 не найдена"));

        // When & Then
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void update_shouldReturnForbiddenForNonOwner() throws Exception {
        // Given
        when(bookingService.update(anyLong(), anyBoolean(), anyLong()))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        // When & Then
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 2L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getBooking_shouldReturnForbiddenForUnauthorizedUser() throws Exception {
        // Given
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Booking недоступен для пользователя"));

        // When & Then
        mockMvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 3L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getBookingsByUser_shouldReturnBadRequestForInvalidState() throws Exception {
        // Given
        when(bookingService.getBookingsByUser(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Unknown state: INVALID_STATE"));

        // When & Then
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getBookingsByOwner_shouldReturnBadRequestForInvalidState() throws Exception {
        // Given
        when(bookingService.getBookingsByOwner(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Unknown state: INVALID_STATE"));

        // When & Then
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllEndpoints_shouldReturnBadRequestForMissingUserIdHeader() throws Exception {
        // Test missing user header for various endpoints
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }
}