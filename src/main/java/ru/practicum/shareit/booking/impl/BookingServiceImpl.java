package ru.practicum.shareit.booking.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exeption.BadRequestException;
import ru.practicum.shareit.exeption.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public BookingDto createNewBooking(BookingDto bookingDto) {
        Booking booking = BookingMapper.toBooking(bookingDto);
        Long itemId = bookingDto.getItemId();
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("%s with id= %s not found", Item.class.getSimpleName(), itemId));
        }
        booking.setItem(item.get());

        if (!item.get().getAvailable()) {
            throw new BadRequestException("Item is not available");
        }
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new BadRequestException("Start time is after end time");
        }

        Long bookerId = bookingDto.getBookerId();
        Optional<User> booker = userRepository.findById(bookerId);
        if (booker.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("%s with id= %s not found", User.class.getSimpleName(), bookerId));
        }
        booking.setBooker(booker.get());
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }
}
