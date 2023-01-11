package ru.practicum.shareit.item.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exeption.BadRequestException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
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
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto addNewItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Long ownerId = itemDto.getOwner();
        User owner = userService.checkIfUserExist(ownerId);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.saveAndFlush(item));
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item itemToUpdate = checkIfItemExist(itemDto.getId());
        checkIfUserIsOwner(itemToUpdate, itemDto.getOwner());
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
        ItemDto itemDto = ItemMapper.toItemDto(checkIfItemExist(id));
        List<CommentDto> comments = commentRepository
                .findAllByItem_IdIs(id).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemDto.setComments(comments);
        if (!Objects.equals(itemDto.getOwner(), userId)) {
            return itemDto;
        }
        return addBookings(itemDto);
    }

    @Transactional
    @Override
    public List<ItemDto> getItemsByOwner(Long id) {
        List<ItemDto> items = itemRepository
                .findAllByOwnerIdIsOrderById(id).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        List<Long> itemIds = items.stream().map(ItemDto::getId).collect(Collectors.toList());
        List<Comment> comments = commentRepository.findAllByItem_IdIn(itemIds);
        List<Booking> bookings = bookingRepository.findAllByItem_IdIn(itemIds);
        return items.stream()
                .map(i -> findComments(i, comments))
                .map(i -> setLastAndNextBookings(i, bookings))
                .collect(Collectors.toList());
    }

    private ItemDto setLastAndNextBookings(ItemDto itemDto, List<Booking> bookings) {
        Optional<Booking> lastBooking = bookings.stream()
                .filter(b -> b.getItem().getId().equals(itemDto.getId()))
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now())).max(Comparator.comparing(Booking::getEnd));
        Optional<Booking> nextBooking = bookings.stream()
                .filter(b -> b.getItem().getId().equals(itemDto.getId()))
                .filter(b -> b.getStart().isAfter(LocalDateTime.now())).min(Comparator.comparing(Booking::getStart));
        itemDto.setLastBooking(lastBooking.map(BookingMapper::toBookingInfoDto).orElse(null));
        itemDto.setNextBooking(nextBooking.map(BookingMapper::toBookingInfoDto).orElse(null));
        return itemDto;
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
    public void checkIfUserIsOwner(Item item, Long ownerId) {
        if (!Objects.equals(ownerId, item.getOwner().getId())) {
            throw new NotFoundException("item can be updated only by owner");
        }
    }

    @Transactional
    @Override
    public Item checkIfItemExist(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new NotFoundException(
                    String.format("%s with id= %s not found", Item.class.getSimpleName(), id));
        }
        return item.get();
    }

    @Transactional
    @Override
    public CommentDto addComment(Long id, Long authorId, CommentDto commentDto) {
        User author = userService.checkIfUserExist(authorId);
        Item item = checkIfItemExist(id);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> pastBookings = bookingRepository.findAllPastBookings(author.getId(), now);
        Optional<Booking> booking = pastBookings.stream()
                .filter(b -> b.getItem().getId().equals(item.getId()))
                .findFirst();
        if (booking.isEmpty()) {
            throw new BadRequestException("user can't comment item without past bookings");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        return CommentMapper.toCommentDto(commentRepository.saveAndFlush(comment));
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

    private ItemDto findComments(ItemDto itemDto, List<Comment> comments) {
        List<CommentDto> commentsToAdd = comments.stream()
                .filter(comment -> comment.getItem().getId().equals(itemDto.getId()))
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemDto.setComments(commentsToAdd);
        return itemDto;
    }
}
