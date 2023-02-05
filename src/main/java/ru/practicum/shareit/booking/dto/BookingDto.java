package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.validator.StartAndEndValid;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.user.dto.UserInfoDto;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TODO Sprint add-bookings.
 */
@AllArgsConstructor
@StartAndEndValid
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public ItemInfoDto getItem() {
        return item;
    }

    public void setItem(ItemInfoDto item) {
        this.item = item;
    }

    public UserInfoDto getBooker() {
        return booker;
    }

    public void setBooker(UserInfoDto booker) {
        this.booker = booker;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingDto that = (BookingDto) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BookingDto{" +
                "id=" + id +
                ", start=" + start +
                ", end=" + end +
                ", itemId=" + itemId +
                ", item=" + item +
                ", booker=" + booker +
                ", status=" + status +
                '}';
    }
}
