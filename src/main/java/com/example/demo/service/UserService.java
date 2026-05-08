package com.example.demo.service;

import com.example.demo.entity.CV;
import com.example.demo.entity.Employee;
import com.example.demo.entity.Employer;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.CvResponse;
import com.example.demo.payload.Response.ListCvResponse;
import com.example.demo.payload.Response.UserResponseDTO;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class UserService {
    //    demo/upload
    @Value("${file.upload-dir}")
    private String pathUpload;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
    @Value("${aws.s3.domain}")
    private String domain;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final CvService cvService;
    private final EmployeeRepository employeeRepository;
    private final JobApplicationService jobApplicationService;
    private final EmployerRepository employerRepository;
    public UserService(
            CvService cvService,
            UserRepository userRepository, EmployeeRepository employeeRepository,
            EmployerRepository employerRepository,
            ModelMapper modelMapper,
            JobApplicationService jobApplicationService
    ){
        this.employeeRepository = employeeRepository;
        this.modelMapper= modelMapper;
        this.employerRepository= employerRepository;
        this.userRepository= userRepository;
        this.cvService= cvService;
        this.jobApplicationService= jobApplicationService;
    }

    public User getUserById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFound("User not found"));
    }
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFound("User not found"));
    }
    public Employer getEmployerByEmail(String email){
        return employerRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFound("Employer not found"));
    }
    public Employee getEmployeeByEmail(String email){
        return employeeRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFound("Employee not found"));
    }
    public String getCompanyNameByUserId(String email){
        Employer employer= employerRepository.findCompanyByEmployerEmail(email)
                .orElseThrow(()-> new ResourceNotFound("NOT FOUND"));
        return employer.getCompany().getTitle();
    }
    public UserResponseDTO profile(String email){
        User user= getUserByEmail(email);
        String urlCv= cvService.getUrlByUserId(user.getId());
        UserResponseDTO dto= modelMapper.map(user, UserResponseDTO.class);
        return dto;
    }

//    Cloud
    public List<String> getAllUserCv(String email){
        List<CV> cvs= cvService.findAllByUserId(getUserByEmail(email).getId());
        List<String> urls= cvs.stream().map(cv->{
            return domain+cv.getUrl();
        })
                .toList();
        return urls;
    }
    public void deleteCv(String cvId,String email){
        CV cv= cvService.findById(UUID.fromString(cvId),email);
        if(jobApplicationService.checkCvFromApplyJob(UUID.fromString(cvId))){
            cv.setActive(false);
            cvService.save(cv);
        }else{
            cvService.deleteFromCloud(cv);
        }
    }
//    local disk
    public void uploadCv(String email, MultipartFile file){
        try {
            Employee employee= getEmployeeByEmail(email);
            if(file.isEmpty() || !file.getContentType().equals("application/pdf")){{
                throw new ResourceNotFound("Invalid file");
            }}
            String originalName= StringUtils.cleanPath(file.getOriginalFilename());
            String typeFile= originalName.substring(originalName.lastIndexOf("."));
            String fileName= file.getName()+ "-" +System.currentTimeMillis()+ typeFile;
            Path folderPath= Paths.get(pathUpload);
            Path filePath= folderPath.resolve(fileName);
            Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);

            CV cv= new CV();
            cv.setEmployee(employee);
            cv.setUrl(fileName);
            cvService.save(cv);

        }
        catch (Exception e){
            e.printStackTrace();
            throw new ResourceNotFound("Could not upload file");
        }
    }
    public List<ListCvResponse> listCv(String email){
        User user= getUserByEmail(email);
        List<ListCvResponse> cvResponses= cvService.getListCvNameByUser(user.getId());
        return cvResponses;
    }
}
