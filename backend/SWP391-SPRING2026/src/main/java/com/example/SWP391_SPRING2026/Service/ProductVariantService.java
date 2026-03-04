package com.example.SWP391_SPRING2026.Service;


import com.example.SWP391_SPRING2026.DTO.Request.ProductVariantRequestDTO;
import com.example.SWP391_SPRING2026.DTO.Response.ProductVariantResponseDTO;
import com.example.SWP391_SPRING2026.Entity.Product;
import com.example.SWP391_SPRING2026.Entity.ProductVariant;
import com.example.SWP391_SPRING2026.Repository.ProductRepository;
import com.example.SWP391_SPRING2026.Repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductVariantResponseDTO create(Long productId, ProductVariantRequestDTO dto){
        Product product = productRepository.findById(productId).orElseThrow(()->new RuntimeException("Product Not Found"));

        ProductVariant productVariant = new ProductVariant();

        productVariant.setSku(dto.getSku());
        productVariant.setPrice(dto.getPrice());
        productVariant.setStockQuantity(dto.getStockQuantity());
        productVariant.setSaleType(dto.getSaleType());
        productVariant.setProduct(product);

        productVariantRepository.save(productVariant);

        return toDTO(productVariant);
    }

    public ProductVariantResponseDTO update(Long variantId, ProductVariantRequestDTO dto){
        ProductVariant variant =  productVariantRepository.findById(variantId).orElseThrow(()->new RuntimeException("Product Variant Not Found"));

        variant.setSku(dto.getSku());
        variant.setPrice(dto.getPrice());
        variant.setStockQuantity(dto.getStockQuantity());
        variant.setSaleType(dto.getSaleType());
        productVariantRepository.save(variant);
        return toDTO(variant);
    }

    public List<ProductVariantResponseDTO> getAllVariants(){
        List<ProductVariant> productVariants = productVariantRepository.findAll();

        return productVariants.stream().map(v -> ProductVariantResponseDTO.builder()
                .id(v.getId())
                .sku(v.getSku())
                .price(v.getPrice())
                .stockQuantity(v.getStockQuantity())
                .saleType(v.getSaleType())
                .product_id(v.getProduct().getId())
                .build()).collect(Collectors.toList());
    }

    public void delete(Long variantId){
        productVariantRepository.deleteById(variantId);
    }

    private ProductVariantResponseDTO toDTO(ProductVariant productVariant){
        ProductVariantResponseDTO productVariantResponseDTO = new ProductVariantResponseDTO();
        productVariantResponseDTO.setId(productVariant.getId());
        productVariantResponseDTO.setSku(productVariant.getSku());
        productVariantResponseDTO.setPrice(productVariant.getPrice());
        productVariantResponseDTO.setStockQuantity(productVariant.getStockQuantity());
        productVariantResponseDTO.setSaleType(productVariant.getSaleType());
        productVariantResponseDTO.setProduct_id(productVariant.getProduct().getId());
        return productVariantResponseDTO;
    }
}
