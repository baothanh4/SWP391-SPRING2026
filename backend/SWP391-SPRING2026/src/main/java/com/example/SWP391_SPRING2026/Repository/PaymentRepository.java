package com.example.SWP391_SPRING2026.Repository;

import com.example.SWP391_SPRING2026.Entity.Payment;
import com.example.SWP391_SPRING2026.Enum.PaymentStage;
import com.example.SWP391_SPRING2026.Enum.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder_Id(Long orderId);

    Optional<Payment> findTopByOrder_IdAndStageOrderByIdDesc(Long orderId, PaymentStage stage);

    boolean existsByOrder_IdAndStatus(Long orderId, PaymentStatus status);

    List<Payment> findByStatusAndExpiresAtBefore(PaymentStatus status, LocalDateTime now);

    @Query("""
        SELECT p FROM Payment p
        JOIN p.order o
        WHERE o.address.user.id = :userId
        ORDER BY p.createdAt DESC
    """)
    List<Payment> findByUserId(Long userId);


    @Query("""
    SELECT COALESCE(SUM(p.amount),0)
    FROM Payment p
    WHERE p.status = 'SUCCESS'
    AND p.paidAt BETWEEN :from AND :to
    """)
    BigDecimal getRevenue(LocalDateTime from, LocalDateTime to);

    @Query("""
SELECT 
    EXTRACT(MONTH FROM p.paidAt),
    EXTRACT(YEAR FROM p.paidAt),
    SUM(p.amount)
FROM Payment p
WHERE p.status = 'SUCCESS'
AND p.paidAt BETWEEN :from AND :to
GROUP BY EXTRACT(YEAR FROM p.paidAt), EXTRACT(MONTH FROM p.paidAt)
ORDER BY EXTRACT(YEAR FROM p.paidAt), EXTRACT(MONTH FROM p.paidAt)
""")
    List<Object[]> getRevenueByMonth(LocalDateTime from, LocalDateTime to);

    @Query("""
SELECT 
    EXTRACT(QUARTER FROM p.paidAt),
    EXTRACT(YEAR FROM p.paidAt),
    SUM(p.amount)
FROM Payment p
WHERE p.status = 'SUCCESS'
AND p.paidAt BETWEEN :from AND :to
GROUP BY EXTRACT(YEAR FROM p.paidAt), EXTRACT(QUARTER FROM p.paidAt)
ORDER BY EXTRACT(YEAR FROM p.paidAt), EXTRACT(QUARTER FROM p.paidAt)
""")
    List<Object[]> getRevenueByQuarter(LocalDateTime from, LocalDateTime to);

    @Query("""
    SELECT p.stage, SUM(p.amount)
    FROM Payment p
    WHERE p.status = 'SUCCESS'
    AND p.paidAt BETWEEN :from AND :to
    GROUP BY p.stage
    """)
    List<Object[]> getRevenueByStage(LocalDateTime from, LocalDateTime to);


    @Query("""
    SELECT p.status, COUNT(p)
    FROM Payment p
    WHERE p.createdAt BETWEEN :from AND :to
    GROUP BY p.status
    """)
    List<Object[]> getPaymentStats(LocalDateTime from, LocalDateTime to);
}