package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.impl.BookingServiceImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exeption.BadRequestException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.exeption.UserVerificationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserService userService;
    @Mock
    ItemService itemService;
    @InjectMocks
    BookingServiceImpl bookingService;

    Booking bookingToSave;
    Booking bookingToReturn;
    Item item;
    User booker;
    User owner;
    BookingDto bookingDto;

    @BeforeEach
    void setup() {
        owner = new User(1L, "name", "email@yandex.ru");
        booker = new User(2L, "name", "email1@yandex.ru");
        item = new Item("name1", "description1", true);
        item.setId(1L);
        item.setOwner(owner);

        bookingDto = new BookingDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                null, null, null
        );

        bookingToSave = new Booking();
        bookingToSave.setId(1L);
        bookingToSave.setItem(item);
        bookingToSave.setBooker(booker);
        bookingToSave.setStatus(BookingStatus.WAITING);

        bookingToReturn = new Booking();
        bookingToReturn.setId(1L);
        bookingToReturn.setItem(item);
        bookingToReturn.setBooker(booker);
    }

    @Test
    void createNewBooking_whenInvoked_thenReturnBooking() {
        Mockito.when(itemService.checkIfItemExist(anyLong())).thenReturn(item);
        Mockito.when(userService.checkIfUserExist(anyLong())).thenReturn(booker);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(bookingToReturn);

        BookingDto savedBooking = bookingService.createNewBooking(bookingDto, 2L);
        assertEquals(1L, savedBooking.getId());
        verify(bookingRepository, atMostOnce()).save(any(Booking.class));
    }

    @Test
    void createNewBooking_whenBookerIsOwner_thenThrowsUserVerificationException() {
        Mockito.when(itemService.checkIfItemExist(anyLong())).thenReturn(item);
        Mockito.when(userService.checkIfUserExist(anyLong())).thenReturn(owner);

        assertThrows(
                UserVerificationException.class,
                () -> bookingService.createNewBooking(bookingDto, 1L)
        );
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createNewBooking_whenItemIsNotAvailable_thenThrowsBadRequestException() {
        item.setAvailable(false);
        Mockito.when(itemService.checkIfItemExist(anyLong())).thenReturn(item);
        Mockito.when(userService.checkIfUserExist(anyLong())).thenReturn(booker);

        assertThrows(
                BadRequestException.class,
                () -> bookingService.createNewBooking(bookingDto, 2L)
        );
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_whenApproved_thenReturnApprovedBooking() {
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingToSave));
        Mockito.when(userService.checkIfUserExist(1L)).thenReturn(owner);
        bookingToReturn.setStatus(BookingStatus.APPROVED);
        Mockito.when(bookingRepository.save(bookingToSave)).thenReturn(bookingToReturn);

        BookingDto approvedBooking = bookingService.approveBooking(1L, 1L, true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
        verify(bookingRepository, atMostOnce()).save(any(Booking.class));
    }

    @Test
    void approveBooking_whenRejected_thenReturnRejectedBooking() {
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingToSave));
        Mockito.when(userService.checkIfUserExist(1L)).thenReturn(owner);
        bookingToReturn.setStatus(BookingStatus.REJECTED);
        Mockito.when(bookingRepository.save(bookingToSave)).thenReturn(bookingToReturn);

        BookingDto approvedBooking = bookingService.approveBooking(1L, 1L, false);

        assertEquals(BookingStatus.REJECTED, approvedBooking.getStatus());
        verify(bookingRepository, atMostOnce()).save(any(Booking.class));
    }

    @Test
    void approveBooking_whenAlreadyRejectedOrApproved_thenThrowsBadRequestException() {
        bookingToSave.setStatus(BookingStatus.REJECTED);
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingToSave));
        Mockito.when(userService.checkIfUserExist(1L)).thenReturn(owner);

        assertThrows(
                BadRequestException.class,
                () -> bookingService.approveBooking(1L, 1L, false)
        );
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBooking_whenFound_thenReturnBookingDto() {
        Mockito.when(userService.checkIfUserExist(1L)).thenReturn(owner);
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingToSave));

        BookingDto actualBookingDto = bookingService.getBooking(1L, 1L);

        assertEquals(1L, actualBookingDto.getId());
    }

    @Test
    void getBooking_whenNotFound_thenThrowsNotFoundException() {
        Mockito.when(userService.checkIfUserExist(1L)).thenReturn(owner);
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(1L, 1L)
        );
    }

    @Test
    void getUserBookings_whenInvoked_thenReturnListOfBookings() {
        List<BookingDto> actualDto;
        Mockito.when(userService.checkIfUserExist(2L)).thenReturn(booker);

        Mockito.when(bookingRepository.findAllByBookerIdOrderByIdDesc(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getUserBookings(2L, "ALL", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllPastBookings(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getUserBookings(2L, "PAST", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllFutureBookings(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getUserBookings(2L, "FUTURE", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllCurrentBookings(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getUserBookings(2L, "CURRENT", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllWaiting(any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getUserBookings(2L, "WAITING", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllRejected(any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getUserBookings(2L, "REJECTED", 1L, 1);
        assertEquals(1, actualDto.size());
    }

    @Test
    void getUserBookings_whenWrongState_thenThrowsBadRequestException() {
        Mockito.when(userService.checkIfUserExist(2L)).thenReturn(booker);

        assertThrows(
                BadRequestException.class,
                () -> bookingService.getUserBookings(2L, "WRONG", 1L, 1)

        );
    }

    @Test
    void getOwnerBookings_whenInvoked_thenReturnListOfBookings() {
        List<BookingDto> actualDto;
        Mockito.when(userService.checkIfUserExist(1L)).thenReturn(owner);

        Mockito.when(bookingRepository.findAllOwnersBookings(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getOwnerBookings(1L, "ALL", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllOwnersPastBookings(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getOwnerBookings(1L, "PAST", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllOwnersFutureBookings(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getOwnerBookings(1L, "FUTURE", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllOwnersCurrentBookings(any(), any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getOwnerBookings(1L, "CURRENT", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllOwnersWaiting(any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getOwnerBookings(1L, "WAITING", 1L, 1);
        assertEquals(1, actualDto.size());

        Mockito.when(bookingRepository.findAllOwnersRejected(any()))
                .thenReturn(List.of(bookingToReturn));
        actualDto = bookingService.getOwnerBookings(1L, "REJECTED", 1L, 1);
        assertEquals(1, actualDto.size());
    }

    @Test
    void getOwnerBookings_whenWrongState_thenThrowsBadRequestException() {
        Mockito.when(userService.checkIfUserExist(1L)).thenReturn(owner);

        assertThrows(
                BadRequestException.class,
                () -> bookingService.getOwnerBookings(1L, "WRONG", 1L, 1)

        );
    }

    @Test
    void checkIfBookingExist_WhenNotFound_thenThrowsNotFoundException() {
        Mockito.when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.checkIfBookingExist(1L)
        );
    }
}