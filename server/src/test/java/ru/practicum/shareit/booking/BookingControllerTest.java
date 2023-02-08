package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.user.dto.UserInfoDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    MockMvc mockMvc;

    BookingDto bookingDto;
    LocalDateTime start;
    LocalDateTime end;
    ItemInfoDto itemInfoDto;
    UserInfoDto userInfoDto;


    @BeforeEach
    void setup() {
        start = LocalDateTime.now().plusDays(1);
        end = LocalDateTime.now().plusDays(2);
        itemInfoDto = new ItemInfoDto(1L, "name", 1L);
        userInfoDto = new UserInfoDto(1L);
        bookingDto = new BookingDto(
                1L,
                start,
                end,
                1L,
                itemInfoDto,
                userInfoDto,
                BookingStatus.WAITING
        );
    }

    @Test
    void createNewBooking_whenInvoked_thenReturnStatusOkAndBookingJson() throws Exception {
        when(bookingService.createNewBooking(any(), any())).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
        verify(bookingService, times(1)).createNewBooking(any(), any());
    }

    @Test
    void approveBooking_whenInvoked_thenReturnStatusOkAndBookingJson() throws Exception {
        when(bookingService.approveBooking(any(), any(), any())).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .param("approved", String.valueOf(true))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
        verify(bookingService, times(1)).approveBooking(any(), any(), any());
    }

    @Test
    void getBookign_whenFound_thenReturnStatusOkAndBookingJson() throws Exception {
        when(bookingService.getBooking(any(), any())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .param("approved", String.valueOf(true))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
        verify(bookingService, times(1)).getBooking(any(), any());
    }

    @Test
    void getBookign_whenNotFound_thenReturnStatusNotFound() throws Exception {
        when(bookingService.getBooking(any(), any())).thenThrow(new NotFoundException("not found"));

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .param("approved", String.valueOf(true))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1)).getBooking(any(), any());
    }


    @Test
    void getUserBookings_whenInvoked_thenReturnStatusOkAndListOfBookingsJson() throws Exception {
        when(bookingService.getUserBookings(any(), any(), any(), any())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class));
        verify(bookingService, times(1)).getUserBookings(any(), any(), any(), any());
    }

    @Test
    void getOwnerBookings_whenInvoked_thenReturnStatusOkAndListOfBookingsJson() throws Exception {
        when(bookingService.getOwnerBookings(any(), any(), any(), any())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class));
        verify(bookingService, times(1)).getOwnerBookings(any(), any(), any(), any());
    }

}