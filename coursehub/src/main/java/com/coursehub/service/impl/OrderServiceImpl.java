package com.coursehub.service.impl;

import com.coursehub.dto.request.CheckoutRequest;
import com.coursehub.dto.response.OrderResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.CourseStatus;
import com.coursehub.enums.EnrollmentStatus;
import com.coursehub.enums.PaymentStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.*;
import com.coursehub.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public OrderResponse checkout(UUID userId, CheckoutRequest request) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, request.getCourseId())) {
            throw new BadRequestException("VALID_001", "Bạn đã đăng ký khóa học này rồi.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException("VALID_001", "Khóa học chưa được xuất bản.");
        }

        BigDecimal amount = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .totalAmount(amount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(amount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        order = orderRepository.save(order);

        OrderItemEntity orderItem = OrderItemEntity.builder()
                .order(order)
                .course(course)
                .price(amount)
                .build();

        orderItemRepository.save(orderItem);

        log.info("Created PENDING order {} for user {} buying course {}", order.getId(), userId, course.getId());
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(UUID userId, UUID orderId, String transactionId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("AUTHZ_003", "Đơn hàng này không thuộc về bạn.");
        }

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            return mapToResponse(order);
        }

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setTransactionId(transactionId);
        order = orderRepository.save(order);

        // Fetch order items and activate enrollment
        var items = orderItemRepository.findByOrderId(orderId);
        for (var item : items) {
            CourseEntity course = item.getCourse();
            if (!enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId())) {
                EnrollmentEntity enrollment = EnrollmentEntity.builder()
                        .user(order.getUser())
                        .course(course)
                        .enrollmentDate(LocalDateTime.now())
                        .progressPercent(BigDecimal.ZERO)
                        .status(EnrollmentStatus.ACTIVE)
                        .build();
                enrollmentRepository.save(enrollment);
                log.info("Activated enrollment for user {} in course {} after order {}", userId, course.getId(), orderId);
            }
        }

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .paymentStatus(order.getPaymentStatus())
                .transactionId(order.getTransactionId())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
