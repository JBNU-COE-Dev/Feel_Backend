package com.feel.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum NoticeCategory {
    DEPARTMENT("학과소식"),
    GENERAL("일반공지"),
    ACADEMIC("학사공지"),
    PROJECT("사업단공지"),
    EMPLOYMENT("취업정보");

    private final String displayName;

    NoticeCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static NoticeCategory fromDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (NoticeCategory category : NoticeCategory.values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }

        throw new IllegalArgumentException(
            String.format("유효하지 않은 카테고리입니다: '%s'. 허용된 카테고리: %s",
                displayName,
                getAllowedCategories())
        );
    }

    public static Set<String> getAllowedCategories() {
        return Arrays.stream(NoticeCategory.values())
                .map(NoticeCategory::getDisplayName)
                .collect(Collectors.toSet());
    }

    public static boolean isValid(String displayName) {
        if (displayName == null) {
            return false;
        }
        return Arrays.stream(NoticeCategory.values())
                .anyMatch(category -> category.displayName.equals(displayName));
    }
}
