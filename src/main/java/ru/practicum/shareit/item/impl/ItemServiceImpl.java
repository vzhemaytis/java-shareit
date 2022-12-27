package ru.practicum.shareit.item.impl;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto addNewItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        User owner = userStorage.findUser(itemDto.getOwner());
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemStorage.addNewItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        item.setId(itemDto.getId());
        User owner = userStorage.findUser(itemDto.getOwner());
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemStorage.updateItem(item));
    }

    @Override
    public ItemDto getItem(Long id) {
        return ItemMapper.toItemDto(itemStorage.getItem(id));
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long id) {
        return itemStorage.getItemsByOwner(id)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemStorage.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
