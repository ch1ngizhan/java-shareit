package ru.yandex.practicum.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemRequest {
    private Long id;
    private String description;
    private Long requestorID;
    private LocalDateTime created;
}
