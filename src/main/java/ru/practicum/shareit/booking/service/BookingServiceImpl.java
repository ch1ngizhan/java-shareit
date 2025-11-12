package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.booking.model.BookingOut;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Transactional
    @Override
    public BookingOut create(Long bookerId, BookingDto bookingDto) {
        log.info("Создание нового бронирования. Пользователь ID={}, Вещь ID={}", bookerId, bookingDto.getItemId());
        if (bookingDto.getItemId() == null) {
            throw new NotFoundException("null");
        }
        Item item = getItemOrThrow(bookingDto.getItemId());

        if (!item.getAvailable()) {
            log.warn("Вещь ID={} недоступна для бронирования", item.getId());
            throw new IllegalArgumentException("Item is not available");
        }

        User user = getUserOrThrow(bookerId);
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        bookingStorage.save(booking);
        log.info("Бронирование ID={} успешно создано", booking.getId());
        return BookingMapper.toBookingOut(booking);
    }

    @Transactional
    @Override
    public BookingOut update(Long ownerId, Boolean approved, Long bookingId) {
        log.info("Обновление бронирования ID={} пользователем ID={} с approved={}", bookingId, ownerId, approved);
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("Пользователь ID={} не является владельцем вещи для бронирования ID={}", ownerId, bookingId);
            throw new AccessDeniedException("Booking id=" + bookingId + " нельзя одобрить: пользователь не владелец");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        bookingStorage.save(booking);
        log.info("Бронирование ID={} обновлено. Новый статус={}", bookingId, booking.getStatus());
        return BookingMapper.toBookingOut(booking);
    }

    @Override
    public BookingOut getBooking(Long userId, Long bookingId) {
        log.info("Получение бронирования ID={} пользователем ID={}", bookingId, userId);
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            log.warn("Пользователь ID={} не имеет доступа к бронированию ID={}", userId, bookingId);
            throw new AccessDeniedException("Booking id=" + bookingId + " недоступен для пользователя id=" + userId);
        }

        log.info("Бронирование ID={} возвращено пользователю ID={}", bookingId, userId);
        return BookingMapper.toBookingOut(booking);
    }

    @Override
    public List<BookingOut> getBookingsByUser(Long userId, String state) {
        log.info("Получение всех бронирований пользователя ID={} с фильтром state={}", userId, state);
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();

        List<BookingOut> result = bookingStorage.findByBookerIdOrderByStartDesc(userId)
                .stream()
                .filter(booking -> filterByState(booking, state, now))
                .map(BookingMapper::toBookingOut)
                .collect(Collectors.toList());

        log.info("Найдено {} бронирований для пользователя ID={} с фильтром state={}", result.size(), userId, state);
        return result;
    }

    @Override
    public List<BookingOut> getBookingsByOwner(Long ownerId, String state) {
        log.info("Получение всех бронирований владельца ID={} с фильтром state={}", ownerId, state);
        getUserOrThrow(ownerId);
        LocalDateTime now = LocalDateTime.now();

        List<BookingOut> result = bookingStorage.findAllByItemOwnerIdOrderByStartDesc(ownerId)
                .stream()
                .filter(booking -> filterByState(booking, state, now))
                .map(BookingMapper::toBookingOut)
                .collect(Collectors.toList());

        log.info("Найдено {} бронирований для владельца ID={} с фильтром state={}", result.size(), ownerId, state);
        return result;
    }

    private boolean filterByState(Booking booking, String state, LocalDateTime now) {
        return switch (state.toUpperCase()) {
            case "CURRENT" -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
            case "PAST" -> booking.getEnd().isBefore(now);
            case "FUTURE" -> booking.getStart().isAfter(now);
            case "WAITING" -> booking.getStatus() == Status.WAITING;
            case "REJECTED" -> booking.getStatus() == Status.REJECTED;
            default -> true;
        };
    }

    private User getUserOrThrow(Long id) {
        log.debug("Поиск пользователя по ID={}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь ID={} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
    }

    private Item getItemOrThrow(Long id) {
        log.debug("Поиск вещи по ID={}", id);
        return itemStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Вещь ID={} не найдена", id);
                    return new NotFoundException("Вещь с id " + id + " не найдена");
                });
    }

    private Booking getBookingOrThrow(Long id) {
        log.debug("Поиск бронирования по ID={}", id);
        return bookingStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Бронирование ID={} не найдено", id);
                    return new NotFoundException("Бронирование с id " + id + " не найдено");
                });
    }
}
