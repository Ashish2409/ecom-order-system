package com.ashish.ecom.notification_service.controller;

import com.ashish.ecom.notification_service.dto.ApiResponse;
import com.ashish.ecom.notification_service.dto.NotificationResponse;
import com.ashish.ecom.notification_service.dto.PagedResponse;
import com.ashish.ecom.notification_service.model.NotificationStatus;
import com.ashish.ecom.notification_service.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get notifications by order ID")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByOrderId(@PathVariable Long orderId) {
        List<NotificationResponse> notifications = notificationRepository.findByOrderId(orderId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications by user ID (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        PagedResponse<NotificationResponse> pagedResponse = PagedResponse.from(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable),
                NotificationResponse::from
        );

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @GetMapping("/failed")
    @Operation(summary = "Get all failed notifications (for admin monitoring)")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getFailedNotifications() {
        List<NotificationResponse> failedNotifications = notificationRepository
                .findByStatus(NotificationStatus.FAILED).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(failedNotifications));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<ApiResponse<NotificationStats>> getStats() {
        long total = notificationRepository.count();
        long sent = notificationRepository.findByStatus(NotificationStatus.SENT).size();
        long failed = notificationRepository.findByStatus(NotificationStatus.FAILED).size();
        long pending = notificationRepository.findByStatus(NotificationStatus.PENDING).size();

        NotificationStats stats = NotificationStats.builder()
                .total(total)
                .sent(sent)
                .failed(failed)
                .pending(pending)
                .successRate(total > 0 ? (double) sent / total * 100 : 0)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Data
    @Builder
    static class NotificationStats {
        private long total;
        private long sent;
        private long failed;
        private long pending;
        private double successRate;
    }
}
