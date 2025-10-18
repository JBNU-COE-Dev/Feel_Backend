package com.feel.backend.repository;

import com.feel.backend.entity.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {

    // 카테고리별 조회
    Page<Gallery> findByCategory(String category, Pageable pageable);

    // 제목으로 검색
    Page<Gallery> findByTitleContaining(String keyword, Pageable pageable);

    // 카테고리와 키워드로 검색
    Page<Gallery> findByCategoryAndTitleContaining(String category, String keyword, Pageable pageable);

    // 최근 등록 순으로 조회
    List<Gallery> findTop10ByOrderByCreatedAtDesc();
}
