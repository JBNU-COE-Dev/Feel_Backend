package com.feel.backend.controller;

import com.feel.backend.dto.FinanceReportDto;
import com.feel.backend.service.FinanceReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/finance/reports")
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReportService financeReportService;

    /**
     * 회계 보고서 목록 조회
     * GET /api/finance/reports?page=0&size=10&keyword=xxx&sortBy=createdAt&sortOrder=desc&year=2026
     */
    @GetMapping
    public ResponseEntity<Page<FinanceReportDto.Response>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer year
    ) {
        Page<FinanceReportDto.Response> reports = financeReportService.getReports(
                page, size, keyword, sortBy, sortOrder, year
        );
        return ResponseEntity.ok(reports);
    }

    /**
     * 회계 보고서 상세 조회
     * GET /api/finance/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FinanceReportDto.Response> getReportById(@PathVariable Long id) {
        FinanceReportDto.Response report = financeReportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * 회계 보고서 등록
     * POST /api/finance/reports
     */
    @PostMapping
    public ResponseEntity<FinanceReportDto.Response> createReport(
            @Valid @RequestBody FinanceReportDto.Request request
    ) {
        FinanceReportDto.Response response = financeReportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 회계 보고서 수정
     * PUT /api/finance/reports/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<FinanceReportDto.Response> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody FinanceReportDto.Request request
    ) {
        FinanceReportDto.Response response = financeReportService.updateReport(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 회계 보고서 삭제
     * DELETE /api/finance/reports/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        financeReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PDF 파일 업로드
     * POST /api/finance/reports/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<FinanceReportDto.UploadResponse> uploadPdf(
            @RequestParam MultipartFile file
    ) {
        FinanceReportDto.UploadResponse response = financeReportService.uploadPdf(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PDF 파일 다운로드
     * GET /api/finance/reports/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        FinanceReportDto.Response report = financeReportService.getReportById(id);

        try {
            // fileUrl에서 파일 경로 추출
            String fileUrl = report.getFileUrl();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get("uploads", "finance", fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + report.getFileName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("파일을 찾을 수 없습니다.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 잘못되었습니다.", e);
        }
    }
}
