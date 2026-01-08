package com.example.demo.service;

import com.example.demo.entity.CV;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.CvResponse;
import com.example.demo.repository.CVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Service
public class CvService {
    @Autowired
    private CVRepository cvRepository;

    public void save(CV cv){
         cvRepository.save(cv);
    }
    public CV findById(UUID id){
        return cvRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFound("CV not found"));
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
}
