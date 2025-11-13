package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;


@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "Start cannot be null")
    @FutureOrPresent
    private LocalDateTime start;

    @NotNull(message = "End cannot be null")
    @Future
    private LocalDateTime end;

    private Long booker;

    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    private Status status;
}
