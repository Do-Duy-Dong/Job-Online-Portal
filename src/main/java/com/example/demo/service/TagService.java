package com.example.demo.service;

import com.example.demo.entity.Tag;
import com.example.demo.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TagService {
    private final TagRepository tagRepository;
    public TagService(TagRepository tagRepository){
        this.tagRepository= tagRepository;
    }
    public Tag getTagById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Tag not found"));
    }
    public List<Tag> getAllTags(){
        return tagRepository.findAll();
    }
    public void createTag(String title) {
        Tag tag = new Tag();
        tag.setTitle(title);
        tagRepository.save(tag);
    }

    public List<Tag> getAllTagsByIds(List<UUID> ids){
        List<Tag> tags= tagRepository.findAllById(ids);
        if(tags.size() != ids.size()){
            throw new RuntimeException("Some tags not found");
        }
        return tags;
    }
}
