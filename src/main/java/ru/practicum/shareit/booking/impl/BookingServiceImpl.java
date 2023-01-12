package ru.practicum.shareit.booking.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exeption.BadRequestException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.exeption.UserVerificationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Transactional
    @Override
    public BookingDto createNewBooking(BookingDto bookingDto, Long bookerId) {
        Long itemId = bookingDto.getItemId();
        Item item = itemService.checkIfItemExist(itemId);
        User booker = userService.checkIfUserExist(bookerId);

        if (Objects.equals(item.getOwner().getId(), booker.getId())) {
            throw new UserVerificationException("booking could not be created by item owner");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Item is not available");
        }

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = checkIfBookingExist(bookingId);
        User owner = userService.checkIfUserExist(ownerId);
        Item item = booking.getItem();
        itemService.checkIfUserIsOwner(item, owner.getId());
        if (approved && !booking.getStatus().equals(BookingStatus.APPROVED)) {
            booking.setStatus(BookingStatus.APPROVED);
        } else if (!approved && !booking.getStatus().equals(BookingStatus.REJECTED)) {
            booking.setStatus(BookingStatus.REJECTED);
        } else {
            throw new BadRequestException("Booking status was already changed");
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        User user = userService.checkIfUserExist(userId);
        Booking booking = checkIfBookingExist(bookingId);
        checkAccess(user, booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        User user = userService.checkIfUserExist(userId);
        List<Booking> bookings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        BookingState bookingState = getBookingState(state);
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByIdDesc(user.getId());
                break;
            case PAST:
                bookings = bookingRepository.findAllPastBookings(user.getId(), now);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureBookings(user.getId(), now);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentBookings(user.getId(), now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllWaiting(user.getId());
                break;
            case REJECTED:
                bookings = bookingRepository.findAllRejected(user.getId());
                break;
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state) {
        User owner = userService.checkIfUserExist(ownerId);
        List<Booking> bookings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        BookingState bookingState = getBookingState(state);
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllOwnersBookings(owner.getId());
                break;
            case PAST:
                bookings = bookingRepository.findAllOwnersPastBookings(owner.getId(), now);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllOwnersFutureBookings(owner.getId(), now);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllOwnersCurrentBookings(owner.getId(), now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllOwnersWaiting(owner.getId());
                break;
            case REJECTED:
                bookings = bookingRepository.findAllOwnersRejected(owner.getId());
                break;
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Booking checkIfBookingExist(Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new NotFoundException(
                    String.format("%s with id= %s not found", Booking.class.getSimpleName(), id));
        }
        return booking.get();
    }

    private void checkAccess(User user, Booking booking) {
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (!Objects.equals(ownerId, user.getId()) && !Objects.equals(bookerId, user.getId())) {
            throw new UserVerificationException("only booker or item owner could get booking info");
        }
    }

    private BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (Throwable e) {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
    }


}
