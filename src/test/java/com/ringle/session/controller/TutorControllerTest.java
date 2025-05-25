package com.ringle.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringle.session.dto.request.TimeSlotCreateRequest;
import com.ringle.session.dto.response.TimeSlotResponse;
import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.SlotStatus;
import com.ringle.session.entity.Tutor;
import com.ringle.session.service.TutorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TutorController.class)
class TutorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TutorService tutorService;

    @Test
    void 시간대_생성_API_성공() throws Exception {
        // given
        Long tutorId = 1L;
        TimeSlotCreateRequest request = new TimeSlotCreateRequest(
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 11, 0)
        );

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", tutorId);

        SessionSlot slot = new SessionSlot(tutor, request.getStartTime(), request.getEndTime());
        ReflectionTestUtils.setField(slot, "id", 1L);

        when(tutorService.createTimeSlot(eq(tutorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(slot);

        // when & then
        mockMvc.perform(post("/api/tutors/{tutorId}/time-slots", tutorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tutorId").value(tutorId))
                .andExpect(jsonPath("$.data.startTime").value("2025-05-25T10:00:00"))
                .andExpect(jsonPath("$.data.endTime").value("2025-05-25T11:00:00"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        verify(tutorService).createTimeSlot(tutorId, request.getStartTime(), request.getEndTime());
    }

    @Test
    void 시간대_삭제_API_성공() throws Exception {
        // given
        Long tutorId = 1L;
        Long slotId = 1L;

        doNothing().when(tutorService).deleteTimeSlot(tutorId, slotId);

        // when & then
        mockMvc.perform(delete("/api/tutors/{tutorId}/time-slots/{slotId}", tutorId, slotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("시간대가 성공적으로 삭제되었습니다."));

        verify(tutorService).deleteTimeSlot(tutorId, slotId);
    }

    @Test
    void 잘못된_요청_데이터_검증() throws Exception {
        // given
        Long tutorId = 1L;
        TimeSlotCreateRequest invalidRequest = new TimeSlotCreateRequest(null, null);

        // when & then
        mockMvc.perform(post("/api/tutors/{tutorId}/time-slots", tutorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
