package ru.practicum.shareit.item.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeption.EntityNotFoundException;
import ru.practicum.shareit.exeption.UserVerificationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ItemDto addNewItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Long ownerId = itemDto.getOwner();
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("%s with id= %s not found", User.class.getSimpleName(), ownerId));
        }
        item.setOwner(owner.get());
        return ItemMapper.toItemDto(itemRepository.saveAndFlush(item));
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item itemToUpdate = checkItem(itemDto.getId());
        checkOwner(itemDto.getId(), itemDto.getOwner());
        if (itemDto.getName() != null) {
            itemToUpdate.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            itemToUpdate.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            itemToUpdate.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.saveAndFlush(itemToUpdate));
    }

    @Transactional
    @Override
    public ItemDto getItem(Long id) {
        return ItemMapper.toItemDto(checkItem(id));
    }

    @Transactional
    @Override
    public List<ItemDto> getItemsByOwner(Long id) {
        List<Item> items = itemRepository.findAllByOwnerIdIs(id);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<ItemDto> search(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void checkOwner(Long itemId, Long ownerId) {
        if (!Objects.equals(ownerId, getItem(itemId).getOwner())) {
            throw new UserVerificationException("item can be updated only by owner");
        }
    }

    @Override
    public Item checkItem(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("%s with id= %s not found", Item.class.getSimpleName(), id));
        }
        return item.get();
    }
}
