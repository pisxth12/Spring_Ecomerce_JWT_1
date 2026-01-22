package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 charectors")
    private String name;

    @Size(max = 500 , message = "Description too long")
    private String description;

    private MultipartFile image;
}
