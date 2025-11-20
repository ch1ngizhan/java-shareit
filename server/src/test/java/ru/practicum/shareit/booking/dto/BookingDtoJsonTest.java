package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeAndDeserialize() throws Exception {
        // Given
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItemId(1L);
        bookingDto.setStatus(Status.WAITING);

        // When - Serialize
        String jsonContent = json.write(bookingDto).getJson();

        // Then - Deserialize
        BookingDto result = objectMapper.readValue(jsonContent, BookingDto.class);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getItemId()).isEqualTo(1L);
        // ИСПРАВЛЕНИЕ: сравниваем с enum, а не со строкой
        assertThat(result.getStatus()).isEqualTo(Status.WAITING);
    }
}