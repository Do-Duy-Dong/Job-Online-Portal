package com.example.demo.controller;

import com.example.demo.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/api/category")
@Controller
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService){
        this.categoryService= categoryService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createCategory(
            @RequestBody Map<String,String> request
            ){
        try{
            System.out.println(request.get("title"));
            categoryService.createCategory(request.get("title"));
            return ResponseEntity.ok("Category created successfully");
        }
        catch(Exception e){
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
