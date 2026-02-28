package com.example.demo.repository;

import com.example.demo.entity.SelectedTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SelectedTagRepository extends JpaRepository<SelectedTag, UUID> {
    @EntityGraph(attributePaths = {"tag"})
    List<SelectedTag> findAllByJob_Id(UUID id);
}
