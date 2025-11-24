package ru.practicum.shareit.item.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor
public class CommentMapper {

    public static Comment toComment(CommentDto commentDto, User author, Item item) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setItemId(comment.getItem().getId());
        commentDto.setAuthor(comment.getAuthor().getId());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }
}
