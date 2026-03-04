package com.example.SWP391_SPRING2026.Controller;

import com.example.SWP391_SPRING2026.DTO.Response.PayRemainingResponseDTO;
import com.example.SWP391_SPRING2026.Entity.Order;
import com.example.SWP391_SPRING2026.Entity.Payment;
import com.example.SWP391_SPRING2026.Entity.UserPrincipal;
import com.example.SWP391_SPRING2026.Enum.*;
import com.example.SWP391_SPRING2026.Exception.BadRequestException;
import com.example.SWP391_SPRING2026.Repository.OrderRepository;
import com.example.SWP391_SPRING2026.Repository.PaymentRepository;
import com.example.SWP391_SPRING2026.Service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderPaymentController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;

    @PostMapping("/{orderId}/pay-remaining")
    @Transactional
    public PayRemainingResponseDTO payRemaining(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🔥 Check ownership
        if (!order.getAddress().getUser().getId()
                .equals(principal.getUserId())) {

            throw new BadRequestException(
                    "You are not allowed to pay this order");
        }

        // 🔥 Only PRE_ORDER supports remaining
        if (order.getOrderType() != OrderType.PRE_ORDER) {
            throw new BadRequestException(
                    "Only PRE_ORDER supports remaining payment");
        }

        long remaining = order.getRemainingAmount() == null
                ? 0L
                : order.getRemainingAmount();

        if (remaining <= 0) {
            throw new BadRequestException(
                    "No remaining amount to pay");
        }

        // 🔥 Không cho thanh toán khi đã ship
        if (order.getOrderStatus() == OrderStatus.SHIPPING
                || order.getOrderStatus() == OrderStatus.COMPLETED) {

            throw new BadRequestException(
                    "Cannot pay remaining after shipping started");
        }

        // =====================================================
        // CHECK EXISTING REMAINING PAYMENT
        // =====================================================
        Payment existingRemaining = paymentRepository
                .findTopByOrder_IdAndStageOrderByIdDesc(
                        orderId,
                        PaymentStage.REMAINING
                )
                .orElse(null);

        // Nếu đã SUCCESS rồi thì không cho trả nữa
        if (existingRemaining != null
                && existingRemaining.getMethod() == PaymentMethod.VNPAY
                && existingRemaining.getStatus() == PaymentStatus.SUCCESS) {

            throw new BadRequestException(
                    "Remaining already paid");
        }

        // Nếu đang là COD UNPAID → cancel để chuyển VNPAY
        if (existingRemaining != null
                && existingRemaining.getMethod() == PaymentMethod.COD
                && existingRemaining.getStatus() == PaymentStatus.UNPAID) {

            existingRemaining.setStatus(PaymentStatus.CANCELLED);
        }

        // =====================================================
        // CREATE NEW REMAINING PAYMENT (VNPAY)
        // =====================================================
        order.setRemainingPaymentMethod(PaymentMethod.VNPAY);

        Payment pay = new Payment();
        pay.setOrder(order);
        pay.setStage(PaymentStage.REMAINING);
        pay.setMethod(PaymentMethod.VNPAY);
        pay.setAmount(remaining);
        pay.setStatus(PaymentStatus.PENDING);
        pay.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(pay);

        // =====================================================
        // CREATE VNPAY URL
        // =====================================================
        String ipAddress = request.getRemoteAddr();
        if (ipAddress == null
                || ipAddress.equals("0:0:0:0:0:0:0:1")) {

            ipAddress = "127.0.0.1";
        }

        String paymentUrl;
        try {
            paymentUrl = vnPayService.createVNPayUrl(
                    pay.getId().toString(),
                    remaining,
                    ipAddress
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot create VNPay URL");
        }

        return new PayRemainingResponseDTO(
                orderId,
                pay.getId(),
                remaining,
                paymentUrl
        );
    }
}