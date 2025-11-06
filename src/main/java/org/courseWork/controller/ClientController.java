package org.courseWork.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.courseWork.model.Client;
import org.courseWork.model.Product;
import org.courseWork.service.ClientService;
import org.courseWork.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api")
public class ClientController {

    @Autowired
    private ClientService clientService;

    
    @Autowired
    private ProductService productService;

    public ClientController(ClientService clientService, ProductService productService) {
        this.clientService = clientService;
        this.productService = productService;
    }
    @GetMapping
    public String testApi() {
        return "Welcome to Demo!";
    }

    @GetMapping("/client/{id}")
    public Client getClientById(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        return clientService.findById(uuid);
    }
    @GetMapping ("/allClients")
    public ResponseEntity<Collection<Client>> getAllClients() {
        List<Client> clients = clientService.getAllClients();

        return ResponseEntity.ok(clients);
    }
    @Operation(summary = "Получить рекомендации по ID пользователя")
    @ApiResponse(responseCode = "200", description = "Список рекомендованных продуктов")
    @GetMapping("/recomendations/{id}")
    public List<Product> getProductByUserId (@Parameter (description = "ID пользователя")@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        return productService.findProductByUserId(uuid);
    }
}
