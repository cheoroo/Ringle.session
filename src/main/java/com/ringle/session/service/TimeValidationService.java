package com.ringle.session.service;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeValidationService {

    public void validateSlotTime(LocalDateTime startTime, LocalDateTime endTime) {

        if (startTime.getMinute() != 0 && startTime.getMinute() != 30) {
            throw new ValidationException("시작 시간은 정각 또는 30분이어야 합니다.");
        }

        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes < 30 || minutes % 30 != 0) {
            throw new ValidationException("수업 시간은 최소 30분이며 30분 단위여야 합니다.");
        }
        if (minutes > 60) {
            throw new ValidationException("한 번의 등록 요청으로는 최대 60분까지만 등록 가능합니다.");
        }
    }

    public void validateNoOverlap(LocalDateTime startTime, LocalDateTime endTime, List<SessionSlot> existingSlots) {
        boolean hasOverlap = existingSlots.stream()
                .anyMatch(slot ->
                        startTime.isBefore(slot.getEndTime()) && endTime.isAfter(slot.getStartTime())
                );

        if (hasOverlap) {
            throw new ValidationException("해당 시간대에 이미 등록된 수업이 있습니다.");
        }
    }


    public void validateConsecutiveSlots(List<SessionSlot> slots) {
        if (slots.size() <= 1) {
            return; // 슬롯 1개 이하시 검증 불필요
        }

        for (int i = 0; i < slots.size() - 1; i++) {
            SessionSlot current = slots.get(i);
            SessionSlot next = slots.get(i + 1);

            if (!current.getEndTime().equals(next.getStartTime())) {
                throw new ValidationException("선택된 슬롯들이 연속되지 않습니다.");
            }
        }
    }

    public void validateBookingRequest(LocalDateTime requestStart, LocalDateTime requestEnd,
                                       int requestedDuration, List<SessionSlot> availableSlots) {

        validateSlotTime(requestStart, requestEnd);

        List<SessionSlot> matchingSlots = availableSlots.stream()
                .filter(slot ->
                        !slot.getStartTime().isBefore(requestStart) &&
                                !slot.getEndTime().isAfter(requestEnd)
                )
                .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
                .toList();

        int totalAvailableMinutes = matchingSlots.stream()
                .mapToInt(SessionSlot::getDurationMinutes)
                .sum();

        if (totalAvailableMinutes < requestedDuration) {
            throw new ValidationException("요청한 시간대에 충분한 수업 슬롯이 없습니다.");
        }

        if (requestedDuration == 60) {
            validateConsecutiveSlots(matchingSlots);
        }
    }

    public boolean canAccommodateBooking(LocalDateTime requestStart, LocalDateTime requestEnd,
                                         int requestedDuration, List<SessionSlot> availableSlots) {
        try {
            validateBookingRequest(requestStart, requestEnd, requestedDuration, availableSlots);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    public List<SessionSlot> findRequiredSlots(LocalDateTime requestStart, LocalDateTime requestEnd,
                                               List<SessionSlot> availableSlots) {
        return availableSlots.stream()
                .filter(slot ->
                        !slot.getStartTime().isBefore(requestStart) &&
                                !slot.getEndTime().isAfter(requestEnd)
                )
                .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
                .toList();
    }
}
