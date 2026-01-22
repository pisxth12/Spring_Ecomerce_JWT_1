package com.example.demo.controller;

import com.example.demo.dto.request.ProductRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ProductResponse;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts(){
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse(true, "Products retrieved", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProduct(@PathVariable Long id){
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product retrieved", product));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse> getProductsByCategory(@PathVariable Long categoryId){
        List<ProductResponse> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse(true, "Product retrieved", products));
    }

    @GetMapping("/{search}")
    public ResponseEntity<ApiResponse> searchProducts(@RequestParam String keyword){
        List<ProductResponse> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(new ApiResponse(true, "Search result", products));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createProduct(@Valid @ModelAttribute ProductRequest request){
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.ok(new ApiResponse(true,"Product created", product ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id,@Valid @ModelAttribute ProductRequest request){
        ProductResponse product = productService.updateProduct(id , request);
        return ResponseEntity.ok(new ApiResponse(true, "Product updated success", product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse(true,"Product deleted", null ));
    }



}
