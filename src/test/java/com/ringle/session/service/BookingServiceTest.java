package com.ringle.session.service;

import com.ringle.session.entity.*;
import com.ringle.session.exception.ValidationException;
import com.ringle.session.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private SessionSlotRepository sessionSlotRepository;

    @Mock
    private SessionBookingRepository sessionBookingRepository;

    @Mock
    private SlotBookingMappingRepository slotBookingMappingRepository;

    @Mock
    private TimeValidationService timeValidationService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void 수업_예약_성공_30분() {
        // given
        Long studentId = 1L;
        Long tutorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        int duration = 30;

        Student student = new Student("학생1", "student@test.com");
        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        SessionSlot slot = new SessionSlot(tutor, startTime, startTime.plusMinutes(30));
        SessionBooking expectedBooking = new SessionBooking(student, tutor, startTime, startTime.plusMinutes(30), 30);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(sessionSlotRepository.findAvailableSlotsByTutorAndTimeRange(
                eq(tutorId), eq(startTime), eq(startTime.plusMinutes(duration))))
                .thenReturn(Arrays.asList(slot));
        when(timeValidationService.canAccommodateBooking(eq(startTime), eq(startTime.plusMinutes(duration)), eq(duration), anyList()))
                .thenReturn(true);
        when(timeValidationService.findRequiredSlots(eq(startTime), eq(startTime.plusMinutes(duration)), anyList()))
                .thenReturn(Arrays.asList(slot));
        when(sessionBookingRepository.save(any(SessionBooking.class)))
                .thenReturn(expectedBooking);

        // when
        SessionBooking result = bookingService.createBooking(studentId, tutorId, startTime, duration);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getTutor()).isEqualTo(tutor);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getDurationMinutes()).isEqualTo(duration);

        verify(timeValidationService).canAccommodateBooking(eq(startTime), eq(startTime.plusMinutes(duration)), eq(duration), anyList());
        verify(timeValidationService).findRequiredSlots(eq(startTime), eq(startTime.plusMinutes(duration)), anyList());
        verify(sessionSlotRepository).save(slot);
        verify(slotBookingMappingRepository).save(any(SlotBookingMapping.class));
        verify(sessionBookingRepository).save(any(SessionBooking.class));
    }

    @Test
    void 수업_예약_성공_60분() {
        // given
        Long studentId = 1L;
        Long tutorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        int duration = 60;

        Student student = new Student("학생1", "student@test.com");
        Tutor tutor = new Tutor("튜터1", "튜터설명1");

        SessionSlot slot1 = new SessionSlot(tutor, startTime, startTime.plusMinutes(30));
        SessionSlot slot2 = new SessionSlot(tutor, startTime.plusMinutes(30), startTime.plusMinutes(60));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(sessionSlotRepository.findAvailableSlotsByTutorAndTimeRange(
                eq(tutorId), eq(startTime), eq(startTime.plusMinutes(duration))))
                .thenReturn(Arrays.asList(slot1, slot2));
        when(timeValidationService.canAccommodateBooking(any(), any(), eq(duration), anyList()))
                .thenReturn(true);
        when(timeValidationService.findRequiredSlots(any(), any(), anyList()))
                .thenReturn(Arrays.asList(slot1, slot2));
        when(sessionBookingRepository.save(any(SessionBooking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SessionBooking result = bookingService.createBooking(studentId, tutorId, startTime, duration);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDurationMinutes()).isEqualTo(60);

        verify(sessionSlotRepository, times(2)).save(any(SessionSlot.class)); // 두 슬롯 상태 변경
        verify(slotBookingMappingRepository, times(2)).save(any(SlotBookingMapping.class));
    }

    @Test
    void 존재하지_않는_학생_예약_실패() {
        // given
        Long studentId = 999L;
        Long tutorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        int duration = 30;

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(studentId, tutorId, startTime, duration))
                .isInstanceOf(ValidationException.class)
                .hasMessage("학생을 찾을 수 없습니다.");
    }

    @Test
    void 충분한_슬롯이_없는_경우_예약_실패() {
        // given
        Long studentId = 1L;
        Long tutorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        int duration = 60;

        Student student = new Student("학생1", "student@test.com");
        Tutor tutor = new Tutor("튜터1", "튜터설명1");

        SessionSlot slot = new SessionSlot(tutor, startTime, startTime.plusMinutes(30));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(sessionSlotRepository.findAvailableSlotsByTutorAndTimeRange(
                eq(tutorId), eq(startTime), eq(startTime.plusMinutes(duration))))
                .thenReturn(Arrays.asList(slot));
        when(timeValidationService.canAccommodateBooking(any(), any(), eq(duration), anyList()))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> bookingService.createBooking(studentId, tutorId, startTime, duration))
                .isInstanceOf(ValidationException.class)
                .hasMessage("요청한 시간대에 예약 가능한 슬롯이 충분하지 않습니다.");
    }

    @Test
    void 학생_예약_목록_조회() {
        // given
        Long studentId = 1L;
        Student student = new Student("학생1", "student@test.com");
        Tutor tutor = new Tutor("튜터1", "튜터설명1");

        SessionBooking booking1 = new SessionBooking(student, tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30), 30);
        SessionBooking booking2 = new SessionBooking(student, tutor,
                LocalDateTime.of(2025, 5, 25, 14, 0),
                LocalDateTime.of(2025, 5, 25, 15, 0), 60);

        when(sessionBookingRepository.findByStudentId(studentId))
                .thenReturn(Arrays.asList(booking1, booking2));

        // when
        List<SessionBooking> result = bookingService.getStudentBookings(studentId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDurationMinutes()).isEqualTo(30);
        assertThat(result.get(1).getDurationMinutes()).isEqualTo(60);
    }
}
