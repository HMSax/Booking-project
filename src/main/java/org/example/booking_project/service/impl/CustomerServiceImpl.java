package org.example.booking_project.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.booking_project.Dtos.CustomerDTO;
import org.example.booking_project.models.Customer;
import org.example.booking_project.repos.BookingRepo;
import org.example.booking_project.repos.CustomerRepo;
import org.example.booking_project.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo customerRepo;
    private final BookingRepo bookingRepo;

    @Override
    public CustomerDTO customerToCustomerDTO(Customer c) {
        return CustomerDTO.builder().id(c.getId()).customerNumber(c.getCustomerNumber())
                .customerName(c.getCustomerName()).phoneNumber(c.getPhoneNumber()).email(c.getEmail()).build();
    }

    @Override
    public Customer customerDTOToCustomer(CustomerDTO c) {
        return Customer.builder().id(c.getId()).customerNumber(c.getCustomerNumber())
                .customerName(c.getCustomerName()).phoneNumber(c.getPhoneNumber()).email(c.getEmail()).build();
    }

    @Override
    public void addCustomer(CustomerDTO customerDTO) {
        Customer customer = customerDTOToCustomer(customerDTO);
        Customer savedCustomer = customerRepo.save(customer);
        customerToCustomerDTO(savedCustomer);
    }

    @Override
    public CustomerDTO getCustomerByEmail(String email) {
        Customer customer = customerRepo.findByEmail(email);
        return customerToCustomerDTO(customer);
    }

    @Override
    public boolean existsCustomerByEmail(String email) {
        return customerRepo.existsByEmail(email);
    }

    @Override
    public void updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepo.findById(id).orElse(null);
        if (existingCustomer != null) {
            // Uppdatera kundens egenskaper
            existingCustomer.setCustomerName(customerDTO.getCustomerName());
            existingCustomer.setPhoneNumber(customerDTO.getPhoneNumber());
            existingCustomer.setEmail(customerDTO.getEmail()); // Uppdatera e-postadressen
            customerRepo.save(existingCustomer); // Spara den uppdaterade kunden i databasen
        }
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepo.findById(id).orElse(null);
        if (customer != null) {
            boolean hasBookings = bookingRepo.existsByCustomerId(id);
            if (!hasBookings) {
                customerRepo.deleteById(id);
            } else {
                System.out.println("Kunden har bokningar och kan inte tas bort!");
            }
        }
    }
}
