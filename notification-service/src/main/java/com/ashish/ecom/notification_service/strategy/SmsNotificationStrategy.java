package com.ashish.ecom.notification_service.strategy;

import com.ashish.ecom.notification_service.model.Notification;
import com.ashish.ecom.notification_service.model.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsNotificationStrategy implements NotificationStrategy {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(Notification notification) {
        log.info("📱 Sending SMS to {}", notification.getRecipient());
        
        // Mock implementation - simulate sending SMS
        try {
            // Simulate network delay
            Thread.sleep(150);
            
            log.info("✅ SMS sent successfully to {}", notification.getRecipient());
            log.debug("SMS content: {}", notification.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS sending interrupted", e);
        }
        
        // In production, integrate with:
        // - Twilio: twilioClient.messages.create(params)
        // - AWS SNS: snsClient.publish(request)
        // - Nexmo: nexmoClient.sendMessage(message)
    }
}
