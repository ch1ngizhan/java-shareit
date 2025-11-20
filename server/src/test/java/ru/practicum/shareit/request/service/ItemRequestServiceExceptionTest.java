package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.storage.ItemRequestService;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ItemRequestServiceExceptionTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private UserDto requestor;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestor = userService.create(UserDto.builder()
                .name("Requestor")
                .email("requestor@example.com")
                .build());

        requestDto = new ItemRequestDto();
        requestDto.setDescription("I need a drill");
    }

    @Test
    void createRequest_shouldThrowNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemRequestService.createRequest(999L, requestDto));
    }

    @Test
    void getUserRequests_shouldThrowNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getUserRequests(999L));
    }

    @Test
    void getAllRequests_shouldThrowNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getAllRequests(999L, 0, 10));
    }

    @Test
    void getRequestById_shouldThrowNotFoundExceptionForNonExistentRequest() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(requestor.getId(), 999L));
    }

    @Test
    void getRequestById_shouldThrowNotFoundExceptionForNonExistentUser() {
        // Given
        var createdRequest = itemRequestService.createRequest(requestor.getId(), requestDto);

        // When & Then
        assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(999L, createdRequest.getId()));
    }
}