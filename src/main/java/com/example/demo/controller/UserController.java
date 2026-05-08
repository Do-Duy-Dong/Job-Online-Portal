package com.example.demo.controller;

import com.example.demo.payload.Request.MetaDataFile;
import com.example.demo.payload.Response.CvUploadReponse;
import com.example.demo.payload.Response.UserResponseDTO;
import com.example.demo.repository.JobRepository;
import com.example.demo.service.CvService;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Controller
public class UserController {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private final UserService userService;
    private final CvService cvService;
    private final JobRepository jobRepository;
    public UserController(UserService userService,
    JobRepository jobRepository,
    CvService cvService
    ){
        this.userService= userService;
        this.jobRepository= jobRepository;
        this.cvService= cvService;
    }

    @GetMapping("/api/user/profile")
    public ResponseEntity<?> getProfile(
            Authentication authentication
    ){
        try {
            String email= authentication.getName();
            UserResponseDTO dto= userService.profile(email);
            return ResponseEntity.ok(dto);
        }catch (Exception e){
            return ResponseEntity.status(400).body("error fetching profile");
        }
    }
    @GetMapping("/api/user/list-cv")
    public ResponseEntity<?> listCv(
            Authentication authentication
    ){
        return ResponseEntity.ok(
                userService.listCv(authentication.getName())
        );
    }

    @PostMapping(
            value= "/api/user/upload"
    )
    public ResponseEntity<?> uploadFile(
            @RequestBody MetaDataFile metaDataFile,
            Authentication authentication
    ){
        String email= authentication.getName();
//        userService.uploadCv(email,file);
        CvUploadReponse url=cvService.uploadToCLoudByPresign(email,metaDataFile);
        return ResponseEntity.ok(url);
    }
    @PostMapping("/api/user/confirmCv/{s3Key}")
    public ResponseEntity<?> confirmCv(
            @PathVariable String s3Key,
            Authentication authentication
    ){
        String email= authentication.getName();
        cvService.confirmUpload(s3Key,email);
        return ResponseEntity.ok("cv uploaded successfully");
    }
    @DeleteMapping("/api/user/delete-cv/{cvId}")
    public ResponseEntity<?> deleteCv(
            @PathVariable String cvId,
            Authentication authentication
    ){
        String email= authentication.getName();
        userService.deleteCv(cvId,email);
        return ResponseEntity.ok("cv deleted");
    }
    @GetMapping("/api/user/cv-url/{fileName}")
    public ResponseEntity<?> getCvUrl(
            @PathVariable String fileName,
            Authentication authentication
    ){
        String url = cvService.getSignUrl(fileName);
        if(url==null){
            return ResponseEntity.status(404).body("cv not found");
        }
        return ResponseEntity.ok(url);
    }

    @GetMapping("/api/file/look/{fileName}")
    public ResponseEntity<?> lookFile(
            @PathVariable String fileName
    ){
        try {
            Path path= Paths.get(uploadDir).resolve(fileName).normalize();
            log.info("File Path: {}", path.toUri().toString());
            Resource resource = new UrlResource(path.toUri());
            if(!resource.exists()){
                return ResponseEntity.status(404).body("file not founds");
            } else {
                log.info("Content Type: {}", MediaType.APPLICATION_PDF);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body("error loading file");
        }
    }

    @PostMapping(
            value= "/api/user/test"
    )
    public ResponseEntity<?> test(

    ){
        try{
            int num= jobRepository.updateExpiredJobs(10);
            return ResponseEntity.ok(num);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("error updating jobs");
        }
    }

}
