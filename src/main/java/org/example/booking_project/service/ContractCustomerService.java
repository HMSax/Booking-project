package org.example.booking_project.service;

import org.example.booking_project.Dtos.ContractCustomerDTO;
import org.example.booking_project.models.ContractCustomer;

public interface ContractCustomerService {

    public ContractCustomerDTO contractCustomerToContractCustomerDTO(ContractCustomer c);

    public ContractCustomer contractCustomerDTOToContractCustomer(ContractCustomerDTO c);

    public void updateOrAddContractCustomer(Long id, ContractCustomerDTO contractCustomerDTO);

    public ContractCustomer getContractCustomerById(Long customerId);

}

