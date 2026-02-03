package com.example.SWP391_SPRING2026.Repository;

import com.example.SWP391_SPRING2026.Entity.Cart;
import com.example.SWP391_SPRING2026.Enum.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("""
        select distinct c
        from Cart c
        left join fetch c.items i
        left join fetch i.productVariant v
        left join fetch v.product p
        where c.user.id = :userId and c.status = :status
    """)
    Optional<Cart> findCartWithItems(@Param("userId") Long userId,
                                     @Param("status") CartStatus status);

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
}
