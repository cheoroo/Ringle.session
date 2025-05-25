package com.ringle.session.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequest {

    @NotNull(message = "튜터 ID는 필수입니다.")
    private Long tutorId;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    @Min(value = 30, message = "수업 시간은 최소 30분입니다.")
    private int durationMinutes;
}
