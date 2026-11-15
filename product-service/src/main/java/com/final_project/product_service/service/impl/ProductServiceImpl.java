package com.final_project.product_service.service.impl;

import com.final_project.product_service.dto.request.ProductRequest;
import com.final_project.product_service.dto.response.DummyProductItem;
import com.final_project.product_service.dto.response.DummyProductResponse;
import com.final_project.product_service.entity.Product;
import com.final_project.product_service.repository.ProductRepository;
import com.final_project.product_service.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final RestClient restClient;

    @Value("${app.dummy-json.url:https://dummyjson.com/products}")
    private String dummyJsonUrl;

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(ProductRequest request) {
        log.info("Creating new product and evicting 'products' cache");
        AnalysisResult analysis = analyzeKalimat(request.getKalimat());

        Product product = Product.builder()
                .kalimat(request.getKalimat())
                .totalHuruf(analysis.totalHuruf())
                .frekuensi(analysis.frekuensi())
                .build();

        return productRepository.save(product);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product createRandomProduct() {
        log.info("Fetching random product from DummyJSON...");
        // DummyJSON memiliki 194 produk (ID 1 - 194)
        int randomId = ThreadLocalRandom.current().nextInt(1, 195);
        String targetUrl = dummyJsonUrl + "/" + randomId;

        try {
            DummyProductItem item = restClient.get()
                    .uri(targetUrl)
                    .retrieve()
                    .body(DummyProductItem.class);

            if (item != null && item.title() != null && !item.title().trim().isEmpty()) {
                log.info("Random product fetched successfully: '{}' (DummyJSON ID: {})", item.title(), item.id());
                return createProduct(new ProductRequest(item.title().trim()));
            }
        } catch (Exception e) {
            log.warn("Direct fetch by random ID ({}) failed: {}. Trying fallback with random skip...", randomId, e.getMessage());
        }

        // Fallback: ambil 1 produk secara acak menggunakan parameter skip
        try {
            int randomSkip = ThreadLocalRandom.current().nextInt(0, 190);
            DummyProductResponse response = restClient.get()
                    .uri(dummyJsonUrl + "?limit=1&skip=" + randomSkip)
                    .retrieve()
                    .body(DummyProductResponse.class);

            if (response != null && response.products() != null && !response.products().isEmpty()) {
                DummyProductItem fallbackItem = response.products().get(0);
                log.info("Fallback random product fetched: '{}'", fallbackItem.title());
                return createProduct(new ProductRequest(fallbackItem.title().trim()));
            }
        } catch (Exception ex) {
            log.error("Fallback random fetch also failed: {}", ex.getMessage());
        }

        throw new RuntimeException("Gagal mengambil data produk random dari DummyJSON");
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        log.info("Cache miss! Fetching product with ID: {} from database", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product tidak ditemukan dengan ID: " + id));
    }

    @Override
    @Cacheable(value = "products", key = "#huruf != null ? #huruf.trim().toLowerCase() : 'all'")
    public List<Product> getProducts(String huruf) {
        log.info("Cache miss! Fetching product list (filter: {}) from database", huruf);
        if (huruf != null && !huruf.trim().isEmpty()) {
            return productRepository.findByKalimatContainingIgnoreCase(huruf.trim());
        }
        return productRepository.findAll();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public Product updateProduct(Long id, ProductRequest request) {
        log.info("Updating product ID: {} and evicting relevant caches", id);
        Product existingProduct = getProductById(id);

        AnalysisResult analysis = analyzeKalimat(request.getKalimat());
        existingProduct.setKalimat(request.getKalimat());
        existingProduct.setTotalHuruf(analysis.totalHuruf());
        existingProduct.setFrekuensi(analysis.frekuensi());

        return productRepository.save(existingProduct);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void deleteProduct(Long id) {
        log.info("Deleting product ID: {} and evicting relevant caches", id);
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
