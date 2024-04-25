package org.example.booking_project.service.impl;

import org.example.booking_project.Dtos.BookingDTO;
import org.example.booking_project.models.Booking;
import org.example.booking_project.repos.BookingRepo;
import org.example.booking_project.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class BookingServiceImpl implements BookingService {

/*    private Long id;
    private String bookingNr;
    private Customer customer;
    private Room room;
    private int bookedBeds;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;*/
    @Override
    public BookingDTO bookingToBookingDTO(Booking b) {
        return BookingDTO.builder().id(b.getId()).bookingNr(b.getBookingNr()).customer(b.getCustomer()).room(b.getRoom())
                .bookedBeds(b.getBookedBeds()).checkInDate(b.getCheckInDate()).checkOutDate(b.getCheckOutDate()).build();
    }

    @Override
    public Booking bookingDTOToBooking(BookingDTO b) {
        return Booking.builder().id(b.getId()).bookingNr(b.getBookingNr()).customer(b.getCustomer()).room(b.getRoom())
                .bookedBeds(b.getBookedBeds()).checkInDate(b.getCheckInDate()).checkOutDate(b.getCheckOutDate()).build();
    }

    @Override
    public double calculatePrice(BookingDTO b) {
        return ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate()) * b.getRoom().getPricePerNight();
    }
}
