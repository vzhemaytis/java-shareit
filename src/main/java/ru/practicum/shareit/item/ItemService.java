package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(ItemDto itemDto);
    ItemDto updateItem(ItemDto itemDto);
    ItemDto getItem(Long id);
    List<ItemDto> getItemsByOwner(Long id);
}
