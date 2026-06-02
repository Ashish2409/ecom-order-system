package com.ashish.ecom.notification_service.repository;

import com.ashish.ecom.notification_service.model.Notification;
import com.ashish.ecom.notification_service.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    boolean existsByIdempotencyKey(String idempotencyKey);
    
    List<Notification> findByOrderId(Long orderId);
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Notification> findByStatus(NotificationStatus status);
}
