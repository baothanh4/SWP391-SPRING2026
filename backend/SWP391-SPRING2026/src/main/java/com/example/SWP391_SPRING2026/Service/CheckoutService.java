package com.example.SWP391_SPRING2026.Service;

import com.example.SWP391_SPRING2026.Entity.*;
import com.example.SWP391_SPRING2026.Enum.*;
import com.example.SWP391_SPRING2026.Exception.BadRequestException;
import com.example.SWP391_SPRING2026.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutService {
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository  orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository  productVariantRepository;
    private final PreOrderRepository preOrderRepository;

    public Order checkout(Long userId,Long addressId){
        Cart cart=cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE).orElseThrow(()->new BadRequestException("Cart not found"));

        if(cart.getItems().isEmpty()){
            throw new BadRequestException("Cart items is empty");
        }

        Address address = addressRepository.findByIdAndUser_Id(addressId,userId).orElseThrow(()->new BadRequestException("Address not found"));

        SaleType saleType = null;

        for(CartItem item :  cart.getItems()){
            SaleType itemType = item.getProductVariant().getSaleType();

            if(saleType==null){
                saleType = itemType;
            }else if(saleType != itemType){
                throw new RuntimeException("Cannot checkout mixed IN_STOCK and PRE_ORDER products");
            }
        }

        Order order = new Order();
        order.setOrderCode("ORD-"+System.currentTimeMillis());
        order.setOrderType(saleType == SaleType.IN_STOCK ? OrderType.IN_STOCK : OrderType.PRE_ORDER);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        long totalAmount = 0;

        orderRepository.save(order);

        for(CartItem cartItem :  cart.getItems()){
            ProductVariant variant = productVariantRepository.lockById(cartItem.getProductVariant().getId()).orElseThrow(()->new BadRequestException("Product variant not found"));

            int quantity = cartItem.getQuantity();
            long price = variant.getPrice().longValue();

            if(saleType == SaleType.IN_STOCK){
                if(variant.getStockQuantity() < quantity){
                    throw new BadRequestException("Stock quantity less than stock quantity");
                }
                variant.setStockQuantity(variant.getStockQuantity() - quantity);
            }

            OrderItems orderItems = new OrderItems();
            orderItems.setOrder(order);
            orderItems.setProductVariant(variant);
            orderItems.setQuantity(quantity);
            orderItems.setPrice(price);
            orderItems.setIsCombo(false);

            orderItemRepository.save(orderItems);

            totalAmount += price * quantity;

            if(saleType == SaleType.PRE_ORDER){
                PreOrder preOrder = new PreOrder();
                preOrder.setOrder(order);
                preOrder.setProductVariant(variant);
                preOrder.setExpectedReleaseDate(LocalDate.now().plusMonths(1));
                preOrder.setDepositAmount(price * quantity * 30/100);
                preOrder.setPreorderStatus(PreOrderStatus.WAITING);

                preOrderRepository.save(preOrder);
            }
        }

        order.setTotalAmount(totalAmount);

        if(saleType == SaleType.PRE_ORDER){
            long deposit = totalAmount *30/100;
            order.setDeposit(deposit);
            order.setRemainingAmount(totalAmount - deposit);
        }

        cart.setStatus(CartStatus.CHECKED_OUT);

        return order;


    }
}
