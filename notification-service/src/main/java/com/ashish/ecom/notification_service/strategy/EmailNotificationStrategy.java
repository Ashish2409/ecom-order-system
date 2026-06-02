package com.ashish.ecom.notification_service.strategy;

import com.ashish.ecom.notification_service.model.Notification;
import com.ashish.ecom.notification_service.model.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        log.info("📧 Sending email to {}: {}", notification.getRecipient(), notification.getSubject());
        
        // Mock implementation - simulate sending email
        try {
            // Simulate network delay
            Thread.sleep(200);
            
            log.info("✅ Email sent successfully to {}", notification.getRecipient());
            log.debug("Email content: {}", notification.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email sending interrupted", e);
        }
        
        // In production, integrate with:
        // - SendGrid: sendGridClient.send(email)
        // - AWS SES: sesClient.sendEmail(request)
        // - Mailgun: mailgunClient.sendMessage(domain, request)
    }
}
