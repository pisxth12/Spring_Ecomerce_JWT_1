package com.example.demo.service.impl;

import com.example.demo.dto.request.ProductRequest;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.dto.response.ProductResponse;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final Path uploadPath = Paths.get("uploads/products");


    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request){
        // Get current user from security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        //Find category
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(()-> new ResourceNotFoundException("Category not found!"));

        //Create product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(category);
        product.setIsActive(request.getIsActive());

        //Handle main image
        if(request.getMainImage() != null && !request.getMainImage().isEmpty()){
            product.setMainImageUrl(uploadImage(request.getMainImage()));
        }

        // Handle additional images
        if(request.getAdditionalImages() != null ){
            List<ProductImage> productImages = new ArrayList<>();
            int order =1 ;
            for(MultipartFile file : request.getAdditionalImages()){
                if(!file.isEmpty()){
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(uploadImage(file));
                    productImage.setDisplayOrder(order++);
                    productImage.setProduct(product);
                    productImages.add(productImage);
                }
            }
            product.setImages(productImages);
        }
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }


    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request){
        Product product = productRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Product not found"));
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(()-> new ResourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(category);
        product.setIsActive(request.getIsActive());

        // Update main image if provided
        if(request.getMainImage() != null && !request.getMainImage().isEmpty()){
            deleteOldImage(product.getMainImageUrl());
            product.setMainImageUrl(uploadImage(request.getMainImage()));
        }

        // Update additional images
        if(request.getAdditionalImages() != null){
            // Clear existing images
            product.getImages().clear();

            int order = 1;
            for(MultipartFile file : request.getAdditionalImages()){
                if(!file.isEmpty()){
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(uploadImage(file));
                    productImage.setDisplayOrder(order++);
                    productImage.setProduct(product);
                    product.getImages().add(productImage);
                }
            }
        }
        Product updated =  productRepository.save(product);
        return mapToResponse(updated);
    }



    @Override
    @Transactional
    public void deleteProduct(Long id){
        Product product = productRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Product not found"));

        //Delete  images
        deleteOldImage(product.getMainImageUrl());
        for (ProductImage image : product.getImages()){
            deleteOldImage(image.getImageUrl());
        }
        productRepository.delete(product);
    }

    @Override
    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword){
        return productRepository.findAll().stream()
                .filter(p->p.getName().toLowerCase().contains(keyword.toLowerCase()) || p.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::mapToResponse)
                .toList();
    }





    private String uploadImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" +
                    StringUtils.cleanPath(file.getOriginalFilename());
            Path target = uploadPath.resolve(fileName);

            // DEBUG: Check paths
            System.out.println("=== DEBUG: FILE UPLOAD ===");
            System.out.println("Project Directory: " + System.getProperty("user.dir"));
            System.out.println("Upload Path: " + uploadPath.toAbsolutePath());
            System.out.println("Target File: " + target.toAbsolutePath());
            System.out.println("File exists before save: " + Files.exists(target));

            // Create directory if doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created directory: " + uploadPath.toAbsolutePath());
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // Verify after save
            boolean saved = Files.exists(target);
            System.out.println("File saved: " + saved);
            if (saved) {
                System.out.println("File size: " + Files.size(target));
            }

            String relativePath = "/uploads/products/" + fileName;
            System.out.println("Returning relative path: " + relativePath);
            System.out.println("=== END DEBUG ===");

            return relativePath;

        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    private void deleteOldImage(String imageUrl){
        if(imageUrl != null){
            try{
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
                Path filePath = uploadPath.resolve(fileName);
                Files.deleteIfExists(filePath);
            }catch (IOException e){
                System.err.println("Failed to delete image: "+e.getMessage());
            }
        }
    }

    public ProductResponse mapToResponse(Product product){
        List<String> images = new ArrayList<>();
        if(product.getMainImageUrl() != null){
            images.add(product.getMainImageUrl());
        }
        images.addAll(product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .toList()
        );

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getSku(),
            product.getMainImageUrl(),
            product.getIsActive(), new CategoryResponse(
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getCategory().getDescription(),
                    product.getCategory().getImageUrl(),
                    product.getCategory().getCreatedAt(),
                    product.getCategory().getUpdatedAt()
        ),
                images,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

}
