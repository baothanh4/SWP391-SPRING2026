package com.example.SWP391_SPRING2026.Service;

import com.example.SWP391_SPRING2026.DTO.Request.CheckoutRequestDTO;
import com.example.SWP391_SPRING2026.DTO.Response.CheckoutResponseDTO;
import com.example.SWP391_SPRING2026.Entity.*;
import com.example.SWP391_SPRING2026.Enum.*;
import com.example.SWP391_SPRING2026.Exception.BadRequestException;
import com.example.SWP391_SPRING2026.Repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.SWP391_SPRING2026.Utility.DepositPolicy;

import java.time.LocalDateTime;

import com.example.SWP391_SPRING2026.Enum.PaymentStage;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutService {

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PreOrderRepository preOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private Long depositAmount; // optional, chỉ dùng cho PRE_ORDER



    public CheckoutResponseDTO checkout(Long userId,
                                        CheckoutRequestDTO dto,
                                        HttpServletRequest request) {

        Cart cart = cartRepository
                .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart items is empty");
        }

        Address address = resolveAddress(userId, dto.getAddressId());

        // ===== XÁC ĐỊNH SALE TYPE =====
        SaleType saleType = null;

        for (CartItem item : cart.getItems()) {

            if (item.getProductVariant() != null) {

                SaleType itemType = item.getProductVariant().getSaleType();

                if (saleType == null) saleType = itemType;
                else if (saleType != itemType) {
                    throw new BadRequestException("Cannot checkout mixed products");
                }

            } else if (item.getProductCombo() != null) {

                // 🔥 Combo luôn được coi là IN_STOCK
                if (saleType == null) saleType = SaleType.IN_STOCK;
                else if (saleType != SaleType.IN_STOCK) {
                    throw new BadRequestException("Cannot checkout mixed products");
                }
            }
        }

        // ===== CREATE ORDER =====
        Order order = new Order();
        order.setOrderCode("ORD-" + System.currentTimeMillis());
        order.setOrderType(
                saleType == SaleType.IN_STOCK ?
                        OrderType.IN_STOCK : OrderType.PRE_ORDER
        );
        order.setOrderStatus(OrderStatus.WAITING_CONFIRM);
        order.setAddress(address);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        long totalAmount = 0;

        // ===== CREATE ORDER ITEMS =====
        for (CartItem cartItem : cart.getItems()) {

            int quantity = cartItem.getQuantity();

            // ================= PRODUCT =================
            if (cartItem.getProductVariant() != null) {

                ProductVariant variant = productVariantRepository
                        .lockById(cartItem.getProductVariant().getId())
                        .orElseThrow(() -> new BadRequestException("Variant not found"));

                long price = variant.getPrice().longValue();

                if (saleType == SaleType.IN_STOCK) {
                    if (variant.getStockQuantity() < quantity) {
                        throw new BadRequestException("Insufficient stock");
                    }
                    variant.setStockQuantity(
                            variant.getStockQuantity() - quantity
                    );
                }

                OrderItems orderItem = new OrderItems();
                orderItem.setOrder(order);
                orderItem.setProductVariant(variant);
                orderItem.setQuantity(quantity);
                orderItem.setPrice(price);
                orderItem.setIsCombo(false);

                orderItemRepository.save(orderItem);

                totalAmount += price * quantity;
            }

            // ================= COMBO =================
            else if (cartItem.getProductCombo() != null) {

                ProductCombo combo = cartItem.getProductCombo();

                long comboPrice = combo.getComboPrice().longValue();

                OrderItems orderItem = new OrderItems();
                orderItem.setOrder(order);
                orderItem.setProductCombo(combo);
                orderItem.setQuantity(quantity);
                orderItem.setPrice(comboPrice);
                orderItem.setIsCombo(true);

                orderItemRepository.save(orderItem);

                totalAmount += comboPrice * quantity;

                // 🔥 Trừ stock từng variant trong combo
                for (ComboItem comboItem : combo.getItems()) {

                    ProductVariant variant = productVariantRepository
                            .lockById(comboItem.getProductVariant().getId())
                            .orElseThrow(() -> new BadRequestException("Variant not found"));

                    int required = comboItem.getQuantity() * quantity;

                    if (variant.getStockQuantity() < required) {
                        throw new BadRequestException("Insufficient stock in combo");
                    }

                    variant.setStockQuantity(
                            variant.getStockQuantity() - required
                    );
                }
            }
        }

        order.setTotalAmount(totalAmount);

// ===== PAYABLE / DEPOSIT FOR PRE_ORDER =====
        long payableAmount;

        if (saleType == SaleType.PRE_ORDER) {

            long minDeposit = DepositPolicy.minDeposit(totalAmount);

            Long requestedDeposit = dto.getDepositAmount();
            long depositToPay = (requestedDeposit == null) ? minDeposit : requestedDeposit;

            try {
                DepositPolicy.validate(totalAmount, depositToPay);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(ex.getMessage());
            }

            order.setDeposit(depositToPay);
            order.setRemainingAmount(totalAmount - depositToPay);

            payableAmount = depositToPay;

        } else {
            // IN_STOCK: không cho nhập depositAmount
            if (dto.getDepositAmount() != null) {
                throw new BadRequestException("depositAmount is only applicable for PRE_ORDER");
            }
            payableAmount = totalAmount;
        }

        // ===== CREATE PAYMENT =====
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(dto.getPaymentMethod());
        payment.setAmount(payableAmount);

        // - PRE_ORDER nếu trả cọc (< total) thì cọc nên trả online (VNPAY). COD chỉ hợp lý cho phần còn lại.
// - PRE_ORDER trả 100% thì method có thể COD hoặc VNPAY (tuỳ bạn muốn siết thêm hay không).
        if (saleType == SaleType.PRE_ORDER) {
            long depositToPay = payableAmount; // payableAmount đã là deposit hoặc full sau khi bạn chỉnh DepositPolicy

            if (depositToPay < totalAmount && dto.getPaymentMethod() == PaymentMethod.COD) {
                throw new BadRequestException("For PRE_ORDER, deposit must be paid online (VNPAY). COD is for remaining.");
            }

            // remaining method default COD nếu FE không gửi
            PaymentMethod remainingMethod =
                    dto.getRemainingPaymentMethod() == null ? PaymentMethod.COD : dto.getRemainingPaymentMethod();
            order.setRemainingPaymentMethod(remainingMethod);
        }

// 1) Initial payment record (FULL hoặc DEPOSIT)
        Payment initialPayment = new Payment();
        initialPayment.setOrder(order);

        PaymentStage initialStage;
        if (saleType == SaleType.IN_STOCK) initialStage = PaymentStage.FULL;
        else initialStage = (payableAmount == totalAmount) ? PaymentStage.FULL : PaymentStage.DEPOSIT;

        initialPayment.setStage(initialStage);
        initialPayment.setMethod(dto.getPaymentMethod());
        initialPayment.setAmount(payableAmount);

        if (dto.getPaymentMethod() == PaymentMethod.COD) {
            initialPayment.setStatus(PaymentStatus.UNPAID);
        } else {
            initialPayment.setStatus(PaymentStatus.PENDING);
        }
        initialPayment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(initialPayment);

// 2) Nếu PRE_ORDER còn lại và remaining = COD => tạo record REMAINING COD để thu khi giao
        if (saleType == SaleType.PRE_ORDER) {
            long remaining = order.getRemainingAmount() == null ? 0L : order.getRemainingAmount();
            if (remaining > 0 && order.getRemainingPaymentMethod() == PaymentMethod.COD) {

                Payment remainingCOD = new Payment();
                remainingCOD.setOrder(order);
                remainingCOD.setStage(PaymentStage.REMAINING);
                remainingCOD.setMethod(PaymentMethod.COD);
                remainingCOD.setAmount(remaining);
                remainingCOD.setStatus(PaymentStatus.UNPAID);
                remainingCOD.setCreatedAt(LocalDateTime.now());

                paymentRepository.save(remainingCOD);
            }
        }

        // ===== CREATE SHIPMENT =====
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setStatus(ShipmentStatus.WAITING_CONFIRM);
        shipmentRepository.save(shipment);
        order.setShipment(shipment);

        cart.setStatus(CartStatus.CHECKED_OUT);

        // ===== VNPAY =====
        String paymentUrl = null;

        if (dto.getPaymentMethod() == PaymentMethod.VNPAY) {

            // 🔥 LẤY IP CHUẨN
            String ipAddress = request.getRemoteAddr();
            if (ipAddress == null || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                ipAddress = "127.0.0.1";
            }

            try {
                paymentUrl = vnPayService.createVNPayUrl(
                        initialPayment.getId().toString(),
                        payableAmount,
                        ipAddress
                );
            } catch (Exception e) {
                throw new RuntimeException("Cannot create VNPay URL");
            }
        }

        return new CheckoutResponseDTO(
                order.getId(),
                order.getOrderCode(),
                payableAmount,
                dto.getPaymentMethod(),
                paymentUrl
        );
    }

    private Address resolveAddress(Long userId, Long addressId) {
        if (addressId != null) {
            return addressRepository
                    .findByIdAndUser_Id(addressId, userId)
                    .orElseThrow(() -> new BadRequestException("Address not found"));
        }

        return addressRepository
                .findFirstByUser_IdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new BadRequestException("No default address"));
    }
}
