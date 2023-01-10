package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    ItemDto getItem(Long id, Long userId);

    List<ItemDto> getItemsByOwner(Long id);

    List<ItemDto> search(String text);

    Item checkIfItemExist(Long id);

    void checkIfUserIsOwner(Item item, Long ownerId);
}
