package com.ringle.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringle.session.dto.request.BookingCreateRequest;
import com.ringle.session.entity.SessionBooking;
import com.ringle.session.entity.Student;
import com.ringle.session.entity.Tutor;
import com.ringle.session.service.BookingService;
import com.ringle.session.service.StudentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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

    @MockBean
    private BookingService bookingService;

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

    @Test
    void 수업_예약_생성_API() throws Exception {
        // given
        Long studentId = 1L;
        Long tutorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);

        BookingCreateRequest request = new BookingCreateRequest(tutorId, startTime, 30);

        Student student = new Student("학생1", "student@test.com");
        ReflectionTestUtils.setField(student, "id", studentId);

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", tutorId);

        SessionBooking booking = new SessionBooking(student, tutor, startTime, startTime.plusMinutes(30), 30);
        ReflectionTestUtils.setField(booking, "id", 1L);

        when(bookingService.createBooking(eq(studentId), eq(tutorId), eq(startTime), eq(30)))
                .thenReturn(booking);

        // when & then
        mockMvc.perform(post("/api/students/{studentId}/bookings", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.tutorId").value(tutorId));

        verify(bookingService).createBooking(studentId, tutorId, startTime, 30);
    }

    @Test
    void 학생_예약_목록_조회_API() throws Exception {
        // given
        Long studentId = 1L;

        Student student = new Student("학생1", "student@test.com");
        ReflectionTestUtils.setField(student, "id", studentId);

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", 1L);

        SessionBooking booking = new SessionBooking(student, tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30), 30);
        ReflectionTestUtils.setField(booking, "id", 1L);

        when(bookingService.getStudentBookings(studentId))
                .thenReturn(Arrays.asList(booking));

        // when & then
        mockMvc.perform(get("/api/students/{studentId}/bookings", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].studentId").value(studentId));

        verify(bookingService).getStudentBookings(studentId);
    }

    @Test
    void 특정_예약_조회_API() throws Exception {
        // given
        Long studentId = 1L;
        Long bookingId = 1L;

        Student student = new Student("학생1", "student@test.com");
        ReflectionTestUtils.setField(student, "id", studentId);

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", 1L);

        SessionBooking booking = new SessionBooking(student, tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30), 30);
        ReflectionTestUtils.setField(booking, "id", 1L);

        when(bookingService.getBooking(studentId, bookingId))
                .thenReturn(booking);

        // when & then
        mockMvc.perform(get("/api/students/{studentId}/bookings/{bookingId}", studentId, bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.tutorId").value(tutor.getId()));

        verify(bookingService).getBooking(studentId, bookingId);
    }
}
