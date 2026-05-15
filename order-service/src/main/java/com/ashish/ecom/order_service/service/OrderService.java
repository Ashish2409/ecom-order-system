package com.ashish.ecom.order_service.service;

import com.ashish.ecom.order_service.dto.*;
import com.ashish.ecom.order_service.model.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse placeOrder(PlaceOrderRequest request, Long userId, String userEmail, String jwtToken);

    OrderResponse getOrderById(Long id, Long requestingUserId, boolean isAdmin);

    PagedResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable);

    PagedResponse<OrderResponse> getAllOrders(Pageable pageable);

    OrderResponse updateStatus(Long orderId, OrderStatus newStatus);

    OrderResponse cancelOrder(Long orderId, Long requestingUserId, boolean isAdmin, String jwtToken);
}