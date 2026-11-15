package com.final_project.product_service.service;

import com.final_project.product_service.dto.request.ProductRequest;
import com.final_project.product_service.entity.Product;

import java.util.List;

public interface ProductService {

    Product createProduct(ProductRequest request);

    Product getProductById(Long id);

    List<Product> getProducts(String huruf);

    Product updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
