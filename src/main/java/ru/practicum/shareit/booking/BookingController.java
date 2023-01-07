package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createNewBooking(@Valid @RequestBody @NotNull BookingDto bookingDto,
                                       @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        bookingDto.setBookerId(bookerId);
        bookingDto.setStatus(BookingStatus.WAITING);
        log.info("save new item = {}", bookingDto);
        return bookingService.createNewBooking(bookingDto);
    }

}
