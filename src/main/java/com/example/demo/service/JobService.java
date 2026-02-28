package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.JobAllResponse;
import com.example.demo.payload.Request.JobRequest;
import com.example.demo.payload.Response.JobResponse;
import com.example.demo.payload.Response.KeysetPageResponse;
import com.example.demo.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.FilterJob;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class JobService {
    private static String[] positionEnum = {
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
            TagDetailService tagDetailService) {
        this.jobRepository = jobRepository;
        this.categoryService = categoryService;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.tagService = tagService;
        this.tagDetailService = tagDetailService;
    }

    public Job getJobEntityById(UUID id) {
        Job job = jobRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFound("Job not found"));
        if (job.getStatus().equals("CLOSED")) {
            throw new ResourceNotFound("Job is closed");
        }
        return job;
    }

    // GETBYID
    public JobResponse getJobById(UUID id) {
        Job job = getJobEntityById(id);
        User user = job.getUser();
        JobResponse jobResponse = modelMapper.map(job, JobResponse.class);
        List<SelectedTag> selectedTags = tagDetailService.getTagsByJobId(id);

        // Set each field
        jobResponse.setCategoryName(job.getCategory().getTitle());
        jobResponse.setCompanyName(user.getCompany().getTitle());
        jobResponse.setCompanyId(user.getCompany().getId().toString());
        jobResponse.setTags(
                selectedTags.stream()
                        .map(record -> {
                            Map<String, String> mp = new HashMap<>();
                            mp.put("id", record.getTag().getId().toString());
                            mp.put("title", record.getTag().getTitle());
                            return mp;
                        })
                        .toList());
        return jobResponse;
    }

    // GET ALL JOB
    public KeysetPageResponse<JobAllResponse> getAllJobs(
            int size,
            String keyword,
            Integer position,
            Integer salary,
            LocalDateTime cursorTime,
            UUID cursorId) {
        List<Job> jobs = jobRepository.findAllJobs(size + 1, keyword, position, salary, cursorTime, cursorId);
        boolean hasNext = jobs.size() == size + 1 ? true : false;
        List<Job> pageJobs = hasNext ? jobs.subList(0, size) : jobs;

        List<JobAllResponse> content = pageJobs.stream()
                .map(record -> {
                    JobAllResponse item = modelMapper.map(record, JobAllResponse.class);
                    item.setId(record.getId().toString());
                    item.setCompanyName(record.getUser().getCompany().getTitle());
                    item.setCategoryName(record.getCategory().getTitle());
                    return item;
                })
                .toList();

        // Cursor cho trang tiếp theo là createdAt và id của item cuối cùng
        String nextCursorTime = null;
        String nextCursorId = null;
        if (hasNext && !content.isEmpty()) {
            JobAllResponse last = content.get(content.size() - 1);
            nextCursorTime = last.getCreatedAt().toString();
            nextCursorId = last.getId();
        }

        return KeysetPageResponse.<JobAllResponse>builder()
                .content(content)
                .size(content.size())
                .hasNext(hasNext)
                .nextCursorTime(nextCursorTime)
                .nextCursorId(nextCursorId)
                .build();
    }

    @Transactional
    public void createJob(JobRequest jobRequest, String email) {
        Category category = categoryService.getCategoryById(UUID.fromString(jobRequest.getCategoryId()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("User not found"));
        Job job = modelMapper.map(jobRequest, Job.class);
        job.setId(null);
        job.setPosition(positionEnum[jobRequest.getPosition()]);
        job.setCategory(category);
        job.setUser(user);
        jobRepository.save(job);

        if (jobRequest.getTagsId() != null && jobRequest.getTagsId().size() > 0) {
            List<Tag> tags = tagService.getAllTagsByIds(
                    jobRequest.getTagsId()
                            .stream()
                            .map(record -> UUID.fromString(record))
                            .toList());

            List<SelectedTag> selectedTags = tags.stream()
                    .map(record -> new SelectedTag(job, record))
                    .toList();
            tagDetailService.saveSelectedTags(selectedTags);
        }
    }

    public void test() {
        // Specification<Job> spec = FilterJob.filterKeyset(salary, position, keyword,
        // cursorTime, cursorId);
        //
        // // Sort cố định: createdAt DESC, id DESC — bắt buộc để cursor hợp lệ
        // Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        jobRepository.findJob();
    }
}
