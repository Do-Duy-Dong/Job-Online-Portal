package com.example.demo.service;

import com.example.demo.entity.CV;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.CvResponse;
import com.example.demo.repository.CVRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvService {
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private final CVRepository cvRepository;
    private final S3Client s3Client;
    private final UserRepository userRepository;

    public void save(CV cv){
         cvRepository.save(cv);
    }
    public CV findById(UUID id){
        return cvRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFound("CV not found"));
    }
    public List<CV> findAllByUserId(UUID userId){
        return cvRepository.findAllByEmployee_IdAndIsActiveTrue(userId);
    }
    public List<CvResponse> CvResponses (UUID userId ){
        List<CV> cvs= cvRepository.findAllByEmployee_IdAndIsActiveTrue(userId);
        List<CvResponse> cvResponseList= cvs.stream()
                .map(cv->{
                    String url= ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/file/look/")
                            .path(cv.getUrl())
                            .toUriString();
                    CvResponse record= new CvResponse(
                            cv.getId(),
                            url
                    );
                    return record;
                })
                .toList();
        return cvResponseList;
    }
    public void delete(CV cv){
        cvRepository.delete(cv);
    }
    public void uploadToCloud(String username, MultipartFile file){
//         todo upload to aws s3
        User user= userRepository.findByEmail(username)
                .orElseThrow(()-> new ResourceNotFound("User not found"));
        String fileName= username + "_" + file.getOriginalFilename().substring(0,10)+ "_"+ LocalDateTime.now();
        PutObjectRequest putObjectRequest= PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();
        try{
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
                CV cv= new CV();
                cv.setEmployee(user);
                cv.setUrl(fileName);
                save(cv);

        }catch (Exception e){
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }
    @Transactional
    public void deleteFromCloud(CV cv){
        try{
            DeleteObjectRequest deleteObjectRequest= DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cv.getUrl())
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            cvRepository.delete(cv);
        }catch (Exception e){
            log.error("Error deleting file from S3", e);
            throw new RuntimeException("Failed to delete file", e);
        }

    }

//    Local disk
    public String getUrlByUserId(UUID userId){
        CV cv= cvRepository.findByEmployee_IdAndIsActiveTrue(userId)
                .orElseThrow(()-> new ResourceNotFound("Active CV not found"));
        String url= ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/file/look/")
                .path(cv.getUrl())
                .toUriString();
        return url;
    }

}
