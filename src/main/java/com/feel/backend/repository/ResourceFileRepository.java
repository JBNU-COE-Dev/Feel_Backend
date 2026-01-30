package com.feel.backend.repository;

import com.feel.backend.entity.ResourceFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceFileRepository extends JpaRepository<ResourceFile, Long> {
    
    Page<ResourceFile> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    
    List<ResourceFile> findByCategoryOrderByCreatedAtDesc(String category);
    
    long countByCategory(String category);
}
