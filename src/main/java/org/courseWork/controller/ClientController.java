package org.courseWork.controller;

import org.courseWork.model.Client;
import org.courseWork.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ClientController {

    @Autowired
    private ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    @GetMapping
    public String testApi() {
        return "Welcome to Demo!";
    }

    @GetMapping("/recomendation/{id}")
    public Client getClientById(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        return clientService.findById(uuid);
    }
    @GetMapping ("/all")
    public ResponseEntity<Collection<Client>> getAllClients() {
        List<Client> clients = clientService.getAllClients();

        return ResponseEntity.ok(clients);
    }

}
