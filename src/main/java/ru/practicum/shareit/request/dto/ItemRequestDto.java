package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * TODO Sprint add-item-requests.
 */
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "request description could not be null or blank")
    private String description;
    private Long requestorId;
    private LocalDateTime created;
    private List<ItemDto> items;

    public ItemRequestDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(Long requestorId) {
        this.requestorId = requestorId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRequestDto that = (ItemRequestDto) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
