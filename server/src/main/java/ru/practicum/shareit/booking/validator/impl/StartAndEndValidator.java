package ru.practicum.shareit.booking.validator.impl;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.validator.StartAndEndValid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StartAndEndValidator implements ConstraintValidator<StartAndEndValid, BookingDto> {
    @Override
    public boolean isValid(BookingDto bookingDto, ConstraintValidatorContext constraintValidatorContext) {
        if (bookingDto.getStart() != null && bookingDto.getEnd() != null) {
            return bookingDto.getStart().isBefore(bookingDto.getEnd());
        }
        return false;
    }
}
