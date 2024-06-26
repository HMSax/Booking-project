package org.example.booking_project.service.impl;

import org.example.booking_project.Dtos.BookingDTO;
import org.example.booking_project.Dtos.CustomerDTO;
import org.example.booking_project.Dtos.MiniBookingDTO;
import org.example.booking_project.Dtos.RoomDTO;
import org.example.booking_project.configs.IntegrationsProperties;
import org.example.booking_project.models.Booking;
import org.example.booking_project.models.Customer;
import org.example.booking_project.models.Room;
import org.example.booking_project.models.RoomType;
import org.example.booking_project.repos.BookingRepo;
import org.example.booking_project.repos.CustomerRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookingServiceImplTests {
    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private RoomServiceImpl roomServiceImpl;

    @Mock
    private CustomerServiceImpl customerServiceImpl;

    private BookingServiceImpl bookingService;

    @Autowired
    private IntegrationsProperties properties;

    private Customer testcustomer = new Customer((long) 123, "CN101", "Kalle",
            "012-345678", "abc@abcdef.se");
    private CustomerDTO testcustomerDTO = new CustomerDTO((long) 123, "CN101", "Kalle",
            "012-345678", "abc@abcdef.se");
    private Room testroom;
    private Room testroom2;
    private RoomDTO testroomDTO = new RoomDTO((long) 321, 101, 3, 500, RoomType.DOUBLE);
    private LocalDate checkIn = LocalDate.now().plusMonths(1);
    private LocalDate checkOut1 = LocalDate.now().plusMonths(1).plusDays(1);
    private LocalDate checkOut2 = LocalDate.now().plusMonths(1).plusDays(7);

    private Booking testbooking1;
    private Booking testbooking2;

    private BookingDTO actualBookingDTO;

    private BookingDTO testbdto1;
    private BookingDTO testbdto2;

    private MiniBookingDTO testMini = new MiniBookingDTO(2, checkIn, checkOut1);

    @BeforeEach
    void setUp(){
        bookingService = new BookingServiceImpl(bookingRepo, customerRepo,roomServiceImpl, customerServiceImpl, properties);
        testroom = new Room((long) 321, 101, RoomType.DOUBLE, 3, 500);
        testroom2 = new Room((long) 322, 102, RoomType.SINGLE, 1, 200);
        if(checkIn.getDayOfWeek().equals(DayOfWeek.SUNDAY)){
            checkIn =checkIn.minusDays(1);
            checkOut1 = checkOut1.minusDays(1);
            checkOut2 = checkOut2.minusDays(1);
        }
        testbooking1 = new Booking((long) 213, "BN101", testcustomer, testroom, 2, checkIn, checkOut1);
        testbooking2 = new Booking((long) 213, "BN101", testcustomer, testroom, 2, checkIn, checkOut2);
        testbdto1 = bookingService.bookingToBookingDTO(testbooking1);
        testbdto2 = bookingService.bookingToBookingDTO(testbooking2);
        actualBookingDTO = new BookingDTO((long) 213, "BN101", testcustomerDTO, testroomDTO, 2, checkIn, checkOut1);
    }

    @Test
    void calculatePrice() {

        double result1 = bookingService.calculatePrice(testbdto1);
        double result2 = bookingService.calculatePrice(testbdto2);

        assertEquals(500, result1);
        assertEquals(3472.55, result2);
    }

    @Test
    void bookingToBookingDTO() {
        assertEquals(actualBookingDTO, bookingService.bookingToBookingDTO(testbooking1));
    }

    @Test
    void bookingDTOToBooking() {
        assertEquals(testbooking1, bookingService.bookingDTOToBooking(actualBookingDTO, testcustomer, testroom));
    }

    @Test
    void generateBookingNr() {
        when(bookingRepo.findAll()).thenReturn(Arrays.asList(testbooking1));
        BookingServiceImpl service2 = new BookingServiceImpl(bookingRepo, customerRepo, roomServiceImpl,customerServiceImpl,properties);
        String testBookingNr = service2.generateBookingNr();
        assertEquals("BN102", testBookingNr);
    }

    @Test
    void getAllBookingTest() {
        when(bookingRepo.findAll()).thenReturn(Arrays.asList(testbooking1, testbooking2));
        List<BookingDTO> test = bookingService.getAllBookings();
        assertEquals(2, test.size());
        assertEquals(213L, testbooking1.getId());
    }

    @Test
    void addBookingTest() {
        when(customerRepo.findByEmail(testcustomerDTO.getEmail())).thenReturn(testcustomer);
        when(roomServiceImpl.getRoom(Integer.parseInt("101"))).thenReturn(testroom);
        when(bookingRepo.save(any(Booking.class))).thenReturn(null);

        BookingDTO booking = bookingService.addBooking(testcustomerDTO,testMini, "101");

        assertEquals(2, booking.getBookedBeds());
        assertEquals("abc@abcdef.se", booking.getCustomer().getEmail());
        assertEquals(LocalDate.now().plusMonths(1), booking.getCheckInDate());
    }

    @Test
    void isNumericShouldReturnTrueOnNumberAndFalseOnNumberFormatException(){
        boolean b1 = bookingService.isNumeric("123");
        boolean b2 = bookingService.isNumeric("ABC");

        assertTrue(b1);
        assertFalse(b2);
    }

    @Test
    void UpdateBookingShouldReturnBedsErrorWhenTooManyBedsInDTO(){
        Booking testbooking3 = new Booking((long) 222, "BN100", testcustomer, testroom2, 1, checkIn, checkOut2);
        when(bookingRepo.findById(testbdto1.getId())).thenReturn(Optional.of(testbooking3));

        String result = bookingService.updateBooking(testbdto1.getId(),testbdto1);

        assertEquals("BedsError",result);
    }

    @Test
    void UpdateBookingShouldReturnSavedWhenEnoughBedsAndCorrectDates(){
        Booking testbooking3 = new Booking((long) 222, "BN100", testcustomer, testroom, 1, checkIn, checkOut2);
        when(bookingRepo.findById(testbdto1.getId())).thenReturn(Optional.of(testbooking3));

        String result = bookingService.updateBooking(testbdto1.getId(),testbdto1);

        assertEquals("Saved",result);
    }

    @Test
    void UpdateBookingShouldReturnErrorWhenExistingBookingIsNull(){
        when(bookingRepo.findById(testbdto1.getId())).thenReturn(Optional.empty());

        String result = bookingService.updateBooking(testbdto1.getId(),testbdto1);

        assertEquals("Error",result);
    }

    @Test
    void UpdateBookingShouldReturnDateErrorWhenCheckinIsAfterCheckout(){
        Booking testbooking3 = new Booking((long) 222, "BN100", testcustomer, testroom, 1, checkIn, checkOut2);
        when(bookingRepo.findById(testbdto1.getId())).thenReturn(Optional.of(testbooking3));

        testbdto1.setCheckInDate(LocalDate.now().plusMonths(1).plusDays(1));
        testbdto1.setCheckOutDate(LocalDate.now().plusMonths(1));

        String result = bookingService.updateBooking(testbdto1.getId(),testbdto1);

        assertEquals("DateError",result);
    }

    @Test
    void UpdateBookingShouldReturnDateErrorWhenCheckinIsEqualToCheckout(){
        Booking testbooking3 = new Booking((long) 222, "BN100", testcustomer, testroom, 1, checkIn, checkOut2);
        when(bookingRepo.findById(testbdto1.getId())).thenReturn(Optional.of(testbooking3));

        testbdto1.setCheckInDate(LocalDate.now().plusMonths(1).plusDays(1));
        testbdto1.setCheckOutDate(LocalDate.now().plusMonths(1).plusDays(1));

        String result = bookingService.updateBooking(testbdto1.getId(),testbdto1);

        assertEquals("DateError",result);
    }

    @Test
    void CheckAvailabilityRoomTest() {
        when(bookingService.getAllBookings()).thenReturn(List.of(testbdto1));
        when(bookingRepo.findAll()).thenReturn(List.of(testbooking1));

        assertTrue(bookingService.checkAvailabilityInRoom(testbdto1.getId()+10, testbdto1.getRoom().getId(),
                LocalDate.now(), LocalDate.now().plusDays(1)));

        assertFalse(bookingService.checkAvailabilityInRoom(testbdto1.getId()+10, testbdto1.getRoom().getId(),
                LocalDate.now(), LocalDate.now().plusMonths(1).plusDays(2)));
    }

    @Test
    void existsBookingByBookingNrTest() {
        String bookingNr = testbooking1.getBookingNr();
        when(bookingRepo.existsByBookingNr(bookingNr)).thenReturn(true);

        boolean exists = bookingService.existsBookingByBookingNr(bookingNr);

        assertTrue(exists);
    }
    @Test
    void getBookingByBookingNrTest(){
        String bookingNr = testbooking1.getBookingNr();
        when(bookingRepo.findByBookingNr(bookingNr)).thenReturn(testbooking1);

        BookingDTO result = bookingService.getBookingByBookingNr(bookingNr);

        assertEquals(testbdto1, result);
    }


}