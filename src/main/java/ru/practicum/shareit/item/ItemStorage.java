package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addNewItem(Item item);

    Item updateItem(Item item);

    Item getItem(Long id);

    List<Item> getItemsByOwner(Long id);

    List<Item> search(String text);
}
