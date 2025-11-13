package ru.practicum.shareit.booking.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "Start cannot be null")
    private LocalDateTime start;

    @NotNull(message = "End cannot be null")
    private LocalDateTime end;

    private Long booker;

    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    private Status status;
}
