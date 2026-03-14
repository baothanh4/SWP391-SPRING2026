package com.example.SWP391_SPRING2026.Repository;

import com.example.SWP391_SPRING2026.DTO.Response.BestSellerDTO;
import com.example.SWP391_SPRING2026.Entity.OrderItems;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems, Long> {

    // lock + check item thuộc user
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select oi from OrderItems oi
        join oi.order o
        join o.address a
        join a.user u
        where oi.id = :orderItemId and u.id = :userId
    """)
    Optional<OrderItems> lockOwnedItem(Long orderItemId, Long userId);

    // dùng cho support/operation lock theo id
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select oi from OrderItems oi where oi.id = :id")
    java.util.Optional<OrderItems> lockById(Long id);


    @Query("""
    SELECT new com.example.SWP391_SPRING2026.DTO.Response.BestSellerDTO(
        p.name,
        SUM(oi.quantity)
    )
    FROM OrderItems oi
    JOIN oi.productVariant pv
    JOIN pv.product p
    GROUP BY p.name
    ORDER BY SUM(oi.quantity) DESC
    """)
    List<BestSellerDTO> findBestSellers(Pageable pageable);
}