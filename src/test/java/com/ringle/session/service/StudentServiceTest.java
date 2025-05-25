package com.ringle.session.service;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.Student;
import com.ringle.session.entity.Tutor;
import com.ringle.session.repository.SessionSlotRepository;
import com.ringle.session.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SessionSlotRepository sessionSlotRepository;

    @Mock
    private TimeValidationService timeValidationService;

    @InjectMocks
    private StudentService studentService;

    @Test
    void 가능한_시간대_조회_30분_수업() {
        // given
        LocalDate startDate = LocalDate.of(2025, 5, 25);
        LocalDate endDate = LocalDate.of(2025, 5, 25);
        int duration = 30;

        Tutor tutor1 = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor1, "id", 1L);

        Tutor tutor2 = new Tutor("튜터2", "튜터설명2");
        ReflectionTestUtils.setField(tutor2, "id", 2L);

        // 같은 시간대에 다른 튜터들
        SessionSlot slot1 = new SessionSlot(tutor1,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30));
        SessionSlot slot2 = new SessionSlot(tutor1,
                LocalDateTime.of(2025, 5, 25, 11, 0),
                LocalDateTime.of(2025, 5, 25, 11, 30));
        SessionSlot slot3 = new SessionSlot(tutor2,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30));

        List<SessionSlot> availableSlots = Arrays.asList(slot1, slot2, slot3);

        when(sessionSlotRepository.findAvailableSlotsBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(availableSlots);

        // when
        Map<String, List<Object>> result = studentService.getAvailableTimes(startDate, endDate, duration);

        // then
        assertThat(result).containsKey("availableSlots");
        List<Object> slots = result.get("availableSlots");

        assertThat(slots).hasSize(2);
    }

    @Test
    void 가능한_시간대_조회_60분_수업() {
        // given
        LocalDate startDate = LocalDate.of(2025, 5, 25);
        LocalDate endDate = LocalDate.of(2025, 5, 25);
        int duration = 60;

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", 1L);

        // 연속된 두 슬롯 (60분 수업 가능)
        SessionSlot slot1 = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30));
        ReflectionTestUtils.setField(slot1, "id", 1L);

        SessionSlot slot2 = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 10, 30),
                LocalDateTime.of(2025, 5, 25, 11, 0));
        ReflectionTestUtils.setField(slot2, "id", 2L);

        // 연속되지 않은 슬롯
        SessionSlot slot3 = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 14, 0),
                LocalDateTime.of(2025, 5, 25, 14, 30));
        ReflectionTestUtils.setField(slot3, "id", 3L);

        List<SessionSlot> availableSlots = Arrays.asList(slot1, slot2, slot3);

        when(sessionSlotRepository.findAvailableSlotsBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(availableSlots);

        // when
        Map<String, List<Object>> result = studentService.getAvailableTimes(startDate, endDate, duration);

        // then
        assertThat(result).containsKey("availableSlots");
        List<Object> slots = result.get("availableSlots");

        assertThat(slots).hasSize(1);
    }

    @Test
    void 특정_시간대_가능한_튜터_조회() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        int duration = 30;

        Tutor tutor1 = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor1, "id", 1L);

        Tutor tutor2 = new Tutor("튜터2", "튜터설명2");
        ReflectionTestUtils.setField(tutor2, "id", 2L);

        SessionSlot slot1 = new SessionSlot(tutor1, startTime, startTime.plusMinutes(30));
        SessionSlot slot2 = new SessionSlot(tutor2, startTime, startTime.plusMinutes(30));

        List<SessionSlot> availableSlots = Arrays.asList(slot1, slot2);

        when(sessionSlotRepository.findAvailableSlotsBetween(eq(startTime), eq(startTime.plusMinutes(duration))))
                .thenReturn(availableSlots);

        // when
        Map<String, Object> result = studentService.getAvailableTutors(startTime, duration);

        // then
        assertThat(result).containsKey("requestedTime");
        assertThat(result).containsKey("availableTutors");

        @SuppressWarnings("unchecked")
        List<Object> tutors = (List<Object>) result.get("availableTutors");
        assertThat(tutors).hasSize(2);

        verify(sessionSlotRepository).findAvailableSlotsBetween(startTime, startTime.plusMinutes(duration));
    }
}