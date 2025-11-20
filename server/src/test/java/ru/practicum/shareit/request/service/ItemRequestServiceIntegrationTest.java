package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestResponseDto;
import ru.practicum.shareit.request.storage.ItemRequestService;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private UserDto requestor;
    private UserDto anotherUser;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // Create requestor
        requestor = userService.create(UserDto.builder()
                .name("Requestor")
                .email("requestor@example.com")
                .build());

        // Create another user
        anotherUser = userService.create(UserDto.builder()
                .name("Another User")
                .email("another@example.com")
                .build());

        // Create request DTO
        requestDto = new ItemRequestDto();
        requestDto.setDescription("I need a drill");
    }

    @Test
    void createRequest_shouldCreateRequestSuccessfully() {
        // When
        ItemRequestResponseDto result = itemRequestService.createRequest(requestor.getId(), requestDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("I need a drill", result.getDescription());
        assertNotNull(result.getCreated());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getUserRequests_shouldReturnUserRequests() {
        // Given
        itemRequestService.createRequest(requestor.getId(), requestDto);

        // When
        List<ItemRequestResponseDto> results = itemRequestService.getUserRequests(requestor.getId());

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("I need a drill", results.get(0).getDescription());
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        // Given
        itemRequestService.createRequest(requestor.getId(), requestDto);

        // When
        List<ItemRequestResponseDto> results = itemRequestService.getAllRequests(anotherUser.getId(), 0, 10);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void getAllRequests_shouldNotReturnOwnRequests() {
        // Given
        itemRequestService.createRequest(requestor.getId(), requestDto);

        // When
        List<ItemRequestResponseDto> results = itemRequestService.getAllRequests(requestor.getId(), 0, 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getRequestById_shouldReturnRequest() {
        // Given
        ItemRequestResponseDto createdRequest = itemRequestService.createRequest(requestor.getId(), requestDto);

        // When
        ItemRequestResponseDto result = itemRequestService.getRequestById(anotherUser.getId(), createdRequest.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdRequest.getId(), result.getId());
        assertEquals("I need a drill", result.getDescription());
    }

    @Test
    void getRequestById_shouldThrowExceptionWhenRequestNotFound() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(requestor.getId(), 999L));
    }
}