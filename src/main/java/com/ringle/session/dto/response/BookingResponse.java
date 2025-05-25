package com.ringle.session.dto.response;

import com.ringle.session.entity.SessionBooking;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookingResponse {
    private final Long bookingId;
    private final Long studentId;
    private final Long tutorId;
    private final String tutorName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int duration;
    private final LocalDateTime createdAt;

    public BookingResponse(SessionBooking booking) {
        this.bookingId = booking.getId();
        this.studentId = booking.getStudent().getId();
        this.tutorId = booking.getTutor().getId();
        this.tutorName = booking.getTutor().getName();
        this.startTime = booking.getStartTime();
        this.endTime = booking.getEndTime();
        this.duration = booking.getDurationMinutes();
        this.createdAt = LocalDateTime.now();
    }
}
