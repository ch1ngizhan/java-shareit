package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.*;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto create(ItemDto newItem, Long ownerId) {
        log.info("Создание новой вещи пользователем с ID: {}", ownerId);
        User owner = getUserOrThrow(ownerId);

        if (newItem == null) {
            throw new ValidationException("Item не должен быть равен null");
        }


        log.debug("BEFORE MAPPING - Incoming ItemDto - name: {}, description: {}, available: {}",
                newItem.getName(), newItem.getDescription(), newItem.getAvailable());


        if (newItem.getAvailable() == null) {
            log.warn("Available is null in incoming DTO, setting to false");
            newItem.setAvailable(false);
        }

        Item item = ItemMapper.toItem(newItem);


        if (item.getAvailable() == null) {
            log.error("CRITICAL: Item available is still null after mapping! Forcing to false.");
            item.setAvailable(false);
        }


        log.debug("AFTER MAPPING - Item available: {}", item.getAvailable());

        item.setOwner(owner);
        Item savedItem = itemStorage.save(item);


        log.debug("AFTER SAVE - Saved Item available: {}", savedItem.getAvailable());

        return ItemMapper.toItemDto(savedItem);
    }

    @Transactional
    @Override
    public void delete(Long itemId, Long userId) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        validateItemOwnership(item, userId);
        log.info("Удаление вещи с ID: {}", itemId);
        itemStorage.delete(item);
        log.debug("Вещь с ID: {} успешно удалена", itemId);
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        log.info("Обновление вещи с ID: {} пользователем с ID: {}", itemId, userId);

        Item oldItem = getItemOrThrow(itemId);

        validateItemOwnership(oldItem, userId);

        Item item = ItemMapper.toItem(itemDto);
        if (Objects.isNull(item.getAvailable())) {
            item.setAvailable(oldItem.getAvailable());
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getName() == null || item.getName().isBlank()) {
            item.setName(oldItem.getName());
        }
        item.setId(oldItem.getId());
        item.setRequest(oldItem.getRequest());
        item.setOwner(oldItem.getOwner());

        Item updatedItem = itemStorage.save(item);
        log.debug("Вещь с ID: {} успешно обновлена", itemId);
        return ItemMapper.toItemDto(updatedItem);


    }

    @Override
    public ItemWithComment getItemById(Long userId, Long itemId) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);
        List<CommentDto> commentsDto = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        BookingOut lastBooking = null;
        BookingOut nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            log.debug("User is owner, checking bookings for item {} at time {}", itemId, now);

            Booking last = bookingStorage.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                    itemId, now, Status.APPROVED).orElse(null);
            Booking next = bookingStorage.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                    itemId, now, Status.APPROVED).orElse(null);
            log.debug("Found last booking: {}, next booking: {}", last, next);

            lastBooking = last != null ? BookingMapper.toBookingOut(last) : null;
            nextBooking = next != null ? BookingMapper.toBookingOut(next) : null;
        } else {
            log.debug("User is not owner, not showing booking information");
        }
        return ItemMapper.toItemWithComment(item,
                lastBooking,
                nextBooking,
                commentsDto);
    }

    @Override
    public Collection<ItemWithComment> getAllItems(Long userId) {
        log.info("Запрос всех вещей пользователя с ID: {}", userId);
        getUserOrThrow(userId);
        List<Item> items = itemStorage.findByOwnerIdOrderByIdDesc(userId);
        log.debug("Найдено {} вещей для пользователя с ID: {}", items.size(), userId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        List<Comment> allComments = commentRepository.findAllByItemIdIn(itemIds);
        Map<Long, List<CommentDto>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        return items.stream()
                .map(item -> {
                    List<CommentDto> comments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());


                    BookingOut lastBooking = null;
                    BookingOut nextBooking = null;

                    LocalDateTime nowTime = LocalDateTime.now();
                    Booking last = bookingStorage.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                            item.getId(), nowTime, Status.APPROVED).orElse(null);
                    Booking next = bookingStorage.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            item.getId(), nowTime, Status.APPROVED).orElse(null);

                    lastBooking = last != null ? BookingMapper.toBookingOut(last) : null;
                    nextBooking = next != null ? BookingMapper.toBookingOut(next) : null;

                    return ItemMapper.toItemWithComment(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        if (text == null || text.trim().isEmpty()) {
            log.debug("Пустой поисковый запрос, возвращен пустой список");
            return Collections.emptyList();
        }
        String searchText = text.toLowerCase();
        Collection<Item> items = itemStorage.search(searchText);
        log.debug("Найдено {} вещей по запросу: '{}'", items.size(), text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        if (!bookingStorage.existsApprovedBooking(itemId, userId, Status.APPROVED)) {
            throw new IllegalArgumentException("Пользователь не брал эту вещь в аренду");
        }
        Comment comment = CommentMapper.toComment(commentDto, user, item);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Item getItemOrThrow(Long id) {
        log.debug("Поиск вещи с ID: {}", id);
        return itemStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID: {} не найдена", id);
                    return new NotFoundException("Вещь с id " + id + " не найдена");
                });
    }

    private void validateItemOwnership(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID: {} не является владельцем вещи с ID: {}", userId, item.getId());
            throw new AccessDeniedException("Пользователь не является владельцем вещи.");
        }
    }

    private User getUserOrThrow(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID: {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найдена");
                });
    }
}