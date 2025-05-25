package com.ringle.session.service;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.SlotStatus;
import com.ringle.session.entity.Tutor;
import com.ringle.session.exception.ValidationException;
import com.ringle.session.repository.SessionSlotRepository;
import com.ringle.session.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TutorService {

    private final TutorRepository tutorRepository;
    private final SessionSlotRepository sessionSlotRepository;
    private final TimeValidationService timeValidationService;


    @Transactional
    public SessionSlot createTimeSlot(Long tutorId, LocalDateTime startTime, LocalDateTime endTime) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ValidationException("튜터를 찾을 수 없습니다."));

        timeValidationService.validateSlotTime(startTime, endTime);

        List<SessionSlot> overlappingSlots = sessionSlotRepository
                .findOverlappingSlots(tutorId, startTime, endTime, SlotStatus.AVAILABLE);

        timeValidationService.validateNoOverlap(startTime, endTime, overlappingSlots);

        SessionSlot slot = new SessionSlot(tutor, startTime, endTime);
        return sessionSlotRepository.save(slot);
    }

    @Transactional
    public void deleteTimeSlot(Long tutorId, Long slotId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ValidationException("튜터를 찾을 수 없습니다."));

        SessionSlot slot = sessionSlotRepository.findById(slotId)
                .orElseThrow(() -> new ValidationException("시간대를 찾을 수 없습니다."));

        if (!slot.getTutor().getId().equals(tutorId)) {
            throw new ValidationException("해당 시간대를 삭제할 권한이 없습니다.");
        }

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new ValidationException("예약된 시간대는 삭제할 수 없습니다.");
        }

        slot.changeStatus(SlotStatus.DELETED);
        sessionSlotRepository.save(slot);
    }

    public List<SessionSlot> getTutorTimeSlots(Long tutorId, SlotStatus status) {
        return sessionSlotRepository.findByTutorIdAndStatus(tutorId, status);
    }
}
