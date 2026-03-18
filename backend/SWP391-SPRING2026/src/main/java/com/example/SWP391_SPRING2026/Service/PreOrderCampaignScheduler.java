package com.example.SWP391_SPRING2026.Service;

import com.example.SWP391_SPRING2026.Entity.ProductVariant;
import com.example.SWP391_SPRING2026.Enum.SaleType;
import com.example.SWP391_SPRING2026.Repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreOrderCampaignScheduler {

    private final ProductVariantRepository productVariantRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void autoCloseExpiredPreOrders() {
        LocalDate today = LocalDate.now();

        List<ProductVariant> expiredVariants =
                productVariantRepository.findBySaleTypeAndAllowPreorderTrueAndPreorderEndDateBefore(
                        SaleType.PRE_ORDER,
                        today
                );

        for (ProductVariant variant : expiredVariants) {
            variant.setAllowPreorder(false);
        }

        productVariantRepository.saveAll(expiredVariants);
    }
}