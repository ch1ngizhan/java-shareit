package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    private String status;
}