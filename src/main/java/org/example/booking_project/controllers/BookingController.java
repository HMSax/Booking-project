package org.example.booking_project.controllers;

import jakarta.mail.MessagingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.example.booking_project.Dtos.BookingDTO;
import org.example.booking_project.Dtos.CustomerDTO;
import org.example.booking_project.Dtos.MiniBookingDTO;
import org.example.booking_project.Dtos.RoomDTO;
import org.example.booking_project.service.BookingService;
import org.example.booking_project.service.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Controller
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingServiceImpl bs;
    private final RoomServiceImpl rs;
    private final CustomerServiceImpl cs;
    private final BlacklistedServiceImpl bls;
    private final EmailTemplateServiceImpl ets;

    public BookingController(RoomServiceImpl rs, CustomerServiceImpl cs, BookingServiceImpl bs, BookingService bookingService, BlacklistedServiceImpl bls, EmailTemplateServiceImpl ets) {
        this.rs = rs;
        this.cs = cs;
        this.bs = bs;
        this.bls = bls;
        this.ets = ets;
    }

    @GetMapping("/bookings/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteBooking(@RequestParam Long id, Model model) {
        bs.deleteBooking(id);
        model.addAttribute("deleted", true);
        return "booking";
    }

    @PostMapping("/bookings/update")
    @PreAuthorize("isAuthenticated()")
    public String updateBooking(@ModelAttribute BookingDTO bookingDTO, Model model) {

        String response = bs.updateBooking(bookingDTO.getId(), bookingDTO);

        switch (response) {
            case "BedsError" -> {
                model.addAttribute("errorMessage", "Antalet bokade sängar överskrider det tillgängliga antalet sängar för detta rum.");
                return "booking";
            }
            case "DateError" -> {
                model.addAttribute("errorMessage", "Det går inte att boka valda datum");
                return "booking";
            }
            case "Error" -> {
                model.addAttribute("errorMessage", "Bokningsnumret existerar ej");
                return "booking";
            }
            default -> {
                model.addAttribute("updated", true);
                return "booking";
            }
        }
    }

    @PostMapping("/bookings/search")
    @PreAuthorize("isAuthenticated()")
    public String searchBooking(@RequestParam String bookingNr, Model model) {
        if (bs.existsBookingByBookingNr(bookingNr)) {
            BookingDTO bookingDTO = bs.getBookingByBookingNr(bookingNr);
            model.addAttribute("booking", bookingDTO);
            model.addAttribute("bookingNotFound", false);
            model.addAttribute("bookingFormToggle", true);
        } else {
            model.addAttribute("bookingNotFound", true);
            model.addAttribute("bookingFormToggle", false);
        }
        return "booking";
    }

    @GetMapping("/bookings/search")
    @PreAuthorize("isAuthenticated()")
    public String showSearchBookingPage(Model model) {
        model.addAttribute("booking", new BookingDTO());
        model.addAttribute("bookingFormToggle", false);
        model.addAttribute("bookingNotFound", false);
        model.addAttribute("updated", false);
        model.addAttribute("deleted", false);
        model.addAttribute("created", false);
        return "booking";
    }

    @RequestMapping("/book")
    @PreAuthorize("isAuthenticated()")
    public String book(Model model) {
        model.addAttribute("booking", new MiniBookingDTO());
        return "searchAvailability.html";
    }

    @RequestMapping("bookReceival")
    @PreAuthorize("isAuthenticated()")
    public String bookReceival(@ModelAttribute MiniBookingDTO booking, Model model) {

        model.addAttribute("book", booking);
        List<RoomDTO> listOfRooms = rs.availableRooms(booking.getCheckInDate(), booking.getCheckOutDate(), booking.getBookedBeds());
        model.addAttribute("listOfRooms", listOfRooms);
        model.addAttribute("customer", new CustomerDTO());

        return "searchAvailabilityResult.html";
    }

    @RequestMapping("createbooking")
    @PreAuthorize("isAuthenticated()")
    public String createBooking(@ModelAttribute MiniBookingDTO booking,
                                @ModelAttribute CustomerDTO customer,
                                @RequestParam String roomNumber,
                                Model model) throws IOException, MessagingException {

        model.addAttribute("book", booking);
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("customer", customer);
        List<RoomDTO> listOfRooms = rs.availableRooms(booking.getCheckInDate(), booking.getCheckOutDate(), booking.getBookedBeds());
        model.addAttribute("listOfRooms", listOfRooms);

        if(bls.checkIfCstBlacklisted(customer.getEmail())){
            model.addAttribute("blacklistedCustomer", true);
            return "searchAvailabilityResult.html";
        }

        if (customer.getCustomerName() != null && customer.getPhoneNumber() != null) {
            customer.setCustomerNumber(cs.generateCustomerNr());
            cs.addCustomer(customer);
        }

        if (cs.existsCustomerByEmail(customer.getEmail())) {
            BookingDTO bdto = bs.addBooking(customer, booking, roomNumber);
            model.addAttribute("booking", bdto);

            ets.sendBookingConfirmationEmail(customer.getEmail(),
                                            bdto.getCustomer().getCustomerName(),
                                            bdto.getCustomer().getPhoneNumber(),
                                            bdto.getCheckInDate(),
                                            bdto.getCheckOutDate(),
                                            roomNumber,
                                            bdto.getBookingNr(),
                                            bs.calculatePrice(bdto)
                                            );

            model.addAttribute("totalPrice",String.format("%.2f", bs.calculatePrice(bdto)));
            model.addAttribute("created", true);

            return "bookingConfirmation.html";
        }

        model.addAttribute("missingCustomer", true);

        return "searchAvailabilityResult.html";
    }

    @DeleteMapping("/bookings/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteBooking(@PathVariable Long id) {
        bs.deleteBooking(id);
        return "Removed bookings";
    }

    @RequestMapping("bookings")
    @PreAuthorize("isAuthenticated()")
    List<BookingDTO> getAllBookings() {
        return bs.getAllBookings();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public static String handleConstraintViolationException(ConstraintViolationException ex, Model model) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        StringBuilder errorMessage = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            errorMessage.append(violation.getMessage()).append(". \n");
        }
        model.addAttribute("errorMessage", errorMessage.toString());
        return "errorCVE";
    }
}
