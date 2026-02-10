package com.example.SWP391_SPRING2026.Entity;


import com.example.SWP391_SPRING2026.Enum.PreOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "pre_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "expected_release_date")
    private LocalDate expectedReleaseDate;

    @Column(name = "deposit_amount")
    private Long depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pre_order_status")
    private PreOrderStatus preorderStatus;
}
