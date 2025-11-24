package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithComment;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;
    private ItemDto itemDto;
    private ItemWithComment itemWithComment;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        UserDto booker = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        // FIX: Создаем ItemDto для BookingOut
        ItemDto bookingItemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        // FIX: Правильно создаем BookingOut с установленным item
        BookingOut lastBooking = new BookingOut();
        lastBooking.setId(1L);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));
        lastBooking.setBooker(booker);
        lastBooking.setStatus(Status.APPROVED);
        lastBooking.setItem(bookingItemDto); // Устанавливаем item

        itemWithComment = new ItemWithComment();
        itemWithComment.setId(1L);
        itemWithComment.setName("Test Item");
        itemWithComment.setDescription("Test Description");
        itemWithComment.setAvailable(true);
        itemWithComment.setLastBooking(lastBooking);
        itemWithComment.setNextBooking(null); // Явно устанавливаем null
        itemWithComment.setComments(Collections.emptyList());
    }

    @Test
    void create_shouldReturnCreatedItem() throws Exception {
        when(itemService.create(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        // FIX: Создаем ItemWithComment без BookingOut для простоты
        ItemWithComment simpleItemWithComment = new ItemWithComment();
        simpleItemWithComment.setId(1L);
        simpleItemWithComment.setName("Test Item");
        simpleItemWithComment.setDescription("Test Description");
        simpleItemWithComment.setAvailable(true);
        simpleItemWithComment.setLastBooking(null);
        simpleItemWithComment.setNextBooking(null);
        simpleItemWithComment.setComments(Collections.emptyList());

        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(simpleItemWithComment);

        mockMvc.perform(get("/items/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getAllItems_shouldReturnUserItems() throws Exception {
        // FIX: Создаем ItemWithComment без BookingOut для простоты
        ItemWithComment simpleItemWithComment = new ItemWithComment();
        simpleItemWithComment.setId(1L);
        simpleItemWithComment.setName("Test Item");
        simpleItemWithComment.setDescription("Test Description");
        simpleItemWithComment.setAvailable(true);
        simpleItemWithComment.setLastBooking(null);
        simpleItemWithComment.setNextBooking(null);
        simpleItemWithComment.setComments(Collections.emptyList());

        when(itemService.getAllItems(anyLong())).thenReturn(List.of(simpleItemWithComment));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        ItemDto updatedItem = ItemDto.builder()
                .id(1L)
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        when(itemService.update(anyLong(), any(ItemDto.class), anyLong())).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void search_shouldReturnFoundItems() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, 1L)
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void createComment_shouldReturnCreatedComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");
        commentDto.setCreated(LocalDateTime.now());

        when(itemService.createComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great item!"));
    }
}