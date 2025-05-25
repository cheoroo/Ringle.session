package com.ringle.session.controller;

import com.ringle.session.dto.response.ApiResponse;
import com.ringle.session.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/available-times")
    public ApiResponse<Map<String, List<Object>>> getAvailableTimes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam int duration) {

        Map<String, List<Object>> result = studentService.getAvailableTimes(startDate, endDate, duration);
        return ApiResponse.success(result);
    }

    @GetMapping("/available-tutors")
    public ApiResponse<Map<String, Object>> getAvailableTutors(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam int duration) {

        Map<String, Object> result = studentService.getAvailableTutors(startTime, duration);
        return ApiResponse.success(result);
    }
}
