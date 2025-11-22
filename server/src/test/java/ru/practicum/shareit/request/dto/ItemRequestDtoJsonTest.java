package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialize() throws Exception {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("I need a drill");
        requestDto.setCreated(LocalDateTime.of(2024, 1, 1, 10, 0));

        // When
        JsonContent<ItemRequestDto> result = json.write(requestDto);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("I need a drill");
        assertThat(result).extractingJsonPathStringValue("$.created").isNotNull();
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        // Given
        String jsonContent = "{\"id\":1,\"description\":\"I need a drill\",\"created\":\"2024-01-01T10:00:00\"}";

        // When
        ItemRequestDto result = objectMapper.readValue(jsonContent, ItemRequestDto.class);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("I need a drill");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }
}