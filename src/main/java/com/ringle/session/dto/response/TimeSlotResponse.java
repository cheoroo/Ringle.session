package com.ringle.session.dto.response;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.SlotStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TimeSlotResponse {
    private final Long slotId;
    private final Long tutorId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final SlotStatus status;
    private final int durationMinutes;

    public TimeSlotResponse(SessionSlot slot) {
        this.slotId = slot.getId();
        this.tutorId = slot.getTutor().getId();
        this.startTime = slot.getStartTime();
        this.endTime = slot.getEndTime();
        this.status = slot.getStatus();
        this.durationMinutes = slot.getDurationMinutes();
    }
}
