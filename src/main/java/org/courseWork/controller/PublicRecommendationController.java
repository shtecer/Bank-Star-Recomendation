package org.courseWork.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.courseWork.dto.ProductOffer;
import org.courseWork.model.Client;
import org.courseWork.service.ClientService;
import org.courseWork.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api")
public class PublicRecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    private final ClientService clientService;
    private final RecommendationDynamicRuleService recommendationDynamicRuleService;

    public PublicRecommendationController(RecommendationDynamicRuleService recommendationDynamicRuleService,
                                          RecommendationService recommendationService, ClientService clientService) {
        this.recommendationDynamicRuleService = recommendationDynamicRuleService;
        this.recommendationService = recommendationService;
        this.clientService = clientService;
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
    @GetMapping("/recommendations/{id}")
    public ResponseEntity<List<ProductOffer>> getRecommendations(@RequestParam UUID userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<ProductOffer> recommendations = recommendationService.getRecommendedProducts(userId);
            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Error getting recommendations for user " + userId + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Создание динамического правила", description = "Создает новое динамическое правило")
    @PostMapping
    @ApiResponse(responseCode = "200", description = "Правило успешно создано")

    public ResponseEntity<DynamicRecommendations> createRule(@RequestBody DynamicRecommendations rule) {
        if (rule.getProductName() == null) {
            return ResponseEntity.badRequest().build();
        }
        DynamicRule savedRule = recommendationDynamicRuleService.addRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(savedRule);
    }

    @Operation(summary = "Удаление правила", description = "Удаляет правило по его ID")
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Правило удалено")

    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        try {
            recommendationDynamicRuleService.deleteDynamicRule(id);
            return ResponseEntity.noContent().build();
        } catch (RulesNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Получение всех динамических правил", description = "Возвращает список всех динамических правил")
    @GetMapping
    @ApiResponse(responseCode = "200", description = "Правила получены")

    public ResponseEntity<List<DynamicRecommendations>> getAllRules() {
        List<DynamicRecommendations> rules = recommendationDynamicRuleService.getAllDynamicRules();
        return ResponseEntity.ok(rules);
    }
}
