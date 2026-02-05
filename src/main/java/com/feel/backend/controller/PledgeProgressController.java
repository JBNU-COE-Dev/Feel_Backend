package com.feel.backend.controller;

import com.feel.backend.dto.PledgeProgressDto;
import com.feel.backend.service.PledgeProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 공약 이행률 progress API.
 * - GET: 전체 ID별 completed (공개, JBNU 사이트)
 * - PATCH: 단건 또는 일괄 수정 (관리자)
 */
@RestController
@RequestMapping("/api/pledges")
@RequiredArgsConstructor
public class PledgeProgressController {

    private final PledgeProgressService pledgeProgressService;

    /**
     * 전체 공약 이행 여부 조회 (ID -> completed)
     * GET /api/pledges/progress
     */
    @GetMapping("/progress")
    public ResponseEntity<PledgeProgressDto.ProgressResponse> getProgress() {
        return ResponseEntity.ok(pledgeProgressService.getProgress());
    }

    /**
     * 단건 이행 여부 수정 (관리자)
     * PATCH /api/pledges/progress/{id}
     */
    @PatchMapping("/progress/{id}")
    public ResponseEntity<PledgeProgressDto.ProgressResponse> updateProgress(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        Boolean completed = body != null ? body.get("completed") : null;
        return ResponseEntity.ok(pledgeProgressService.updateProgress(id, completed));
    }

    /**
     * 일괄 이행 여부 수정 (관리자)
     * PATCH /api/pledges/progress
     */
    @PatchMapping("/progress")
    public ResponseEntity<PledgeProgressDto.ProgressResponse> updateProgressBatch(
            @RequestBody PledgeProgressDto.BatchUpdateRequest body) {
        return ResponseEntity.ok(pledgeProgressService.updateProgressBatch(body.getProgress()));
    }
}
