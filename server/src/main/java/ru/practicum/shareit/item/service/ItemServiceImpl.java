package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
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
            throw new NotFoundException("Item не должен быть равен null");
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
        log.info("Начало получения всех вещей для пользователя с ID: {}", userId);

        getUserOrThrow(userId);
        log.debug("Пользователь с ID {} существует и проверен", userId);

        List<Item> items = itemStorage.findByOwnerIdOrderByIdDesc(userId);
        log.debug("Найдено {} вещей для пользователя с ID: {}", items.size(), userId);

        if (items.isEmpty()) {
            log.info("Для пользователя с ID {} не найдено ни одной вещи. Возвращаем пустой список", userId);
            return Collections.emptyList();
        }

        log.debug("Получаем ID всех найденных вещей");
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        log.debug("Получено {} ID вещей", itemIds.size());

        LocalDateTime now = LocalDateTime.now();
        log.debug("Текущее время для фильтрации бронирований: {}", now);

        log.debug("Загружаем комментарии для всех вещей");
        List<Comment> allComments = commentRepository.findAllByItemIdIn(itemIds);
        log.debug("Загружено {} комментариев для {} вещей", allComments.size(), itemIds.size());

        Map<Long, List<CommentDto>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));
        log.debug("Комментарии сгруппированы по ID вещей");

        log.debug("Загружаем последние бронирования (завершенные до текущего момента)");
        List<Booking> allLastBookings = bookingStorage.findByItemIdInAndEndBeforeAndStatusOrderByEndDesc(
                itemIds, now, Status.APPROVED);
        log.debug("Найдено {} последних бронирований", allLastBookings.size());

        log.debug("Загружаем следующие бронирования (начинающиеся после текущего момента)");
        List<Booking> allNextBookings = bookingStorage.findByItemIdInAndStartAfterAndStatusOrderByStartAsc(
                itemIds, now, Status.APPROVED);
        log.debug("Найдено {} следующих бронирований", allNextBookings.size());

        log.debug("Создаем маппинги бронирований по ID вещей");
        Map<Long, Booking> lastBookingsMap = createLastBookingsMap(allLastBookings);
        Map<Long, Booking> nextBookingsMap = createNextBookingsMap(allNextBookings);
        log.debug("Маппинги созданы: {} последних и {} следующих бронирований",
                lastBookingsMap.size(), nextBookingsMap.size());

        log.debug("Начинаем преобразование вещей в DTO с комментариями и бронированиями");
        Collection<ItemWithComment> result = items.stream()
                .map(item -> {
                    log.trace("Обрабатываем вещь ID: {}, название: {}", item.getId(), item.getName());

                    List<CommentDto> comments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    log.trace("Для вещи ID: {} найдено {} комментариев", item.getId(), comments.size());

                    BookingOut lastBooking = lastBookingsMap.containsKey(item.getId()) ?
                            BookingMapper.toBookingOut(lastBookingsMap.get(item.getId())) : null;
                    BookingOut nextBooking = nextBookingsMap.containsKey(item.getId()) ?
                            BookingMapper.toBookingOut(nextBookingsMap.get(item.getId())) : null;

                    log.trace("Для вещи ID: {} - последнее бронирование: {}, следующее бронирование: {}",
                            item.getId(), lastBooking != null ? "есть" : "нет", nextBooking != null ? "есть" : "нет");

                    return ItemMapper.toItemWithComment(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());

        log.info("Успешно получено {} вещей с комментариями для пользователя ID: {}",
                result.size(), userId);
        return result;
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

    private Map<Long, Booking> createLastBookingsMap(List<Booking> allLastBookings) {
        Map<Long, Booking> result = new HashMap<>();

        for (Booking booking : allLastBookings) {
            Long itemId = booking.getItem().getId();
            // Если еще нет брони для этого предмета или текущее бронирование позже
            if (!result.containsKey(itemId) ||
                    booking.getEnd().isAfter(result.get(itemId).getEnd())) {
                result.put(itemId, booking);
            }
        }

        return result;
    }

    private Map<Long, Booking> createNextBookingsMap(List<Booking> allNextBookings) {
        Map<Long, Booking> result = new HashMap<>();

        for (Booking booking : allNextBookings) {
            Long itemId = booking.getItem().getId();
            // Если еще нет брони для этого предмета или текущее бронирование раньше
            if (!result.containsKey(itemId) ||
                    booking.getStart().isBefore(result.get(itemId).getStart())) {
                result.put(itemId, booking);
            }
        }

        return result;
    }
}