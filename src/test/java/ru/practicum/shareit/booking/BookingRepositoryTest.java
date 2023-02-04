package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    BookingRepository bookingRepository;

    Booking bookingToSave;
    Booking lastBooking;
    Booking nextBooking;
    User booker;
    User owner;
    Item itemToSave;
    Item item1;
    Item item2;


    @BeforeEach
    void setup() {
        booker = userRepository.save(new User(null, "user 1", "user1@email"));
        owner = userRepository.save(new User(null, "user 2", "user2@email"));

        itemToSave = new Item("name1", "description1", true);
        itemToSave.setOwner(owner);
        item1 = itemRepository.save(itemToSave);

        itemToSave = new Item("name2", "description2", true);
        itemToSave.setOwner(owner);
        item2 = itemRepository.save(itemToSave);

        bookingToSave = new Booking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                BookingStatus.APPROVED);
        bookingToSave.setBooker(booker);
        bookingToSave.setItem(item1);
        lastBooking = bookingRepository.save(bookingToSave);

        bookingToSave = new Booking(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1),
                BookingStatus.WAITING);
        bookingToSave.setBooker(booker);
        bookingToSave.setItem(item1);
        nextBooking = bookingRepository.save(bookingToSave);
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findFirstByItemIdIsAndEndIsBeforeOrderByEndDesc_whenFound_thenReturnBooking() {
        Optional<Booking> booking = bookingRepository
                .findFirstByItemIdIsAndEndIsBeforeOrderByEndDesc(item1.getId(), LocalDateTime.now());
        assertTrue(booking.isPresent());
    }

    @Test
    void findFirstByItemIdIsAndEndIsBeforeOrderByEndDesc_whenNotFound_thenReturnEmptyOptional() {
        Optional<Booking> booking = bookingRepository
                .findFirstByItemIdIsAndEndIsBeforeOrderByEndDesc(item2.getId(), LocalDateTime.now());
        assertTrue(booking.isEmpty());
    }

    @Test
    void findFirstByItemIdIsAndStartIsAfterOrderByStartAsc_whenFound_thenReturnBooking() {
        Optional<Booking> booking = bookingRepository
                .findFirstByItemIdIsAndStartIsAfterOrderByStartAsc(item1.getId(), LocalDateTime.now());
        assertTrue(booking.isPresent());
    }

    @Test
    void findFirstByItemIdIsAndStartIsAfterOrderByStartAsc_whenNotFound_thenReturnEmptyOptional() {
        Optional<Booking> booking = bookingRepository
                .findFirstByItemIdIsAndStartIsAfterOrderByStartAsc(item2.getId(), LocalDateTime.now());
        assertTrue(booking.isEmpty());
    }

    @Test
    void findAllByBookerIdOrderByIdDesc_whenFound_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository
                .findAllByBookerIdOrderByIdDesc(booker.getId(), PageRequest.of(0, 10));

        assertEquals(2, actualBookings.size());
    }

    @Test
    void findAllByBookerIdOrderByIdDesc_whenNotFound_thenReturnEmptyList() {
        List<Booking> actualBookings = bookingRepository
                .findAllByBookerIdOrderByIdDesc(owner.getId(), PageRequest.of(0, 10));

        assertTrue(actualBookings.isEmpty());
    }

    @Test
    void findAllByItemIdIn_whenFound_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository
                .findAllByItemIdIn(List.of(item1.getId()));

        assertEquals(2, actualBookings.size());
    }

    @Test
    void findAllByItemIdIn_whenNotFound_thenReturnEmptyList() {
        List<Booking> actualBookings = bookingRepository
                .findAllByItemIdIn(List.of(item2.getId()));

        assertTrue(actualBookings.isEmpty());
    }

    @Test
    void findAllRejected_whenInvoked_thenReturnListOfBookings() {
        lastBooking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(lastBooking);

        List<Booking> actualBookings = bookingRepository.findAllRejected(booker.getId());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllWaiting_whenInvoked_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository.findAllWaiting(booker.getId());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllFutureBookings_whenInvoked_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository.findAllFutureBookings(booker.getId(), LocalDateTime.now());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllPastBookings_whenInvoked_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository.findAllPastBookings(booker.getId(), LocalDateTime.now());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllCurrentBookings_whenInvoked_thenReturnListOfBookings() {
        nextBooking.setStart(LocalDateTime.now().minusDays(1));
        bookingRepository.save(nextBooking);

        List<Booking> actualBookings = bookingRepository.findAllCurrentBookings(booker.getId(), LocalDateTime.now());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllOwnersBookings_whenInvoked_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository
                .findAllOwnersBookings(owner.getId(), PageRequest.of(0, 10));
        assertEquals(2, actualBookings.size());
    }

    @Test
    void findAllOwnersPastBookings_whenInvoked_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository
                .findAllOwnersPastBookings(owner.getId(), LocalDateTime.now());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllOwnersFutureBookings_whenInvoked_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository
                .findAllOwnersFutureBookings(owner.getId(), LocalDateTime.now());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllOwnersCurrentBookings_whenInvoked_thenReturnListOfBookings() {
        nextBooking.setStart(LocalDateTime.now().minusDays(1));
        bookingRepository.save(nextBooking);

        List<Booking> actualBookings = bookingRepository
                .findAllOwnersCurrentBookings(owner.getId(), LocalDateTime.now());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllOwnersRejected_whenInvoked_thenReturnListOfBookings() {
        lastBooking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(lastBooking);

        List<Booking> actualBookings = bookingRepository
                .findAllOwnersRejected(owner.getId());
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findAllOwnersWaiting_whenInvoked_thenReturnListOfBookings() {
        List<Booking> actualBookings = bookingRepository
                .findAllOwnersWaiting(owner.getId());
        assertEquals(1, actualBookings.size());
    }

}