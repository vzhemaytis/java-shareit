package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
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
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public ItemDto addNewItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Long ownerId = itemDto.getOwner();
        User owner = userService.checkIfUserExist(ownerId);
        item.setOwner(owner);
        Long requestId = itemDto.getRequestId();
        if (itemDto.getRequestId() == null) {
            return ItemMapper.toItemDto(itemRepository.saveAndFlush(item));
        }
        Optional<ItemRequest> itemRequest = itemRequestRepository.findById(requestId);
        if (itemRequest.isEmpty()) {
            throw new NotFoundException(String.format("request with id = %s not found", requestId));
        }
        item.setRequest(itemRequest.get());
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
                .findAllByItemIdIs(id).stream()
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
    public List<ItemDto> getItemsByOwner(Long id, Long from, Integer size) {
        int startPage = Math.toIntExact(from / size);
        PageRequest pageRequest = PageRequest.of(startPage, size);
        List<ItemDto> items = itemRepository
                .findAllByOwnerIdIsOrderById(id, pageRequest).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        List<Long> itemIds = items.stream().map(ItemDto::getId).collect(Collectors.toList());
        List<Comment> comments = commentRepository.findAllByItemIdIn(itemIds);
        List<Booking> bookings = bookingRepository.findAllByItemIdIn(itemIds);
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
    public List<ItemDto> search(String text, Long from, Integer size) {
        if (text.isEmpty()) {
            return List.of();
        }
        int startPage = Math.toIntExact(from / size);
        PageRequest pageRequest = PageRequest.of(startPage, size);
        return itemRepository.search(text, pageRequest).stream()
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

    @Transactional
    @Override
    public List<Item> getItemsByRequestIdIn(List<Long> requestIds) {
        return itemRepository.findAllByRequestIdIn(requestIds);
    }

    @Transactional
    @Override
    public List<Item> getItemsByRequestId(Long requestId) {
        return itemRepository.findAllByRequestId(requestId);
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
