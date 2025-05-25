package com.ringle.session.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AvailableTutorsResponse {
    private final RequestedTimeInfo requestedTime;
    private final List<TutorInfo> availableTutors;

    public AvailableTutorsResponse(LocalDateTime startTime, LocalDateTime endTime, int duration, List<TutorInfo> tutors) {
        this.requestedTime = new RequestedTimeInfo(startTime, endTime, duration);
        this.availableTutors = tutors;
    }

    @Getter
    public static class RequestedTimeInfo {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final int duration;

        public RequestedTimeInfo(LocalDateTime startTime, LocalDateTime endTime, int duration) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
        }
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
