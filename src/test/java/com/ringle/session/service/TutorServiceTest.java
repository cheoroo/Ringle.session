package com.ringle.session.service;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.SlotStatus;
import com.ringle.session.entity.Tutor;
import com.ringle.session.exception.ValidationException;
import com.ringle.session.repository.SessionSlotRepository;
import com.ringle.session.repository.TutorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private SessionSlotRepository sessionSlotRepository;

    @Mock
    private TimeValidationService timeValidationService;

    @InjectMocks
    private TutorService tutorService;

    @Test
    void 시간대_생성_성공() {
        // given
        Long tutorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 11, 0);

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", tutorId);

        SessionSlot expectedSlot = new SessionSlot(tutor, startTime, endTime);
        ReflectionTestUtils.setField(expectedSlot, "id", 1L);

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(sessionSlotRepository.findOverlappingSlots(eq(tutorId), eq(startTime), eq(endTime), eq(SlotStatus.AVAILABLE)))
                .thenReturn(Collections.emptyList());
        when(sessionSlotRepository.save(any(SessionSlot.class))).thenReturn(expectedSlot);

        // when
        SessionSlot result = tutorService.createTimeSlot(tutorId, startTime, endTime);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTutor()).isEqualTo(tutor);
        assertThat(result.getStartTime()).isEqualTo(startTime);
        assertThat(result.getEndTime()).isEqualTo(endTime);
        assertThat(result.getStatus()).isEqualTo(SlotStatus.AVAILABLE);

        verify(timeValidationService).validateSlotTime(startTime, endTime);
        verify(timeValidationService).validateNoOverlap(eq(startTime), eq(endTime), anyList());
        verify(sessionSlotRepository).save(any(SessionSlot.class));
    }

    @Test
    void 존재하지_않는_튜터_시간대_생성_실패() {
        // given
        Long tutorId = 999L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 11, 0);

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tutorService.createTimeSlot(tutorId, startTime, endTime))
                .isInstanceOf(ValidationException.class)
                .hasMessage("튜터를 찾을 수 없습니다.");
    }

    @Test
    void 겹치는_시간대_생성_실패() {
        // given
        Long tutorId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 11, 0);

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        SessionSlot existingSlot = new SessionSlot(tutor, startTime.minusMinutes(30), startTime.plusMinutes(30));

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(sessionSlotRepository.findOverlappingSlots(eq(tutorId), eq(startTime), eq(endTime), eq(SlotStatus.AVAILABLE)))
                .thenReturn(Arrays.asList(existingSlot));
        doThrow(new ValidationException("해당 시간대에 이미 등록된 수업이 있습니다."))
                .when(timeValidationService).validateNoOverlap(eq(startTime), eq(endTime), anyList());

        // when & then
        assertThatThrownBy(() -> tutorService.createTimeSlot(tutorId, startTime, endTime))
                .isInstanceOf(ValidationException.class)
                .hasMessage("해당 시간대에 이미 등록된 수업이 있습니다.");
    }

    @Test
    void 시간대_삭제_성공() {
        // given
        Long tutorId = 1L;
        Long slotId = 1L;

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", tutorId);

        SessionSlot slot = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 11, 0)
        );
        ReflectionTestUtils.setField(slot, "id", slotId);

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(sessionSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        // when
        tutorService.deleteTimeSlot(tutorId, slotId);

        // then
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.DELETED);
        verify(sessionSlotRepository).save(slot);
    }

    @Test
    void 예약된_시간대_삭제_실패() {
        // given
        Long tutorId = 1L;
        Long slotId = 1L;

        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(tutor, "id", tutorId);

        SessionSlot slot = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 11, 0)
        );
        ReflectionTestUtils.setField(slot, "id", slotId);
        slot.changeStatus(SlotStatus.BOOKED); // 이미 예약됨

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(sessionSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        // when & then
        assertThatThrownBy(() -> tutorService.deleteTimeSlot(tutorId, slotId))
                .isInstanceOf(ValidationException.class)
                .hasMessage("예약된 시간대는 삭제할 수 없습니다.");
    }

    @Test
    void 다른_튜터의_시간대_삭제_실패() {
        // given
        Long tutorId = 1L;
        Long slotId = 1L;

        Tutor requestTutor = new Tutor("튜터1", "튜터설명1");
        ReflectionTestUtils.setField(requestTutor, "id", tutorId);

        Tutor slotOwner = new Tutor("튜터2", "튜터설명2");
        ReflectionTestUtils.setField(slotOwner, "id", 2L);

        SessionSlot slot = new SessionSlot(slotOwner,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 11, 0)
        );

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(requestTutor));
        when(sessionSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        // when & then
        assertThatThrownBy(() -> tutorService.deleteTimeSlot(tutorId, slotId))
                .isInstanceOf(ValidationException.class)
                .hasMessage("해당 시간대를 삭제할 권한이 없습니다.");
    }
}
