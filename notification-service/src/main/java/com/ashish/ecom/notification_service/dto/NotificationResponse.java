package com.ashish.ecom.notification_service.dto;

import com.ashish.ecom.notification_service.model.Notification;
import com.ashish.ecom.notification_service.model.NotificationChannel;
import com.ashish.ecom.notification_service.model.NotificationStatus;
import com.ashish.ecom.notification_service.model.NotificationType;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private NotificationChannel channel;
    private NotificationType type;
    private String recipient;
    private String subject;
    private String message;
    private NotificationStatus status;
    private Instant sentAt;
    private String failureReason;
    private Integer retryCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .orderId(notification.getOrderId())
                .userId(notification.getUserId())
                .channel(notification.getChannel())
                .type(notification.getType())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .failureReason(notification.getFailureReason())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
