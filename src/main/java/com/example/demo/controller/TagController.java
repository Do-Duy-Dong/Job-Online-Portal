package com.example.demo.controller;

import com.example.demo.entity.Tag;
import com.example.demo.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/api/tag")
@Controller
public class TagController {
    private final TagService tagService;
    public TagController(TagService tagService){
        this.tagService= tagService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTag(
            @RequestBody Map<String,String> request){
        tagService.createTag(request.get("title"));
        return ResponseEntity.ok("Tag created successfully");
    }

    @PostMapping("/getTags")
    public ResponseEntity<?> getAllTags(){
        return ResponseEntity.ok(tagService.getAllTags());
    }

}
