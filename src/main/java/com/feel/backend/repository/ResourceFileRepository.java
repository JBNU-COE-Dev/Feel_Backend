package com.feel.backend.repository;

import com.feel.backend.entity.ResourceFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceFileRepository extends JpaRepository<ResourceFile, Long> {
    
    Page<ResourceFile> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    
    List<ResourceFile> findByCategoryOrderByCreatedAtDesc(String category);
    
    long countByCategory(String category);

    // 연도/월별 조회
    List<ResourceFile> findByCategoryAndYearAndMonthOrderByCreatedAtDesc(
            String category, Integer year, Integer month);

    Page<ResourceFile> findByCategoryAndYearAndMonthOrderByCreatedAtDesc(
            String category, Integer year, Integer month, Pageable pageable);

    // 연도별 조회
    List<ResourceFile> findByCategoryAndYearOrderByMonthDescCreatedAtDesc(
            String category, Integer year);

    // 특정 카테고리의 사용 가능한 연도/월 목록 조회
    @Query("SELECT DISTINCT r.year FROM ResourceFile r WHERE r.category = :category AND r.year IS NOT NULL ORDER BY r.year DESC")
    List<Integer> findDistinctYearsByCategory(@Param("category") String category);

    @Query("SELECT DISTINCT r.month FROM ResourceFile r WHERE r.category = :category AND r.year = :year AND r.month IS NOT NULL ORDER BY r.month DESC")
    List<Integer> findDistinctMonthsByCategoryAndYear(@Param("category") String category, @Param("year") Integer year);
}
