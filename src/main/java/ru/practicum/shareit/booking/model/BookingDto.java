package ru.practicum.shareit.booking.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "Start cannot be null")
    @FutureOrPresent(message = "Start must be in future or present")
    private LocalDateTime start;

    @NotNull(message = "End cannot be null")
    @Future(message = "End must be in future")
    private LocalDateTime end;

    private Long booker;

    @NotNull(message = "Item ID cannot be null")
    private Long itemid;

    private Status status;
}
