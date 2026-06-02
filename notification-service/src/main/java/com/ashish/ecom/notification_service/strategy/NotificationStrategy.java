package com.ashish.ecom.notification_service.strategy;

import com.ashish.ecom.notification_service.model.Notification;
import com.ashish.ecom.notification_service.model.NotificationChannel;

public interface NotificationStrategy {
    NotificationChannel getChannel();
    void send(Notification notification) throws Exception;
}
