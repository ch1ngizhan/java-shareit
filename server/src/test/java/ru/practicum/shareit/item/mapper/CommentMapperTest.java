package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommentMapperTest {

    @Test
    void toComment_shouldConvertDtoToComment() {
        // Given
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        User author = new User();
        author.setId(1L);
        author.setName("Test User");

        Item item = new Item();
        item.setId(2L);

        // When
        Comment result = CommentMapper.toComment(commentDto, author, item);

        // Then
        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        assertEquals(author, result.getAuthor());
        assertEquals(item, result.getItem());
        assertNotNull(result.getCreated());
    }

    @Test
    void toCommentDto_shouldConvertCommentToDto() {
        // Given
        User author = new User();
        author.setId(1L);
        author.setName("Test User");

        Item item = new Item();
        item.setId(2L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        // When
        CommentDto result = CommentMapper.toCommentDto(comment);

        // Then
        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getItem().getId(), result.getItemId());
        assertEquals(comment.getAuthor().getId(), result.getAuthor());
        assertEquals(comment.getAuthor().getName(), result.getAuthorName());
        assertEquals(comment.getCreated(), result.getCreated());
    }
}