package com.feel.backend.repository;

import com.feel.backend.entity.FinanceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceReportRepository extends JpaRepository<FinanceReport, Long> {

    // 연도별 조회
    Page<FinanceReport> findByYear(Integer year, Pageable pageable);

    // 키워드 검색 (제목 또는 설명)
    @Query("SELECT f FROM FinanceReport f WHERE " +
           "LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FinanceReport> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 연도 + 키워드 검색
    @Query("SELECT f FROM FinanceReport f WHERE f.year = :year AND " +
           "(LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FinanceReport> searchByYearAndKeyword(
            @Param("year") Integer year,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
