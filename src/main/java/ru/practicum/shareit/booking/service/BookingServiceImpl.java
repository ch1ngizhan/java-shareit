package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDto;
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
    public BookingDto create(Long bookerId, BookingDto bookingDto) {
        Item item = getItemOrThrow(bookingDto.getItemId());
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available");
        }
        User user = getUserOrThrow(bookerId);
        Booking booking = BookingMapper.toBooking(bookingDto,item,user);
        bookingStorage.save(booking);
        return BookingMapper.toBookingDto(booking);
    }
    @Transactional
    @Override
    public BookingDto update(Long bookerId, Boolean approved, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (booking.getItem().getOwner().getId().equals(bookerId)) {
            throw new IllegalArgumentException("Booking id=" + bookingId + " нельзя одобрить: пользователь не владелец");
        }
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        bookingStorage.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (booking.getItem().getOwner().getId().equals(userId) || booking.getBooker().getId().equals(userId)) {
            throw new AccessDeniedException("Booking id=" + bookingId + " недоступен для пользователя id=" + userId);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByUser(Long userId, String state) {
        List<Booking> bookings = bookingStorage.findByBookerIdOrderByStartDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> switch (state.toUpperCase()) {
                    case "CURRENT" -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
                    case "PAST" -> booking.getEnd().isBefore(now);
                    case "FUTURE" -> booking.getStart().isAfter(now);
                    case "WAITING" -> booking.getStatus() == Status.WAITING;
                    case "REJECTED" -> booking.getStatus() == Status.REJECTED;
                    default -> true;
                })
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long userId, String state) {
        User user = getUserOrThrow(userId);
        List<Item> items = itemStorage.findByOwnerId(user.getId());
        if (items.isEmpty()) {
            throw new AccessDeniedException("У пользователя id=" + userId + " нет вещей для бронирований");
        }
        List<Booking> bookings = bookingStorage.findByBookerIdOrderByStartDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> switch (state.toUpperCase()) {
                    case "CURRENT" -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
                    case "PAST" -> booking.getEnd().isBefore(now);
                    case "FUTURE" -> booking.getStart().isAfter(now);
                    case "WAITING" -> booking.getStatus() == Status.WAITING;
                    case "REJECTED" -> booking.getStatus() == Status.REJECTED;
                    default -> true;
                })
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());

    }

    private User getUserOrThrow(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID: {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найдена");
                });
    }
    private Item getItemOrThrow(Long id) {
        log.debug("Поиск вещи с ID: {}", id);
        return itemStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID: {} не найдена", id);
                    return new NotFoundException("Вещь с id " + id + " не найдена");
                });
    }
    private Booking getBookingOrThrow(Long id) {
        log.debug("Поиск бронирования с ID: {}", id);
        return bookingStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Бронирование с ID: {} не найдено", id);
                    return new NotFoundException("Бронирование с id " + id + " не найдено");
                });
    }
}
