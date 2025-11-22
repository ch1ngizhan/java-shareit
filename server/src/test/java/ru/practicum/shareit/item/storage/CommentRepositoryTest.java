package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentRepositoryTest {

    @Mock
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllByItemIdOrderByCreatedDesc() {
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(new Comment()));
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(1L);
        assertFalse(comments.isEmpty());
        verify(commentRepository).findAllByItemIdOrderByCreatedDesc(1L);
    }
}
