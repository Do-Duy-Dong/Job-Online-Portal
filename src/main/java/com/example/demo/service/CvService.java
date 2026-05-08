package com.example.demo.service;

import com.example.demo.entity.CV;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Employer;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Request.MetaDataFile;
import com.example.demo.payload.Response.CvResponse;
import com.example.demo.payload.Response.CvUploadReponse;
import com.example.demo.payload.Response.ListCvResponse;
import com.example.demo.repository.CVRepository;
import com.example.demo.repository.EmployeeRepository;
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
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
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
    private final S3Presigner s3Presigner;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private List<String> listFile = List.of("pdf","doc","docx");
    public void save(CV cv){
         cvRepository.save(cv);
    }
    public CV findById(UUID id,String email){
        return cvRepository.findByCvIdAndEmail(id,email)
                .orElseThrow(()-> new ResourceNotFound("CV not found"));
    }

    public List<CV> findAllByUserId(UUID userId){
        return cvRepository.findAllByEmployee_IdAndIsActiveTrue(userId);
    }
    public String getSignUrl(String fileName){
        try{
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            GetObjectPresignRequest getObjectPresignRequest= GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
        }catch (Exception e){
            log.error("Error checking file existence in S3", e);
            return null;
        }
    }
    public List<ListCvResponse> getListCvNameByUser(UUID userId){
        List<CV> cvs= findAllByUserId(userId);
        List<ListCvResponse> listCvResponse = cvs.stream().map(
                cv-> new ListCvResponse(
                        cv.getUrl(),
                        cv.getId())
        ).toList();
        return listCvResponse;
    }
    public void delete(CV cv){
        cvRepository.delete(cv);
    }
    public void uploadToCloud(String username, MultipartFile file){
//         todo upload to aws s3
        Employee user= employeeRepository.findByEmail(username)
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
    public CvUploadReponse uploadToCLoudByPresign(String username, MetaDataFile metaDataFile){
        String s3Key= username + "_" + metaDataFile.getFileName().substring(0,10)+ "_"+ LocalDateTime.now();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .contentType(metaDataFile.getFileType())
                .key(s3Key)
                .bucket(bucketName)
                .build();
        PutObjectPresignRequest presignRequest= PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest obj= s3Presigner.presignPutObject(presignRequest);

        Employee user= employeeRepository.findByEmail(username)
                .orElseThrow(()-> new ResourceNotFound("User not found"));
        cvRepository.save(
                CV.builder()
                        .employee(user)
                        .url(s3Key)
                        .contentType(metaDataFile.getFileType())
                        .isActive(false)
                        .build()

        );
        return CvUploadReponse.builder()
                .url(obj.url().toString())
                .s3Key(s3Key)
                .build();
    }
    public void confirmUpload(String s3Key, String username){
        CV cv= cvRepository.findByCvNameAndEmail(s3Key, username)
                .orElseThrow(()-> new ResourceNotFound("CV not found"));
        HeadObjectResponse headObjectResponse = s3Client.headObject(r->
                r.bucket(bucketName).key(s3Key)
        );
        if(headObjectResponse.contentLength() >= 5 * 1024*1024 || headObjectResponse.contentType() != cv.getContentType()){
            cvRepository.delete(cv);
            s3Client.deleteObject(
                    r-> r.bucket(bucketName).key(s3Key)
            );
            throw new ResourceNotFound("File size exceeds limit or content type mismatch");
        }
        cv.setActive(true);
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
