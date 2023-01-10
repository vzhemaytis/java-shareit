package ru.practicum.shareit.item.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public ItemDto addNewItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Long ownerId = itemDto.getOwner();
        User owner = userService.checkUser(ownerId);
        item.setOwner(owner);
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
    public ItemDto getItem(Long id, Long userId) {
        ItemDto itemDto = ItemMapper.toItemDto(checkItem(id));
        if (!Objects.equals(itemDto.getOwner(), userId)) {
            return itemDto;
        }
        return addBookings(itemDto);
    }

    @Transactional
    @Override
    public List<ItemDto> getItemsByOwner(Long id) {
        List<Item> items = itemRepository.findAllByOwnerIdIsOrderById(id);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        itemDtos.forEach(this::addBookings);
        return itemDtos;
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

    @Transactional
    @Override
    public void checkOwner(Long itemId, Long ownerId) {
        if (!Objects.equals(ownerId, checkItem(itemId).getOwner().getId())) {
            throw new NotFoundException("item can be updated only by owner");
        }
    }

    @Transactional
    @Override
    public Item checkItem(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new NotFoundException(
                    String.format("%s with id= %s not found", Item.class.getSimpleName(), id));
        }
        return item.get();
    }

    private BookingInfoDto getLastItemBooking(Long id) {
        Optional<Booking> lastBooking =
                bookingRepository.findFirstByItemIdIsAndEndIsBeforeOrderByEndDesc(id, LocalDateTime.now());
        return lastBooking.map(BookingMapper::toBookingInfoDto).orElse(null);
    }

    private BookingInfoDto getNextItemBooking(Long id) {
        Optional<Booking> nextBooking =
                bookingRepository.findFirstByItemIdIsAndStartIsAfterOrderByStartAsc(id, LocalDateTime.now());
        return nextBooking.map(BookingMapper::toBookingInfoDto).orElse(null);
    }

    private ItemDto addBookings(ItemDto itemDto) {
        BookingInfoDto lastBookingDto = getLastItemBooking(itemDto.getId());
        BookingInfoDto nextBookingDto = getNextItemBooking(itemDto.getId());
        itemDto.setLastBooking(lastBookingDto);
        itemDto.setNextBooking(nextBookingDto);
        return itemDto;
    }
}
