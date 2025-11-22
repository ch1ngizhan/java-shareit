package ru.practicum.shareit.item.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private Long itemId;
    private Long author;
    private String authorName;
    private LocalDateTime created;
}
