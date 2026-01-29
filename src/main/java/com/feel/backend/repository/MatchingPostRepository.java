package com.feel.backend.repository;

import com.feel.backend.entity.MatchingPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchingPostRepository extends JpaRepository<MatchingPost, Long> {

    @Query("""
        SELECT m FROM MatchingPost m
        WHERE (:type IS NULL OR m.type = :type)
          AND (:category IS NULL OR m.category = :category)
          AND (:search IS NULL OR m.title LIKE %:search% OR m.description LIKE %:search%)
        ORDER BY m.createdAt DESC
        """)
    Page<MatchingPost> search(
        @Param("type") String type,
        @Param("category") String category,
        @Param("search") String search,
        Pageable pageable
    );

    List<MatchingPost> findByIdIn(List<Long> ids);
}
