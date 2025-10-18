package com.feel.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feel.backend.dto.NoticeRequestDto;
import com.feel.backend.service.NoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeController.class)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoticeService noticeService;

    @Test
    @DisplayName("유효한 카테고리로 공지사항 생성 - 학과소식")
    void createNotice_WithValidCategory_Department() throws Exception {
        NoticeRequestDto request = NoticeRequestDto.builder()
                .title("테스트 공지")
                .content("테스트 내용")
                .author("관리자")
                .category("학과소식")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효한 카테고리로 공지사항 생성 - 일반공지")
    void createNotice_WithValidCategory_General() throws Exception {
        NoticeRequestDto request = NoticeRequestDto.builder()
                .title("일반 공지")
                .content("일반 내용")
                .author("관리자")
                .category("일반공지")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효한 카테고리로 공지사항 생성 - 학사공지")
    void createNotice_WithValidCategory_Academic() throws Exception {
        NoticeRequestDto request = NoticeRequestDto.builder()
                .title("학사 공지")
                .content("학사 내용")
                .author("관리자")
                .category("학사공지")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효한 카테고리로 공지사항 생성 - 사업단공지")
    void createNotice_WithValidCategory_Project() throws Exception {
        NoticeRequestDto request = NoticeRequestDto.builder()
                .title("사업단 공지")
                .content("사업단 내용")
                .author("관리자")
                .category("사업단공지")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효한 카테고리로 공지사항 생성 - 취업정보")
    void createNotice_WithValidCategory_Employment() throws Exception {
        NoticeRequestDto request = NoticeRequestDto.builder()
                .title("취업 공지")
                .content("취업 내용")
                .author("관리자")
                .category("취업정보")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효하지 않은 카테고리로 공지사항 생성 실패")
    void createNotice_WithInvalidCategory_ShouldFail() throws Exception {
        NoticeRequestDto request = NoticeRequestDto.builder()
                .title("테스트 공지")
                .content("테스트 내용")
                .author("관리자")
                .category("잘못된카테고리")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여러 개의 유효하지 않은 카테고리로 공지사항 생성 실패 테스트")
    void createNotice_WithMultipleInvalidCategories_ShouldFail() throws Exception {
        String[] invalidCategories = {"행사공지", "긴급공지", "INVALID", "공지사항", "학과공지"};

        for (String category : invalidCategories) {
            NoticeRequestDto request = NoticeRequestDto.builder()
                    .title("테스트 공지")
                    .content("테스트 내용")
                    .author("관리자")
                    .category(category)
                    .isPinned(false)
                    .build();

            mockMvc.perform(post("/api/notices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("카테고리 없이 공지사항 생성 - 성공 (카테고리는 선택사항)")
    void createNotice_WithoutCategory_ShouldSucceed() throws Exception {
        NoticeRequestDto request = NoticeRequestDto.builder()
                .title("테스트 공지")
                .content("테스트 내용")
                .author("관리자")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("여러 개의 JSON 객체를 연속으로 전송 - 모두 유효한 카테고리")
    void createMultipleNotices_AllValidCategories() throws Exception {
        NoticeRequestDto[] requests = {
                NoticeRequestDto.builder()
                        .title("공지 1")
                        .content("내용 1")
                        .author("관리자1")
                        .category("학과소식")
                        .isPinned(false)
                        .build(),
                NoticeRequestDto.builder()
                        .title("공지 2")
                        .content("내용 2")
                        .author("관리자2")
                        .category("일반공지")
                        .isPinned(false)
                        .build(),
                NoticeRequestDto.builder()
                        .title("공지 3")
                        .content("내용 3")
                        .author("관리자3")
                        .category("학사공지")
                        .isPinned(true)
                        .build()
        };

        // 각 요청을 순차적으로 전송
        for (NoticeRequestDto request : requests) {
            mockMvc.perform(post("/api/notices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("여러 개의 JSON 객체 전송 - 일부 유효하지 않은 카테고리")
    void createMultipleNotices_SomeInvalidCategories() throws Exception {
        // 첫 번째 요청 - 유효한 카테고리
        NoticeRequestDto validRequest = NoticeRequestDto.builder()
                .title("유효한 공지")
                .content("내용")
                .author("관리자")
                .category("학과소식")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // 두 번째 요청 - 유효하지 않은 카테고리
        NoticeRequestDto invalidRequest = NoticeRequestDto.builder()
                .title("유효하지 않은 공지")
                .content("내용")
                .author("관리자")
                .category("잘못된카테고리")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // 세 번째 요청 - 다시 유효한 카테고리
        NoticeRequestDto anotherValidRequest = NoticeRequestDto.builder()
                .title("다시 유효한 공지")
                .content("내용")
                .author("관리자")
                .category("취업정보")
                .isPinned(false)
                .build();

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherValidRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
