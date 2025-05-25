package com.ringle.session.service;

import com.ringle.session.entity.*;
import com.ringle.session.exception.ValidationException;
import com.ringle.session.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;
    private final SessionSlotRepository sessionSlotRepository;
    private final SessionBookingRepository sessionBookingRepository;
    private final SlotBookingMappingRepository slotBookingMappingRepository;
    private final TimeValidationService timeValidationService;

    @Transactional
    public SessionBooking createBooking(Long studentId, Long tutorId, LocalDateTime startTime, int durationMinutes) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ValidationException("학생을 찾을 수 없습니다."));

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ValidationException("튜터를 찾을 수 없습니다."));

        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        List<SessionSlot> availableSlots = sessionSlotRepository
                .findAvailableSlotsByTutorAndTimeRange(tutorId, startTime, endTime);

        if (!timeValidationService.canAccommodateBooking(startTime, endTime, durationMinutes, availableSlots)) {
            throw new ValidationException("요청한 시간대에 예약 가능한 슬롯이 충분하지 않습니다.");
        }

        List<SessionSlot> requiredSlots = timeValidationService.findRequiredSlots(startTime, endTime, availableSlots);

        SessionBooking booking = new SessionBooking(student, tutor, startTime, endTime, durationMinutes);
        SessionBooking savedBooking = sessionBookingRepository.save(booking);

        for (SessionSlot slot : requiredSlots) {
            slot.changeStatus(SlotStatus.BOOKED);
            sessionSlotRepository.save(slot);

            SlotBookingMapping mapping = new SlotBookingMapping(slot, savedBooking);
            slotBookingMappingRepository.save(mapping);
        }

        return savedBooking;
    }


    public SessionBooking getBooking(Long studentId, Long bookingId) {
        SessionBooking booking = sessionBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ValidationException("예약을 찾을 수 없습니다."));

        if (!booking.getStudent().getId().equals(studentId)) {
            throw new ValidationException("해당 예약에 접근할 권한이 없습니다.");
        }

        return booking;
    }

    public List<SessionBooking> getStudentBookings(Long studentId) {
        return sessionBookingRepository.findByStudentId(studentId);
    }
}
