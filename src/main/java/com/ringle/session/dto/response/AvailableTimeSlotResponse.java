package com.ringle.session.dto.response;

import com.ringle.session.entity.SessionSlot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AvailableTimeSlotResponse {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int duration;
    private final List<TutorInfo> tutors;

    public AvailableTimeSlotResponse(LocalDateTime startTime, LocalDateTime endTime, int duration, List<TutorInfo> tutors) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.tutors = tutors;
    }

    @Getter
    public static class TutorInfo {
        private final Long tutorId;
        private final String name;
        private final String bio;

        public TutorInfo(Long tutorId, String name, String bio) {
            this.tutorId = tutorId;
            this.name = name;
            this.bio = bio;
        }
    }
}
