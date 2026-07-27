package com.coursehub.controller;

import com.coursehub.dto.request.CheckoutRequest;
import com.coursehub.dto.response.ApiResponse;
import com.coursehub.dto.response.OrderResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order & Payment", description = "Quản lý đơn hàng và thanh toán")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Tạo đơn hàng thanh toán (Checkout)")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckoutRequest request) {
        OrderResponse response = orderService.checkout(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đơn hàng thành công.", response));
    }

    @PostMapping("/{orderId}/complete")
    @Operation(summary = "Xác nhận hoàn thành thanh toán đơn hàng (Mock Payment Confirmation)")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID orderId,
            @RequestParam(required = false) String transactionId) {
        String txId = transactionId != null ? transactionId : "MOCK-TX-" + System.currentTimeMillis();
        OrderResponse response = orderService.completeOrder(principal.getId(), orderId, txId);
        return ResponseEntity.ok(ApiResponse.success("Thanh toán đơn hàng thành công.", response));
    }
}
