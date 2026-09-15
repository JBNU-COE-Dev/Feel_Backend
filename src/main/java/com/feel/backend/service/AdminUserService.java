package com.feel.backend.service;

import com.feel.backend.dto.ActivityPostResponseDto;
import com.feel.backend.dto.AdminUserResponseDto;
import com.feel.backend.entity.User;
import com.feel.backend.repository.ActivityPostRepository;
import com.feel.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final ActivityPostRepository activityPostRepository;
    private final ActivityPostService activityPostService;

    public Page<AdminUserResponseDto> getUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        return userRepository.search(normalized, pageable)
                .map(user -> AdminUserResponseDto.fromEntity(user, activityPostRepository.countByAuthorId(user.getId())));
    }

    public AdminUserResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        long postCount = activityPostRepository.countByAuthorId(userId);
        return AdminUserResponseDto.fromEntity(user, postCount);
    }

    public Page<ActivityPostResponseDto> getUserPosts(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("회원을 찾을 수 없습니다.");
        }
        return activityPostService.getByAuthorId(userId, page, size);
    }
}
