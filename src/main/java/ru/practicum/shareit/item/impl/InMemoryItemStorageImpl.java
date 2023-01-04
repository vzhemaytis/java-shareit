package ru.practicum.shareit.item.impl;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exeption.EntityNotFoundException;
import ru.practicum.shareit.exeption.UserVerificationException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> items;

    public InMemoryItemStorageImpl() {
        this.items = new HashMap<>();
    }

    private Long id = 1L;

    @Override
    public Item addNewItem(Item item) {
        Long itemId = getId();
        item.setId(itemId);
        items.put(itemId, item);
        return items.get(itemId);
    }

    @Override
    public Item updateItem(Item item) {
        checkOwner(item);
        Long itemId = item.getId();
        checkItemExist(itemId);
        Item itemToUpdate = items.get(itemId);
        if (item.getName() != null) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }
        return items.get(itemId);
    }

    @Override
    public Item getItem(Long id) {
        checkItemExist(id);
        return items.get(id);
    }

    @Override
    public List<Item> getItemsByOwner(Long id) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    private Long getId() {
        return id++;
    }

    private void checkItemExist(Long id) {
        if (!items.containsKey(id)) {
            throw new EntityNotFoundException(String.format("%s with id= %s not found", Item.class, id));
        }
    }

    private void checkOwner(Item item) {
        if (!Objects.equals(item.getOwner().getId(), items.get(item.getId()).getOwner().getId())) {
            throw new UserVerificationException("item can be updated only by owner");
        }
    }
}
