package com.coursehub.service;

import com.coursehub.dto.request.CheckoutRequest;
import com.coursehub.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderService {
    OrderResponse checkout(UUID userId, CheckoutRequest request);
    OrderResponse completeOrder(UUID userId, UUID orderId, String transactionId);
}
