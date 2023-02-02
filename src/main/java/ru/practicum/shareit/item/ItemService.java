package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    ItemDto getItem(Long id, Long userId);

    List<ItemDto> getItemsByOwner(Long id, Long from, Integer size);

    List<ItemDto> search(String text, Long from, Integer size);

    Item checkIfItemExist(Long id);

    void checkIfUserIsOwner(Item item, Long ownerId);

    CommentDto addComment(Long id, Long authorId, CommentDto commentDto);
    List<Item> getItemsByRequestIdIn(List<Long> requestIds);
    List<Item> getItemsByRequestId(Long requestId);
}
