package ru.practicum.shareit.item.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exeption.BadRequestException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    UserService userService;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @InjectMocks
    ItemServiceImpl itemService;

    Item itemToSave;
    Item itemToReturn;
    User owner;
    User otherUser;
    ItemDto itemDto;
    Comment comment;
    LocalDateTime now;
    Booking lastBooking;
    Booking nextBooking;
    ItemRequest request;

    @BeforeEach
    void setup() {
        now = LocalDateTime.now();

        owner = new User(1L, "name", "email@yandex.ru");
        otherUser = new User(2L, "name", "email1@yandex.ru");

        itemToSave = new Item("name1", "description1", true);
        itemToSave.setId(1L);
        itemToSave.setOwner(owner);

        itemToReturn = new Item("name1", "description1", true);
        itemToReturn.setId(1L);
        itemToReturn.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setName("name1");
        itemDto.setDescription("description1");
        itemDto.setAvailable(true);
        itemDto.setOwner(owner.getId());

        comment = new Comment();
        comment.setId(1L);
        comment.setItem(itemToReturn);
        comment.setAuthor(otherUser);

        lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setStart(now.minusDays(2));
        lastBooking.setEnd(now.minusDays(1));
        lastBooking.setBooker(otherUser);
        lastBooking.setItem(itemToReturn);
        lastBooking.setStatus(BookingStatus.APPROVED);

        nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setStart(now.plusDays(1));
        nextBooking.setEnd(now.plusDays(2));
        nextBooking.setBooker(otherUser);
        nextBooking.setItem(itemToReturn);
        nextBooking.setStatus(BookingStatus.APPROVED);

        request = new ItemRequest();
        request.setId(1L);
        request.setRequestor(otherUser);
    }

    @Test
    void addNewItem_whenNoRequest_thenReturnItemDto() {
        Mockito.when(userService.checkIfUserExist(owner.getId())).thenReturn(owner);
        Mockito.when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(itemToSave);

        ItemDto savedItemDto = itemService.addNewItem(itemDto);

        assertAll(
                () -> assertEquals(1L, savedItemDto.getId()),
                () -> assertEquals(itemDto.getName(), savedItemDto.getName()),
                () -> assertEquals(itemDto.getDescription(), savedItemDto.getDescription()),
                () -> assertEquals(itemDto.getAvailable(), savedItemDto.getAvailable()),
                () -> assertEquals(owner.getId(), savedItemDto.getOwner()),
                () -> assertNull(savedItemDto.getRequestId()),
                () -> assertNull(savedItemDto.getLastBooking()),
                () -> assertNull(savedItemDto.getNextBooking()),
                () -> assertNull(savedItemDto.getComments())
        );
    }

    @Test
    void addNewItem_whenRequestFound_thenReturnItemDto() {
        Long requestId = 1L;
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemDto.setRequestId(requestId);
        itemToSave.setRequest(itemRequest);
        Mockito.when(userService.checkIfUserExist(owner.getId())).thenReturn(owner);
        Mockito.when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(itemToSave);
        Mockito.when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        ItemDto savedItemDto = itemService.addNewItem(itemDto);

        assertAll(
                () -> assertEquals(1L, savedItemDto.getId()),
                () -> assertEquals(itemDto.getName(), savedItemDto.getName()),
                () -> assertEquals(itemDto.getDescription(), savedItemDto.getDescription()),
                () -> assertEquals(itemDto.getAvailable(), savedItemDto.getAvailable()),
                () -> assertEquals(owner.getId(), savedItemDto.getOwner()),
                () -> assertEquals(requestId, savedItemDto.getRequestId()),
                () -> assertNull(savedItemDto.getLastBooking()),
                () -> assertNull(savedItemDto.getNextBooking()),
                () -> assertNull(savedItemDto.getComments())
        );

        verify(itemRepository).saveAndFlush(any(Item.class));
    }

    @Test
    void addNewItem_whenRequestNotFound_thenThrowsNotFoundException() {
        Long requestId = 1L;
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemDto.setRequestId(requestId);
        itemToSave.setRequest(itemRequest);
        Mockito.when(userService.checkIfUserExist(owner.getId())).thenReturn(owner);
        Mockito.when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> itemService.addNewItem(itemDto)
        );
        verify(itemRepository, never()).saveAndFlush(any(Item.class));
    }

    @Test
    void updateItem_whenNewName_thenReturnItemDtoWithNewName() {
        ItemDto itemDtoWithNewName = new ItemDto();
        itemDtoWithNewName.setId(itemToSave.getId());
        itemDtoWithNewName.setName("new name");
        itemDtoWithNewName.setOwner(owner.getId());
        Mockito.when(itemRepository.findById(itemToSave.getId())).thenReturn(Optional.of(itemToSave));
        itemToReturn.setName(itemDtoWithNewName.getName());
        Mockito.when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(itemToReturn);

        ItemDto updatedItemDto = itemService.updateItem(itemDtoWithNewName);
        assertAll(
                () -> assertEquals("new name", updatedItemDto.getName()),
                () -> assertEquals(itemToSave.getId(), updatedItemDto.getId()),
                () -> assertEquals(itemToSave.getDescription(), updatedItemDto.getDescription()),
                () -> assertEquals(itemToSave.getAvailable(), updatedItemDto.getAvailable())
        );
        verify(itemRepository).saveAndFlush(any(Item.class));
    }

    @Test
    void updateItem_whenNewDescription_thenReturnItemDtoWithNewDescription() {
        ItemDto itemDtoWithNewDescription = new ItemDto();
        itemDtoWithNewDescription.setId(itemToSave.getId());
        itemDtoWithNewDescription.setDescription("new desc");
        itemDtoWithNewDescription.setOwner(owner.getId());
        Mockito.when(itemRepository.findById(itemToSave.getId())).thenReturn(Optional.of(itemToSave));
        itemToReturn.setDescription(itemDtoWithNewDescription.getDescription());
        Mockito.when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(itemToReturn);

        ItemDto updatedItemDto = itemService.updateItem(itemDtoWithNewDescription);
        assertAll(
                () -> assertEquals(itemToSave.getName(), updatedItemDto.getName()),
                () -> assertEquals(itemToSave.getId(), updatedItemDto.getId()),
                () -> assertEquals("new desc", updatedItemDto.getDescription()),
                () -> assertEquals(itemToSave.getAvailable(), updatedItemDto.getAvailable())
        );
        verify(itemRepository).saveAndFlush(any(Item.class));
    }

    @Test
    void updateItem_whenSetNotAvailable_thenReturnItemDtoWithNotAvailable() {
        ItemDto itemDtoNotAvailable = new ItemDto();
        itemDtoNotAvailable.setId(itemToSave.getId());
        itemDtoNotAvailable.setAvailable(false);
        itemDtoNotAvailable.setOwner(owner.getId());
        Mockito.when(itemRepository.findById(itemToSave.getId())).thenReturn(Optional.of(itemToSave));
        itemToReturn.setAvailable(false);
        Mockito.when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(itemToReturn);

        ItemDto updatedItemDto = itemService.updateItem(itemDtoNotAvailable);
        assertAll(
                () -> assertEquals(itemToSave.getName(), updatedItemDto.getName()),
                () -> assertEquals(itemToSave.getId(), updatedItemDto.getId()),
                () -> assertEquals(itemToSave.getDescription(), updatedItemDto.getDescription()),
                () -> assertFalse(itemDtoNotAvailable.getAvailable())
        );
        verify(itemRepository).saveAndFlush(any(Item.class));
    }

    @Test
    void getItem_WhenUserNotOwner_ThenReturnItemDto() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(itemToReturn));
        Mockito.when(commentRepository.findAllByItem_IdIs(1L)).thenReturn(List.of(comment));

        ItemDto actualDto = itemService.getItem(1L, 2L);

        assertAll(
                () -> assertEquals(itemToReturn.getId(), actualDto.getId()),
                () -> assertEquals(1, actualDto.getComments().size()),
                () -> assertNull(actualDto.getNextBooking()),
                () -> assertNull(actualDto.getLastBooking())
        );
    }

    @Test
    void getItem_WhenUserOwner_ThenReturnItemDto() {
        Long itemId = 1L;
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToReturn));
        Mockito.when(commentRepository.findAllByItem_IdIs(itemId)).thenReturn(List.of(comment));
        Mockito.when(bookingRepository
                        .findFirstByItemIdIsAndEndIsBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        Mockito.when(bookingRepository
                        .findFirstByItemIdIsAndStartIsAfterOrderByStartAsc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));

        ItemDto actualDto = itemService.getItem(1L, 1L);

        assertAll(
                () -> assertEquals(itemToReturn.getId(), actualDto.getId()),
                () -> assertEquals(1, actualDto.getComments().size()),
                () -> assertEquals(1L, actualDto.getLastBooking().getId()),
                () -> assertEquals(2L, actualDto.getNextBooking().getId())
        );
    }

    @Test
    void getItem_WhenItemNotFound_ThenThrowsNotFoundException() {
        Long itemId = 1L;
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> itemService.getItem(1L, 1L)
        );
    }

    @Test
    void getItemsByOwner_whenNoItems_ThenReturnEmptyList() {
        Long ownerId = 1L;
        Mockito.when(itemRepository.findAllByOwnerIdIsOrderById(anyLong(), any()))
                .thenReturn(List.of());
        Mockito.when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());
        Mockito.when(bookingRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemDto> actualDtos = itemService.getItemsByOwner(ownerId, 1L, 1);

        assertTrue(actualDtos.isEmpty());
    }

    @Test
    void getItemsByOwner_whenInvoked_ThenReturnListOfItems() {
        Long ownerId = 1L;
        Mockito.when(itemRepository.findAllByOwnerIdIsOrderById(anyLong(), any()))
                .thenReturn(List.of(itemToReturn));
        Mockito.when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());
        Mockito.when(bookingRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemDto> actualDtos = itemService.getItemsByOwner(ownerId, 1L, 1);

        assertEquals(1, actualDtos.size());
    }

    @Test
    void search_whenTextIsEmpty_thenReturnEmptyList() {
        List<ItemDto> actualDtos = itemService.search("", 1L, 1);

        assertTrue(actualDtos.isEmpty());
    }

    @Test
    void search_whenInvoked_thenReturnListOfItems() {
        Mockito.when(itemRepository.search(any(), any())).thenReturn(List.of(itemToReturn));

        List<ItemDto> actualDtos = itemService.search("name", 1L, 1);

        assertEquals(1, actualDtos.size());
    }

    @Test
    void checkIfUserIsOwner_whenNotOwner_thenThrowsNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> itemService.checkIfUserIsOwner(itemToReturn, 2L)
        );
    }

    @Test
    void checkIfItemExist_whenNotFound_thenThrowsNotFoundException() {
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> itemService.checkIfItemExist(1L)
        );
    }

    @Test
    void checkIfItemExist_whenFound_thenReturnItem() {
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.of(itemToReturn));

        Item returnedItem = itemService.checkIfItemExist(1L);

        assertEquals(itemToReturn, returnedItem);
    }

    @Test
    void addComment_whenInvoked_thenReturnCommentDto() {
        Long authorId = 2L;
        Mockito.when(userService.checkIfUserExist(authorId)).thenReturn(otherUser);
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(itemToReturn));
        Mockito.when(bookingRepository.findAllPastBookings(anyLong(), any())).thenReturn(List.of(lastBooking));
        CommentDto commentToSave = new CommentDto(1L, "text", "author", LocalDateTime.now());
        Mockito.when(commentRepository.saveAndFlush(any(Comment.class))).thenReturn(comment);

        CommentDto savedComment = itemService.addComment(1L, 2L, commentToSave);
        assertEquals(1L, savedComment.getId());
    }

    @Test
    void addComment_whenNotPastBookingFound_thenThrowsBadRequestException() {
        Long authorId = 2L;
        Mockito.when(userService.checkIfUserExist(authorId)).thenReturn(otherUser);
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(itemToReturn));
        Mockito.when(bookingRepository.findAllPastBookings(anyLong(), any())).thenReturn(List.of());
        CommentDto commentToSave = new CommentDto(1L, "text", "author", LocalDateTime.now());

        assertThrows(
                BadRequestException.class,
                () -> itemService.addComment(1L, 2L, commentToSave)
        );
    }

    @Test
    void getItemsByRequestIdIn_whenInvoked_thenReturnListOfItems() {
        itemToReturn.setRequest(request);
        Mockito.when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(itemToReturn));

        List<Item> actualItems = itemService.getItemsByRequestIdIn(List.of(1L));

        assertEquals(1, actualItems.size());
    }

    @Test
    void getItemsByRequestId_whenInvoked_thenReturnListOfItems() {
        itemToReturn.setRequest(request);
        Mockito.when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(itemToReturn));

        List<Item> actualItems = itemService.getItemsByRequestId(1L);

        assertEquals(1, actualItems.size());
    }


}