package com.ashish.ecom.notification_service.kafka;

import com.ashish.ecom.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void consume(ConsumerRecord<String, OrderEventDTO> record, Acknowledgment ack) {
        try {
            OrderEventDTO event = record.value();
            log.info("📩 Received order event: orderId={} status={} userEmail={}", 
                    event.getOrderId(), event.getStatus(), event.getUserEmail());

            // Process event (send notifications)
            notificationService.processOrderEvent(event);

            // ⭐ Manual commit after successful processing
            ack.acknowledge();
            log.info("✅ Order event processed successfully: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ Failed to process order event: offset={} partition={}", 
                    record.offset(), record.partition(), e);
            // ⭐ Don't ack — message will be redelivered
            // In production: implement DLQ (Dead Letter Queue) after max retries
            // For now: log and let Kafka retry
        }
    }
}
