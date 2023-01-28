package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.comment.dto.CommentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank (message = "item name should be not blank or null")
    private final String name;
    @NotBlank (message = "item description should be not blank or null")
    @Size(max = 200, message = "item description should be not longer than 200 letters")
    private final String description;
    @NotNull (message = "item available should be not null")
    private final Boolean available;
    private Long owner;
    private final Long requestId;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
    private List<CommentDto> comments;
}
