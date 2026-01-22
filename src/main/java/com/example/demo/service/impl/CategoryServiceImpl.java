package com.example.demo.service.impl;

import com.example.demo.dto.request.CategoryRequest;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
import jakarta.transaction.Transactional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    private  final CategoryRepository categoryRepository;
    private final Path uploadPath = Paths.get("uploads/categories");

    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
        try{
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory",e);
        }
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request){
        if(categoryRepository.existsByName(request.getName())){
            throw new RuntimeException("Category already exists");
        }
        Category  category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if(request.getImage() != null && !request.getImage().isEmpty()){
            category.setImageUrl(uploadImage(request.getImage()));
        }
        Category saved= categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request){
        Category category = categoryRepository.findById(id).orElseThrow(()->  new UsernameNotFoundException("Category not found"+id));
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if(request.getImage() != null && !request.getImage().isEmpty()){
            //Delete old image
            if(category.getImageUrl() != null){
                deleteImage(category.getImageUrl());
            }
            category.setImageUrl(uploadImage(request.getImage()));
        }
        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }
    @Override
    @Transactional
    public void deleteCategory(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(()-> new UsernameNotFoundException("Category not found"));
        if(category.getImageUrl() != null){
            deleteImage(category.getImageUrl());
        }
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories(){
        return categoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(()-> new UsernameNotFoundException("Category not found"));
        return mapToResponse(category);
    }

    @Override
    public List<CategoryResponse> getActiveCategories(){
        return getActiveCategories();
    }

    private String uploadImage(MultipartFile file){
        try {
            String fileName = UUID.randomUUID() + "_" +
                    StringUtils.cleanPath(file.getOriginalFilename());
            Path target = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/categories/" + fileName;
        } catch (IOException e){
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }


    private void deleteImage(String imageUrl){
        try{
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
            Path filePath = uploadPath.resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch(IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    private CategoryResponse mapToResponse(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
