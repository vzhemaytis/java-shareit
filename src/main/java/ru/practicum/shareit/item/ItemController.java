package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addNewItem(@Valid @RequestBody @NotNull ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long owner) {
        itemDto.setOwner(owner);
        log.info("save new item = {}", itemDto);
        return itemService.addNewItem(itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@PathVariable("id") Long id,
                              @RequestBody @NotNull ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long owner) {
        itemDto.setId(id);
        itemDto.setOwner(owner);
        log.info("update item = {}", itemDto);
        return itemService.updateItem(itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable("id") Long id) {
        log.info("get item with id = {}", id);
        return itemService.getItem(id);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long owner) {
        log.info("get items with owner id = {}", owner);
        return itemService.getItemsByOwner(owner);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(name = "text") String text) {
        log.info("search items name or desc contains = {}", text);
        return itemService.search(text);
    }

}
