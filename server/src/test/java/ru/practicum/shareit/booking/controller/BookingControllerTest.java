package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.user.model.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private BookingOut bookingOut;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        // FIX: Полностью заполняем все поля BookingOut
        UserDto booker = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        bookingOut = new BookingOut();
        bookingOut.setId(1L);
        bookingOut.setStart(bookingDto.getStart());
        bookingOut.setEnd(bookingDto.getEnd());
        bookingOut.setBooker(booker);
        bookingOut.setItem(item); // FIX: Устанавливаем item
        bookingOut.setStatus(Status.WAITING);
    }

    @Test
    void create_shouldReturnCreatedBooking() throws Exception {
        when(bookingService.create(anyLong(), any(BookingDto.class))).thenReturn(bookingOut);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void update_shouldReturnUpdatedBooking() throws Exception {
        // FIX: Создаем полностью заполненный объект для обновленного бронирования
        UserDto booker = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        BookingOut approvedBooking = new BookingOut();
        approvedBooking.setId(1L);
        approvedBooking.setStart(LocalDateTime.now().plusDays(1));
        approvedBooking.setEnd(LocalDateTime.now().plusDays(2));
        approvedBooking.setBooker(booker);
        approvedBooking.setItem(item); // FIX: Устанавливаем item
        approvedBooking.setStatus(Status.APPROVED);

        when(bookingService.update(anyLong(), anyBoolean(), anyLong())).thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking_shouldReturnBooking() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(bookingOut);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBookingsByUser_shouldReturnUserBookings() throws Exception {
        when(bookingService.getBookingsByUser(anyLong(), anyString())).thenReturn(List.of(bookingOut));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getBookingsByOwner_shouldReturnOwnerBookings() throws Exception {
        when(bookingService.getBookingsByOwner(anyLong(), anyString())).thenReturn(List.of(bookingOut));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }
}