package com.example.demo.service;

import com.example.demo.entity.SelectedTag;
import com.example.demo.repository.SelectedTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TagDetailService {
    private final SelectedTagRepository selectedTagRepository;
    private final TagService tagService;
    public TagDetailService(
            SelectedTagRepository selectedTagRepository,
            TagService tagService){
        this.selectedTagRepository= selectedTagRepository;
        this.tagService= tagService;
    }

    public void saveSelectedTags(List<SelectedTag> requestList){
        selectedTagRepository.saveAll(requestList);
    }

    public List<SelectedTag> getTagsByJobId(UUID jobId){
        List<SelectedTag> tags= selectedTagRepository.findAllByJob_Id(jobId);
        return tags;
    }
}
