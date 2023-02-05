package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.comment.dto.CommentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * TODO Sprint add-controllers.
 */
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank (message = "item name should be not blank or null")
    private String name;
    @NotBlank (message = "item description should be not blank or null")
    @Size(max = 200, message = "item description should be not longer than 200 letters")
    private String description;
    @NotNull (message = "item available should be not null")
    private Boolean available;
    private Long owner;
    private Long requestId;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
    private List<CommentDto> comments;

    public ItemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public BookingInfoDto getLastBooking() {
        return lastBooking;
    }

    public void setLastBooking(BookingInfoDto lastBooking) {
        this.lastBooking = lastBooking;
    }

    public BookingInfoDto getNextBooking() {
        return nextBooking;
    }

    public void setNextBooking(BookingInfoDto nextBooking) {
        this.nextBooking = nextBooking;
    }

    public List<CommentDto> getComments() {
        return comments;
    }

    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDto itemDto = (ItemDto) o;
        return id.equals(itemDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", available=" + available +
                ", owner=" + owner +
                ", requestId=" + requestId +
                ", comments=" + comments +
                '}';
    }
}
