package com.ashish.ecom.notification_service.service;

import com.ashish.ecom.notification_service.kafka.OrderEventDTO;
import com.ashish.ecom.notification_service.model.*;
import com.ashish.ecom.notification_service.repository.NotificationRepository;
import com.ashish.ecom.notification_service.strategy.NotificationStrategy;
import com.ashish.ecom.notification_service.strategy.NotificationStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationStrategyFactory strategyFactory;

    @Transactional
    public void processOrderEvent(OrderEventDTO event) {
        log.info("Processing order event: orderId={} status={}", event.getOrderId(), event.getStatus());

        String baseIdempotencyKey = "order-" + event.getOrderId() + "-" + event.getStatus();

        // ⭐ Idempotency check - avoid duplicate processing
        if (notificationRepository.existsByIdempotencyKey(baseIdempotencyKey + "-EMAIL")) {
            log.info("Event already processed, skipping: {}", baseIdempotencyKey);
            return;
        }

        NotificationType type = mapStatusToNotificationType(event.getStatus());
        List<NotificationChannel> channels = getChannelsForType(type);

        log.info("Sending {} notifications via channels: {}", type, channels);

        for (NotificationChannel channel : channels) {
            String idempotencyKey = baseIdempotencyKey + "-" + channel.name();
            sendNotification(event, channel, type, idempotencyKey);
        }
    }

    private void sendNotification(OrderEventDTO event, NotificationChannel channel,
                                   NotificationType type, String idempotencyKey) {
        // Check if already sent
        if (notificationRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.debug("Notification already sent: {}", idempotencyKey);
            return;
        }

        try {
            // Build notification entity
            Notification notification = Notification.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .channel(channel)
                    .type(type)
                    .recipient(getRecipient(event, channel))
                    .subject(generateSubject(type, event.getOrderId()))
                    .message(generateMessage(event, type))
                    .status(NotificationStatus.PENDING)
                    .idempotencyKey(idempotencyKey)
                    .metadata(buildMetadata(event))
                    .build();

            // Save to DB (PENDING status)
            notification = notificationRepository.save(notification);
            log.debug("Notification created: id={} channel={}", notification.getId(), channel);

            // ⭐ Send via strategy
            NotificationStrategy strategy = strategyFactory.getStrategy(channel);
            strategy.send(notification);

            // Update status to SENT
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);

            log.info("✅ Notification sent: id={} channel={} type={}", 
                    notification.getId(), channel, type);

        } catch (Exception e) {
            log.error("❌ Failed to send {} notification for order {}", 
                    channel, event.getOrderId(), e);

            // Save failed notification for retry
            Notification failedNotification = Notification.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .channel(channel)
                    .type(type)
                    .recipient(getRecipient(event, channel))
                    .subject(generateSubject(type, event.getOrderId()))
                    .message(generateMessage(event, type))
                    .status(NotificationStatus.FAILED)
                    .idempotencyKey(idempotencyKey)
                    .failureReason(e.getMessage())
                    .retryCount(0)
                    .build();

            notificationRepository.save(failedNotification);
        }
    }

    // ─── Helper Methods ───

    private NotificationType mapStatusToNotificationType(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> NotificationType.ORDER_PLACED;
            case "CONFIRMED" -> NotificationType.ORDER_CONFIRMED;
            case "SHIPPED" -> NotificationType.ORDER_SHIPPED;
            case "DELIVERED" -> NotificationType.ORDER_DELIVERED;
            case "CANCELLED" -> NotificationType.ORDER_CANCELLED;
            default -> throw new IllegalArgumentException("Unknown order status: " + status);
        };
    }

    private List<NotificationChannel> getChannelsForType(NotificationType type) {
        return switch (type) {
            case ORDER_PLACED -> List.of(NotificationChannel.EMAIL);
            case ORDER_CONFIRMED -> List.of(NotificationChannel.EMAIL, NotificationChannel.SMS);
            case ORDER_SHIPPED -> List.of(NotificationChannel.EMAIL, NotificationChannel.PUSH);
            case ORDER_DELIVERED -> List.of(NotificationChannel.EMAIL);
            case ORDER_CANCELLED -> List.of(NotificationChannel.EMAIL);
        };
    }

    private String getRecipient(OrderEventDTO event, NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> event.getUserEmail();
            case SMS -> "+1234567890";  // Mock phone number (would come from user profile)
            case PUSH -> "device-token-" + event.getUserId();  // Mock device token
        };
    }

    private String generateSubject(NotificationType type, Long orderId) {
        return switch (type) {
            case ORDER_PLACED -> "Order Confirmation - Order #" + orderId;
            case ORDER_CONFIRMED -> "Your Order #" + orderId + " is Confirmed!";
            case ORDER_SHIPPED -> "Your Order #" + orderId + " has been Shipped";
            case ORDER_DELIVERED -> "Your Order #" + orderId + " has been Delivered";
            case ORDER_CANCELLED -> "Order #" + orderId + " has been Cancelled";
        };
    }

    private String generateMessage(OrderEventDTO event, NotificationType type) {
        String baseMessage = switch (type) {
            case ORDER_PLACED -> "Thank you for your order! We've received your order #%d.";
            case ORDER_CONFIRMED -> "Great news! Your order #%d has been confirmed and is being processed.";
            case ORDER_SHIPPED -> "Your order #%d is on its way! Track your package for updates.";
            case ORDER_DELIVERED -> "Your order #%d has been delivered. Enjoy your purchase!";
            case ORDER_CANCELLED -> "Your order #%d has been cancelled. If you didn't request this, please contact support.";
        };

        String message = String.format(baseMessage, event.getOrderId());
        message += String.format("\n\nOrder Details:\n- Quantity: %d\n- Total: $%.2f\n- Date: %s",
                event.getQuantity(),
                event.getTotalAmount(),
                event.getTimestamp());

        return message;
    }

    private String buildMetadata(OrderEventDTO event) {
        // Store as JSON string for flexibility
        return String.format("{\"productId\":%d,\"quantity\":%d,\"totalAmount\":%.2f}",
                event.getProductId(),
                event.getQuantity(),
                event.getTotalAmount());
    }
}
