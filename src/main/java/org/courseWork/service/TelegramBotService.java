package org.courseWork.service;

import lombok.extern.slf4j.Slf4j;
import org.courseWork.model.ProductOffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class TelegramBotService {

    private final RecommendationService recommendationService;
    private final RuleStatisticsService statisticsService;
    private final String botToken;
    private final RestTemplate restTemplate;
    private volatile boolean polling = true;

    public TelegramBotService(RecommendationService recommendationService,
                              RuleStatisticsService statisticsService,
                              @Value("${telegram.bot.token}") String botToken) {
        this.recommendationService = recommendationService;
        this.statisticsService = statisticsService;
        this.botToken = botToken;
        this.restTemplate = new RestTemplate();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startPolling() {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Telegram bot token is not configured. Bot will not start.");
            return;
        }

        log.info("Starting Telegram bot polling...");
        new Thread(this::pollUpdates).start();
    }

    private void pollUpdates() {
        int lastUpdateId = 0;

        while (polling) {
            try {
                String url = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=30";

                Map response = restTemplate.getForObject(url, Map.class);

                if (response != null && response.get("ok").equals(true)) {
                    List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");

                    for (Map<String, Object> update : updates) {
                        processUpdate(update);
                        lastUpdateId = Math.max(lastUpdateId, (Integer) update.get("update_id"));
                    }
                }

                Thread.sleep(1000); // Пауза между запросами

            } catch (Exception e) {
                log.error("Error in Telegram polling", e);
                try {
                    Thread.sleep(5000); // Пауза при ошибке
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processUpdate(Map<String, Object> update) {
        try {
            if (update.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) update.get("message");
                Map<String, Object> chat = (Map<String, Object>) message.get("chat");
                String text = (String) message.get("text");
                Long chatId = ((Number) chat.get("id")).longValue();

                log.info("Received message from chat {}: {}", chatId, text);
                processMessage(chatId, text);
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    public void processMessage(Long chatId, String text) {
        try {
            switch (text.toLowerCase()) {
                case "/start":
                    sendWelcomeMessage(chatId);
                    break;
                case "/recommendations":
                    sendMessage(chatId, "Пожалуйста, укажите ваш ID пользователя: /recommendations <user_id>");
                    break;
                case "/stats":
                    sendStatistics(chatId);
                    break;
                case "/help":
                    sendHelpMessage(chatId);
                    break;
                default:
                    if (text.startsWith("/recommendations ")) {
                        processRecommendationsCommand(chatId, text);
                    } else {
                        sendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.");
                    }
            }
        } catch (Exception e) {
            log.error("Error processing Telegram message", e);
            sendMessage(chatId, "Произошла ошибка при обработке запроса");
        }
    }

    private void sendHelpMessage(Long chatId) {
        String message = "📋 Доступные команды:\n\n" +
                "/start - начать работу\n" +
                "/recommendations <user_id> - получить рекомендации для пользователя\n" +
                "/stats - показать статистику рекомендаций\n" +
                "/help - показать это сообщение";
        sendMessage(chatId, message);
    }

    private void sendStatistics(Long chatId) {
        try {
            Map<String, Object> stats = statisticsService.getOverallStatistics();

            StringBuilder message = new StringBuilder();
            message.append("📊 Статистика рекомендаций:\n\n");
            message.append("• Всего рекомендаций выдано: ").append(stats.get("totalRecommendations")).append("\n");
            message.append("• Уникальных пользователей: ").append(stats.get("uniqueUsers")).append("\n");

            sendMessage(chatId, message.toString());
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            sendMessage(chatId, "Ошибка при получении статистики");
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String message = "🤖 Добро пожаловать в систему рекомендаций!\n\n" +
                "Доступные команды:\n" +
                "/recommendations <user_id> - получить рекомендации\n" +
                "/stats - статистика рекомендаций\n" +
                "/help - помощь";
        sendMessage(chatId, message);
    }


    private void processRecommendationsCommand(Long chatId, String text) {
        try {
            String[] parts = text.split(" ");
            if (parts.length < 2) {
                sendMessage(chatId, "Пожалуйста, укажите ID пользователя: /recommendations <user_id>");
                return;
            }

            UUID userId = UUID.fromString(parts[1]);
            List<ProductOffer> recommendations = recommendationService.getRecommendedProducts(userId);

            if (recommendations.isEmpty()) {
                sendMessage(chatId, "Для пользователя " + userId + " нет доступных рекомендаций");
            } else {
                StringBuilder message = new StringBuilder();
                message.append("🎯 Рекомендации для пользователя ").append(userId).append(":\n\n");

                for (int i = 0; i < recommendations.size(); i++) {
                    ProductOffer offer = recommendations.get(i);
                    message.append(i + 1).append(". ").append(offer.getProductName()).append("\n");
                    message.append("   📝 ").append(offer.getDescription()).append("\n\n");
                }

                sendMessage(chatId, message.toString());
                statisticsService.recordRecommendationEvent(userId, recommendations.size());
            }
        } catch (Exception e) {
            log.error("Error processing recommendations command", e);
            sendMessage(chatId, "Ошибка при обработке команды");
        }
    }

    public void sendMessage(Long chatId, String text) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            Map<String, Object> request = new HashMap<>();
            request.put("chat_id", chatId);
            request.put("text", text);

            restTemplate.postForObject(url, request, String.class);

        } catch (Exception e) {
            log.error("Error sending message to Telegram", e);
        }
    }
}