package com.coursehub.dto.response;

import com.coursehub.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime createdAt;
}
