package com.ashish.ecom.notification_service.strategy;

import com.ashish.ecom.notification_service.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationStrategyFactory {
    
    private final List<NotificationStrategy> strategies;
    
    public NotificationStrategy getStrategy(NotificationChannel channel) {
        return strategies.stream()
                .filter(strategy -> strategy.getChannel() == channel)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for channel: " + channel));
    }
}
