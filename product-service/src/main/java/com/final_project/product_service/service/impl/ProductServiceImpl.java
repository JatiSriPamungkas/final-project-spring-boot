package com.final_project.product_service.service.impl;

import com.final_project.product_service.dto.request.ProductRequest;
import com.final_project.product_service.entity.Product;
import com.final_project.product_service.repository.ProductRepository;
import com.final_project.product_service.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductRequest request) {
        AnalysisResult analysis = analyzeKalimat(request.getKalimat());

        Product product = Product.builder()
                .kalimat(request.getKalimat())
                .totalHuruf(analysis.totalHuruf())
                .frekuensi(analysis.frekuensi())
                .build();

        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product tidak ditemukan dengan ID: " + id));
    }

    @Override
    public List<Product> getProducts(String huruf) {
        if (huruf != null && !huruf.trim().isEmpty()) {
            return productRepository.findByKalimatContainingIgnoreCase(huruf.trim());
        }
        return productRepository.findAll();
    }

    @Override
    public Product updateProduct(Long id, ProductRequest request) {
        Product existingProduct = getProductById(id);

        AnalysisResult analysis = analyzeKalimat(request.getKalimat());
        existingProduct.setKalimat(request.getKalimat());
        existingProduct.setTotalHuruf(analysis.totalHuruf());
        existingProduct.setFrekuensi(analysis.frekuensi());

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product existingProduct = getProductById(id);
        productRepository.delete(existingProduct);
    }

    private AnalysisResult analyzeKalimat(String kalimat) {
        int totalHuruf = 0;
        Map<String, Integer> frekuensi = new TreeMap<>();

        if (kalimat != null) {
            String lower = kalimat.toLowerCase();
            for (char c : lower.toCharArray()) {
                if (c >= 'a' && c <= 'z') {
                    totalHuruf++;
                    String charStr = String.valueOf(c);
                    frekuensi.put(charStr, frekuensi.getOrDefault(charStr, 0) + 1);
                }
            }
        }

        return new AnalysisResult(totalHuruf, frekuensi);
    }

    private record AnalysisResult(int totalHuruf, Map<String, Integer> frekuensi) {}
}
