package com.final_project.product_service.controller;

import com.final_project.product_service.dto.request.ProductRequest;
import com.final_project.product_service.dto.response.BaseResponse;
import com.final_project.product_service.entity.Product;
import com.final_project.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<BaseResponse<Product>> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(201, "Product berhasil dibuat", product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<Product>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new BaseResponse<>(200, "Berhasil mengambil data product", product));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<Product>>> getProducts(@RequestParam(required = false) String huruf) {
        List<Product> products = productService.getProducts(huruf);
        return ResponseEntity.ok(new BaseResponse<>(200, "Berhasil mengambil daftar product", products));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Product>> updateProduct(@PathVariable Long id,
                                                              @Valid @RequestBody ProductRequest request) {
        Product updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(new BaseResponse<>(200, "Product berhasil diperbarui", updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new BaseResponse<>(200, "Product berhasil dihapus", null));
    }
}
