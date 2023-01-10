package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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
        bookingDto.setStatus(BookingStatus.WAITING);
        log.info("save new item = {}", bookingDto);
        return bookingService.createNewBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                     @PathVariable("bookingId") Long bookingId,
                                     @RequestParam(name = "approved") Boolean approved) {
        log.info("try to set approved to booking with id = {} as {} by user with id = {}"
                , bookingId, approved, ownerId);
        return bookingService.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable("bookingId") Long bookingId) {
        log.info("try to get booking with id = {} by user with id = {}", bookingId, userId);
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(name = "state"
                                                    , required = false
                                                    , defaultValue = "ALL") String state) {
        log.info("get all booking of user with id = {} with state = {}", userId, state);
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                            @RequestParam(name = "state"
                                                    , required = false
                                                    , defaultValue = "ALL") String state) {
        log.info("get all bookings for items with owner with id = {} and state = {}" , ownerId, state);
        return bookingService.getOwnerBookings(ownerId, state);
    }

}
