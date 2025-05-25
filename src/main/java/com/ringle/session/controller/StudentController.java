package com.ringle.session.controller;

import com.ringle.session.dto.request.BookingCreateRequest;
import com.ringle.session.dto.response.ApiResponse;
import com.ringle.session.dto.response.BookingResponse;
import com.ringle.session.entity.SessionBooking;
import com.ringle.session.service.BookingService;
import com.ringle.session.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final BookingService bookingService;

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

    @PostMapping("/{studentId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponse> createBooking(
            @PathVariable Long studentId,
            @Valid @RequestBody BookingCreateRequest request) {

        SessionBooking booking = bookingService.createBooking(
                studentId,
                request.getTutorId(),
                request.getStartTime(),
                request.getDurationMinutes()
        );

        BookingResponse response = new BookingResponse(booking);
        return ApiResponse.success(response, "예약이 성공적으로 생성되었습니다.");
    }

    @GetMapping("/{studentId}/bookings/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(
            @PathVariable Long studentId,
            @PathVariable Long bookingId) {

        SessionBooking booking = bookingService.getBooking(studentId, bookingId);
        BookingResponse response = new BookingResponse(booking);
        return ApiResponse.success(response);
    }

    @GetMapping("/{studentId}/bookings")
    public ApiResponse<List<BookingResponse>> getStudentBookings(@PathVariable Long studentId) {
        List<SessionBooking> bookings = bookingService.getStudentBookings(studentId);
        List<BookingResponse> responses = bookings.stream()
                .map(BookingResponse::new)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }
}
