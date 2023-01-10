package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_IdOrderByIdDesc(Long id);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.status = 'rejected' " +
            "order by b.id desc")
    List<Booking> findAllRejected(Long id);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.start > ?2 " +
            "order by b.id desc")
    List<Booking> findAllFutureBookings(Long id, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.end < ?2 " +
            "order by b.id desc")
    List<Booking> findAllPastBookings(Long id, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 " +
            "order by b.id desc")
    List<Booking> findAllCurrentBookings(Long id, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "order by b.id desc")
    List<Booking> findAllOwnersBookings(Long id);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.end < ?2 " +
            "order by b.id desc")
    List<Booking> findAllOwnersPastBookings(Long id, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.start > ?2 " +
            "order by b.id desc")
    List<Booking> findAllOwnersFutureBookings(Long id, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 " +
            "order by b.id desc")
    List<Booking> findAllOwnersCurrentBookings(Long id, LocalDateTime now);

    @Query(" select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = 'rejected' " +
            "order by b.id desc")
    List<Booking> findAllOwnersRejected(Long id);


}
