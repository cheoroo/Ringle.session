package com.ringle.session.controller;

import com.ringle.session.dto.request.TimeSlotCreateRequest;
import com.ringle.session.dto.response.ApiResponse;
import com.ringle.session.dto.response.TimeSlotResponse;
import com.ringle.session.entity.SessionSlot;
import com.ringle.session.service.TutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    @PostMapping("/{tutorId}/time-slots")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TimeSlotResponse> createTimeSlot(
            @PathVariable Long tutorId,
            @Valid @RequestBody TimeSlotCreateRequest request) {

        SessionSlot slot = tutorService.createTimeSlot(
                tutorId,
                request.getStartTime(),
                request.getEndTime()
        );

        TimeSlotResponse response = new TimeSlotResponse(slot);
        return ApiResponse.success(response, "시간대가 성공적으로 생성되었습니다.");
    }

    @DeleteMapping("/{tutorId}/time-slots/{slotId}")
    public ApiResponse<Void> deleteTimeSlot(
            @PathVariable Long tutorId,
            @PathVariable Long slotId) {

        tutorService.deleteTimeSlot(tutorId, slotId);
        return ApiResponse.success("시간대가 성공적으로 삭제되었습니다.");
    }
}
