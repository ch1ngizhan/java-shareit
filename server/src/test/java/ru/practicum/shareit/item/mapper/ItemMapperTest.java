package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithComment;
import ru.practicum.shareit.user.model.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_shouldConvertItemToDto() {
        // Given
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        // When
        ItemDto result = ItemMapper.toItemDto(item);

        // Then
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
    }

    @Test
    void toItem_shouldConvertDtoToItem() {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        // When
        Item result = ItemMapper.toItem(itemDto);

        // Then
        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
    }

    @Test
    void toItem_shouldReturnNullForNullInput() {
        // When
        Item result = ItemMapper.toItem(null);

        // Then
        assertNull(result);
    }

    @Test
    void toItemWithComment_shouldConvertItemWithAllFields() {
        // Given
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        UserDto booker = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        ru.practicum.shareit.item.model.ItemDto itemDto = ru.practicum.shareit.item.model.ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        BookingOut lastBooking = new BookingOut();
        lastBooking.setId(1L);
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));
        lastBooking.setBooker(booker);
        lastBooking.setItem(itemDto);
        lastBooking.setStatus(Status.APPROVED);

        BookingOut nextBooking = new BookingOut();
        nextBooking.setId(2L);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));
        nextBooking.setBooker(booker);
        nextBooking.setItem(itemDto);
        nextBooking.setStatus(Status.WAITING);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setCreated(LocalDateTime.now());

        List<CommentDto> comments = List.of(commentDto);

        // When
        ItemWithComment result = ItemMapper.toItemWithComment(item, lastBooking, nextBooking, comments);

        // Then
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(lastBooking, result.getLastBooking());
        assertEquals(nextBooking, result.getNextBooking());
        assertEquals(comments, result.getComments());
    }

    @Test
    void toItemWithComment_shouldHandleNullBookingsAndComments() {
        // Given
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        // When
        ItemWithComment result = ItemMapper.toItemWithComment(item, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertNull(result.getComments());
    }
}