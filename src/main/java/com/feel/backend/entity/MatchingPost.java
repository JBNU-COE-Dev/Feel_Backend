package com.feel.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "matching_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer maxMembers;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentMembers = 0;

    @Column
    private LocalDate deadline;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column
    private Long organizerId;

    @Column(length = 100)
    private String organizerName;

    @Column(length = 100)
    private String companyType;

    @Column(length = 100)
    private String targetAudience;

    @Column
    private LocalDate applicationStart;

    @Column
    private LocalDate applicationEnd;

    @Column(length = 100)
    private String activityPeriod;

    @Column(length = 100)
    private String recruitCount;

    @Column(length = 100)
    private String activityArea;

    @Column(length = 500)
    private String homepage;

    @Column(length = 500)
    private String tagsActivity;

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer bookmarkCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decrementBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }

    public void incrementCurrentMembers() {
        if (this.currentMembers == null) {
            this.currentMembers = 0;
        }
        this.currentMembers++;
    }
}
