package com.feel.backend.repository;

import com.feel.backend.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 고정된 공지사항 조회
    List<Notice> findByIsPinnedTrueOrderByCreatedAtDesc();

    // 카테고리별 조회
    Page<Notice> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    // 제목 또는 내용으로 검색
    Page<Notice> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
        String titleKeyword, String contentKeyword, Pageable pageable);

    // 카테고리 + 제목 또는 내용으로 검색
    Page<Notice> findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByCreatedAtDesc(
        String category1, String titleKeyword, String category2, String contentKeyword, Pageable pageable);

    // 일반 공지사항 (고정 제외) 조회
    Page<Notice> findByIsPinnedFalseOrderByCreatedAtDesc(Pageable pageable);
}
