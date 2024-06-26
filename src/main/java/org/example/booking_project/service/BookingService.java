package org.example.booking_project.service;

import jakarta.mail.MessagingException;
import org.example.booking_project.Dtos.BookingDTO;
import org.example.booking_project.Dtos.CustomerDTO;
import org.example.booking_project.Dtos.MiniBookingDTO;
import org.example.booking_project.Dtos.RoomDTO;
import org.example.booking_project.models.Booking;
import org.example.booking_project.models.Customer;
import org.example.booking_project.models.Room;

import java.time.LocalDate;
import java.util.List;


public interface BookingService {

    public BookingDTO bookingToBookingDTO(Booking b);

    public Booking bookingDTOToBooking(BookingDTO b, Customer c, Room r);

    public double calculatePrice(BookingDTO b);

    public long bookedNightsLastYear(CustomerDTO customer);

    public BookingDTO addBooking(CustomerDTO customerDTO, MiniBookingDTO miniBookingDTO, String roomNumber);

    public String generateBookingNr();

    public String updateBooking(Long id, BookingDTO bookingDTO);

    public void deleteBooking(Long id);

    public boolean existsBookingByBookingNr(String bookingNr);

    public BookingDTO getBookingByBookingNr(String bookingNr);
    public boolean checkAvailabilityInRoom(Long bookingID, Long roomID, LocalDate checkIn, LocalDate checkOut);
    public List<BookingDTO> getAllBookings();

}
