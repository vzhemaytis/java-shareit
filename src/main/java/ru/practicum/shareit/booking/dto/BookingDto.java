package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.user.dto.UserInfoDto;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
public class BookingDto {
    private Long id;
    @NotNull(message = "start date should be not null")
    @Future(message = "start time should be in future")
    private LocalDateTime start;
    @NotNull(message = "end date should be not null")
    @Future(message = "end time should be in future")
    private LocalDateTime end;
    @NotNull(message = "item id should be not null")
    private Long itemId;
    private ItemInfoDto item;
    private UserInfoDto booker;
    private BookingStatus status;
}
