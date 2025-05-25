package com.ringle.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringle.session.service.StudentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @Test
    void 가능한_시간대_조회_API() throws Exception {
        // given
        Map<String, List<Object>> mockResponse = new HashMap<>();
        mockResponse.put("availableSlots", java.util.Collections.emptyList());

        when(studentService.getAvailableTimes(any(LocalDate.class), any(LocalDate.class), anyInt()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/students/available-times")
                        .param("startDate", "2025-05-25")
                        .param("endDate", "2025-05-25")
                        .param("duration", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());

        verify(studentService).getAvailableTimes(LocalDate.of(2025, 5, 25), LocalDate.of(2025, 5, 25), 30);
    }

    @Test
    void 가능한_튜터_조회_API() throws Exception {
        // given
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("availableTutors", java.util.Collections.emptyList());

        when(studentService.getAvailableTutors(any(LocalDateTime.class), anyInt()))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/students/available-tutors")
                        .param("startTime", "2025-05-25T10:00:00")
                        .param("duration", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());

        verify(studentService).getAvailableTutors(LocalDateTime.of(2025, 5, 25, 10, 0, 0), 30);
    }
}
