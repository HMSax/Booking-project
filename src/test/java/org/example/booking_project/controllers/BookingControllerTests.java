package org.example.booking_project.controllers;

import org.example.booking_project.models.Customer;
import org.example.booking_project.repos.CustomerRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTests {

    @MockBean
    private CustomerRepo mockRepo;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void init() {
        Customer testcustomer = new Customer(123L, "CN001", "Kalle",
                "012-345678", "abc@abcdef.se");
        when(mockRepo.findById(123L)).thenReturn(Optional.of(testcustomer));
    }

    @WithMockUser(value = "spring")
    @Test
    void book() throws Exception {
        this.mvc.perform(get("/book")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Incheckningsdatum")));
    }
    @WithMockUser(value = "spring")
    @Test
    void bookReceival() throws Exception {
        this.mvc.perform(get("/bookReceival"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Tillgängliga rum för valda datum")));
    }
    @WithMockUser(value = "spring")
    @Test
    void createBooking() throws Exception {
        this.mvc.perform(get("/createbooking?roomNumber=201")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingen användare hittades med den emailen.")));
    }
}