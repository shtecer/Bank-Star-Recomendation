package org.courseWork.service;

import org.courseWork.model.Client;
import org.courseWork.repository.BankClientsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {
    private final BankClientsRepository bankClientsRepository;

    public ClientService(BankClientsRepository bankClientsRepository){

        this.bankClientsRepository = bankClientsRepository;
    }

    public Client findById(UUID id){
        return bankClientsRepository.findById(id);

    }

    public List<Client> getAllClients() {
        return bankClientsRepository.findAll();
    }

}
