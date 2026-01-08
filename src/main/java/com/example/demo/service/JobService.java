package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.JobAllResponse;
import com.example.demo.payload.Request.JobRequest;
import com.example.demo.payload.Response.JobResponse;
import com.example.demo.payload.Response.ResponsePageBase;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.FilterJob;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class JobService {
    private static String[] positionEnum={
            "Internship",
            "Fresher",
            "Junior",
            "Senior",
            "Lead",
            "Manager"
    };
    private final JobRepository jobRepository;
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final TagDetailService tagDetailService;
    public JobService(
            JobRepository jobRepository,
            CategoryService categoryService,
            ModelMapper modelMapper,
            UserRepository userRepository,
            TagService tagService,
            TagDetailService tagDetailService
    ){
        this.jobRepository= jobRepository;
        this.categoryService= categoryService;
        this.modelMapper= modelMapper;
        this.userRepository= userRepository;
        this.tagService= tagService;
        this.tagDetailService= tagDetailService;
    }

    public Job getJobEntityById(UUID id){
        Job job= jobRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFound("Job not found"));
        if(job.getStatus().equals("CLOSED")){
            throw new ResourceNotFound("Job is closed");
        }
        return job;
    }

//    GETBYID
    public JobResponse getJobById(UUID id){
        Job job= getJobEntityById(id);
        User user= job.getUser();
        JobResponse jobResponse = modelMapper.map(job, JobResponse.class);
        List<SelectedTag> selectedTags= tagDetailService.getTagsByJobId(id);

//        Get All Selected Tags
        List<Tag> tags= tagService.getAllTagsByIds(
                selectedTags.stream()
                        .map(record -> record.getTag().getId())
                        .toList()
        );
//        Set each field
        jobResponse.setCategoryName(job.getCategory().getTitle());
        jobResponse.setCompanyName(user.getCompany().getTitle());
        jobResponse.setCompanyId(user.getCompany().getId().toString());
        jobResponse.setTags(
                tags.stream()
                        .map(record->{
                            Map<String,String> mp= new HashMap<>();
                            mp.put("id",record.getId().toString());
                            mp.put("title",record.getTitle());
                            return mp;
                        })
                        .toList()
        );
        return jobResponse;
    }
//    GET ALL JOB
    public ResponsePageBase<JobAllResponse> getAllJobs(
            int page,
            int size,
            String keyword,
            String sortKey,
            String sortValue,
            Integer postion,
            Integer salary
    ){
        Specification<Job> spec= FilterJob.filter(salary,postion,keyword);
        Sort sort= sortValue.equalsIgnoreCase("asc") ?
                Sort.by(sortKey).ascending():
                Sort.by(sortKey).descending();

        Pageable pageable= PageRequest.of(page-1,size,sort);
        Page<Job> jobs= jobRepository.findAllByStatus("OPEN",spec,pageable);

        List<JobAllResponse> listJobDTo = jobs.getContent().stream()
                .map(record->{
                    JobAllResponse item= modelMapper.map(record,JobAllResponse.class);
                    item.setCompanyName(record.getUser().getCompany().getTitle());
                    item.setCategoryName(record.getCategory().getTitle());
                    return item;
                })
                .toList();
        ResponsePageBase<JobAllResponse> responses=ResponsePageBase.<JobAllResponse>builder()
                .content(listJobDTo)
                .currentPage(jobs.getNumber())
                .totalPage(jobs.getTotalPages())
                .build();
        return responses;
    }

    @Transactional
    public void createJob(JobRequest jobRequest,String email){
        Category category = categoryService.getCategoryById(UUID.fromString(jobRequest.getCategoryId()));
        User user= userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFound("User not found"));
        Job job= modelMapper.map(jobRequest,Job.class);
        job.setId(null);
        job.setPosition(positionEnum[jobRequest.getPosition()]);
        job.setCategory(category);
        job.setUser(user);
        jobRepository.save(job);

        if(jobRequest.getTagsId() != null && jobRequest.getTagsId().size() > 0){
            List<Tag> tags= tagService.getAllTagsByIds(
                    jobRequest.getTagsId()
                            .stream()
                            .map(record -> UUID.fromString(record))
                            .toList()
            );

            List<SelectedTag> selectedTags = tags.stream()
                    .map(record -> new SelectedTag(job,record))
                    .toList();
            tagDetailService.saveSelectedTags(selectedTags);
        }


    }

}
