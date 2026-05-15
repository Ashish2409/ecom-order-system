package com.ashish.ecom.order_service.controller;

import com.ashish.ecom.order_service.common.ApiResponse;
import com.ashish.ecom.order_service.dto.*;
import com.ashish.ecom.order_service.security.JwtFilter;
import com.ashish.ecom.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management")
public class OrderController {

    private final OrderService orderService;

    // ─── Place order ───
    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpReq) {

        Long userId = (Long) httpReq.getAttribute(JwtFilter.USER_ID_ATTR);
        String userEmail = (String) httpReq.getAttribute(JwtFilter.USER_EMAIL_ATTR);
        String jwtToken = (String) httpReq.getAttribute(JwtFilter.JWT_TOKEN_ATTR);

        OrderResponse response = orderService.placeOrder(request, userId, userEmail, jwtToken);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", response));
    }

    // ─── Get my orders ───
    @GetMapping("/me")
    @Operation(summary = "Get my orders (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> myOrders(
            HttpServletRequest httpReq, Pageable pageable) {

        Long userId = (Long) httpReq.getAttribute(JwtFilter.USER_ID_ATTR);
        return ResponseEntity.ok(ApiResponse.success(
                "Your orders", orderService.getMyOrders(userId, pageable)));
    }

    // ─── Get order by ID ───
    @GetMapping("/{id}")
    @Operation(summary = "Get order details (own or admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long id, HttpServletRequest httpReq, Authentication auth) {

        Long userId = (Long) httpReq.getAttribute(JwtFilter.USER_ID_ATTR);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(ApiResponse.success(
                "Order fetched", orderService.getOrderById(id, userId, isAdmin)));
    }

    // ─── Cancel order ───
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order (own or admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id, HttpServletRequest httpReq, Authentication auth) {

        Long userId = (Long) httpReq.getAttribute(JwtFilter.USER_ID_ATTR);
        String jwtToken = (String) httpReq.getAttribute(JwtFilter.JWT_TOKEN_ATTR);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(ApiResponse.success(
                "Order cancelled",
                orderService.cancelOrder(id, userId, isAdmin, jwtToken)));
    }

    // ─── Admin: list all orders ───
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all orders (admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> allOrders(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "All orders", orderService.getAllOrders(pageable)));
    }

    // ─── Admin: update status ───
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Status updated",
                orderService.updateStatus(id, request.getStatus())));
    }
}