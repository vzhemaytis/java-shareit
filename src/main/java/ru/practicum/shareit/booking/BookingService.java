package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    BookingDto createNewBooking(BookingDto bookingDto, Long bookerId);

    BookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved);

    BookingDto getBooking(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long userId, String state, Long from, Integer size);

    List<BookingDto> getOwnerBookings(Long ownerId, String state, Long from, Integer size);

    Booking checkIfBookingExist(Long id);
}
