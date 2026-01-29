package com.feel.backend.controller;

import com.feel.backend.dto.MatchingRequestDto;
import com.feel.backend.dto.MatchingResponseDto;
import com.feel.backend.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping
    public ResponseEntity<Page<MatchingResponseDto>> getMatchings(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category
    ) {
        Page<MatchingResponseDto> response = matchingService.getMatchings(type, page, limit, search, category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchingResponseDto> getMatchingDetail(@PathVariable Long id) {
        MatchingResponseDto response = matchingService.getMatchingDetail(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MatchingResponseDto> createMatching(@Valid @RequestBody MatchingRequestDto requestDto) {
        MatchingResponseDto response = matchingService.createMatching(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchingResponseDto> updateMatching(
        @PathVariable Long id,
        @Valid @RequestBody MatchingRequestDto requestDto
    ) {
        MatchingResponseDto response = matchingService.updateMatching(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatching(@PathVariable Long id) {
        matchingService.deleteMatching(id);
        return ResponseEntity.noContent().build();
    }
}
