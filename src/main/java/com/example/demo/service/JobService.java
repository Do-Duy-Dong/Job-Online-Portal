package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.JobAllResponse;
import com.example.demo.payload.Request.JobRequest;
import com.example.demo.payload.Response.JobResponse;
import com.example.demo.payload.Response.KeysetPageResponse;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.repository.JobRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class JobService {
    private static final String[] positionEnum = {
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
    private final EmployerRepository employerRepository;

    public JobService(
            JobRepository jobRepository,
            CategoryService categoryService,
            ModelMapper modelMapper,
            UserRepository userRepository,
            EmployerRepository employerRepository,
            TagService tagService,
            TagDetailService tagDetailService) {
        this.jobRepository = jobRepository;
        this.categoryService = categoryService;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.tagService = tagService;
        this.employerRepository = employerRepository;
        this.tagDetailService = tagDetailService;
    }

    public Job findById(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Job not found"));
    }

    // GET /api/job/getJob/{id}
    public Job getJobEntityById(UUID id) {
        Job job = jobRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFound("Job not found"));
        if (job.getStatus().equals("CLOSED")) {
            throw new ResourceNotFound("Job is closed");
        }
        return job;
    }

    // GET /api/job/getJob/{id}
    // GETBYID
    public JobResponse getJobById(UUID id) {
        Job job = getJobEntityById(id);
        Employer employer = job.getEmployer();
        JobResponse jobResponse = modelMapper.map(job, JobResponse.class);
        List<SelectedTag> selectedTags = tagDetailService.getTagsByJobId(id);

        // Set each field
        jobResponse.setCategoryName(job.getCategory().getTitle());
        jobResponse.setCompanyName(employer.getCompany().getTitle());
        jobResponse.setCompanyId(employer.getCompany().getId().toString());
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

    // GET /api/job/getAll
    // GET ALL JOB (keyset pagination)
    public KeysetPageResponse<JobAllResponse> getAllJobs(
            int size,
            String keyword,
            Integer position,
            Integer location,
            Integer salary,
            LocalDateTime cursorTime,
            UUID cursorId) {

        List<Job> jobs = jobRepository.findAllJobs(size + 1, keyword, position, salary, location, cursorTime, cursorId);
        boolean hasNext = jobs.size() == size + 1;
        List<Job> pageJobs = hasNext ? jobs.subList(0, size) : jobs;

        List<JobAllResponse> content = pageJobs.stream()
                .map(record -> {
                    JobAllResponse item = modelMapper.map(record, JobAllResponse.class);
                    item.setId(record.getId().toString());
                    item.setCompanyName(record.getEmployer().getCompany().getTitle());
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

        long totalCount = getJobTotalCount(keyword, position, salary, location);

        return KeysetPageResponse.<JobAllResponse>builder()
                .content(content)
                .size(content.size())
                .hasNext(hasNext)
                .nextCursorTime(nextCursorTime)
                .nextCursorId(nextCursorId)
                .totalCount(totalCount)
                .build();
    }

    // GET /api/job/getAll
    @Cacheable(value = "job:count", key = "T(com.example.demo.service.JobService).buildCountCacheKey(#keyword, #position, #salary)")
    public long getJobTotalCount(String keyword, Integer position, Integer salary, Integer location) {
        log.debug("Cache totalCount for key: {}:{}:{}",
                keyword, position, salary);
        return jobRepository.countAllJobs(keyword, position, location, salary);
    }

    // POST /api/employer/job/create
    @CacheEvict(value = "job:count", allEntries = true)
    @Transactional
    public void createJob(JobRequest jobRequest, String email) {
        Category category = categoryService.getCategoryById(UUID.fromString(jobRequest.getCategoryId()));
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("Employer not found"));
        Job job = modelMapper.map(jobRequest, Job.class);
        job.setId(null);
        job.setPosition(positionEnum[jobRequest.getPosition()]);
        job.setCategory(category);
        job.setEmployer(employer);
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

        log.info("Job created — cache 'job:count' evicted");
    }

    // PUT /api/employer/job/update/{id}
    @CacheEvict(value = "job:count", allEntries = true)
    @Transactional
    public void updateJob(UUID id, JobRequest jobRequest, String email) {
        Job job = findById(id);

        // Ownership check: employer must own this job (Employer extends User so
        // .getEmail() works directly)
        if (!job.getEmployer().getEmail().equals(email)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to update this job");
        }

        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("Employer not found"));
        Category category = categoryService.getCategoryById(UUID.fromString(jobRequest.getCategoryId()));
        modelMapper.map(jobRequest, job);
        job.setId(id); // prevent ID override
        job.setPosition(positionEnum[jobRequest.getPosition()]);
        job.setCategory(category);
        job.setEmployer(employer);
        jobRepository.save(job);
        log.info("Job updated: {}", id);
    }

    // DELETE /api/employer/job/delete/{id}
    @CacheEvict(value = "job:count", allEntries = true)
    @Transactional
    public void deleteJob(UUID id, String email) {
        Job job = findById(id);

        // Ownership check: employer must own this job
        if (!job.getEmployer().getEmail().equals(email)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to delete this job");
        }
        // Soft delete: close the job instead of physically deleting
        job.setStatus("CLOSED");
        jobRepository.save(job);
        log.info("Job closed (soft-deleted): {}", id);
    }

    // GET /api/company/{companyId}/jobs
    // Only Employer from the same company can access (companyId in JWT must match)
    public KeysetPageResponse<JobAllResponse> getJobsByCompany(
            UUID companyId,
            String requesterCompanyId,
            int size,
            LocalDateTime cursorTime,
            UUID cursorId) {

        // Validate: requester's companyId in JWT must match the requested companyId
        if (requesterCompanyId == null || !requesterCompanyId.equals(companyId.toString())) {
            throw new AccessDeniedException(
                    "You can only view jobs of your own company");
        }

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Job> jobs = jobRepository.findAllByCompanyId(companyId, cursorTime, cursorId, pageable);

        boolean hasNext = jobs.size() == size + 1;
        List<Job> pageJobs = hasNext ? jobs.subList(0, size) : jobs;

        List<JobAllResponse> content = pageJobs.stream()
                .map(record -> {
                    JobAllResponse item = modelMapper.map(record, JobAllResponse.class);
                    item.setId(record.getId().toString());
                    item.setCompanyName(record.getEmployer().getCompany().getTitle());
                    item.setCategoryName(record.getCategory().getTitle());
                    return item;
                })
                .toList();

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

    public static String buildCountCacheKey(String keyword, Integer position, Integer salary) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim().toLowerCase() : "_";
        String pos = (position != null) ? position.toString() : "_";
        String sal = (salary != null) ? salary.toString() : "_";
        return kw + ":" + pos + ":" + sal;
    }

    public void test() {
        jobRepository.findJob();
    }
}
