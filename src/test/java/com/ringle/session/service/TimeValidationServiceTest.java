package com.ringle.session.service;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.Tutor;
import com.ringle.session.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TimeValidationServiceTest {

    @InjectMocks
    private TimeValidationService timeValidationService;

    private Tutor tutor;

    @BeforeEach
    void setUp() {
        tutor = new Tutor("튜터1", "튜터설명1");
    }

    @Test
    void 정각_시작_시간_검증_성공() {
        // given
        LocalDateTime validTime = LocalDateTime.of(2025, 5, 25, 10, 0);

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateSlotTime(validTime, validTime.plusMinutes(30))
        );
    }

    @Test
    void 시작_시간_30분_검증_성공() {
        // given
        LocalDateTime validTime = LocalDateTime.of(2025, 5, 25, 10, 30);

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateSlotTime(validTime, validTime.plusMinutes(30))
        );
    }

    @Test
    void 시작_시간_15분_검증_실패() {
        // given
        LocalDateTime invalidTime = LocalDateTime.of(2025, 5, 25, 10, 15);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateSlotTime(invalidTime, invalidTime.plusMinutes(30))
        ).isInstanceOf(ValidationException.class)
                .hasMessage("시작 시간은 정각 또는 30분이어야 합니다.");
    }

    @Test
    void 시작_시간_45분_검증_실패() {
        // given
        LocalDateTime invalidTime = LocalDateTime.of(2025, 5, 25, 10, 45);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateSlotTime(invalidTime, invalidTime.plusMinutes(30))
        ).isInstanceOf(ValidationException.class)
                .hasMessage("시작 시간은 정각 또는 30분이어야 합니다.");
    }

    @Test
    void 수업_길이_30분_검증_성공() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(30);

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateSlotTime(startTime, endTime)
        );
    }

    @Test
    void 수업_길이_60분_검증_성공() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(60);

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateSlotTime(startTime, endTime)
        );
    }

    @Test
    void 수업_길이_15분_검증_실패() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(15);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateSlotTime(startTime, endTime)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("수업 시간은 최소 30분이며 30분 단위여야 합니다.");
    }

    @Test
    void 수업_길이_45분_검증_실패() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(45);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateSlotTime(startTime, endTime)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("수업 시간은 최소 30분이며 30분 단위여야 합니다.");
    }

    @Test
    void 시작시간이_종료시간보다_늦으면_검증_실패() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 11, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 10, 0);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateSlotTime(startTime, endTime)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("시작 시간은 종료 시간보다 빨라야 합니다.");
    }

    @Test
    void 겹치는_슬롯이_없으면_슬롯_등록_검증_성공() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 10, 30);
        List<SessionSlot> existingSlots = Collections.emptyList();

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateNoOverlap(startTime, endTime, existingSlots)
        );
    }

    @Test
    void 겹치는_슬롯이_있으면_슬롯_등록_검증_실패() {
        // given
        LocalDateTime newStartTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime newEndTime = LocalDateTime.of(2025, 5, 25, 10, 30);

        LocalDateTime existingStart = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime existingEnd = LocalDateTime.of(2025, 5, 25, 10, 30);
        SessionSlot existingSlot = new SessionSlot(tutor, existingStart, existingEnd);

        List<SessionSlot> existingSlots = Arrays.asList(existingSlot);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateNoOverlap(newStartTime, newEndTime, existingSlots)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("해당 시간대에 이미 등록된 수업이 있습니다.");
    }

    @Test
    void 연속된_슬롯_검증_성공() {
        // given
        LocalDateTime time1 = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2025, 5, 25, 10, 30);
        LocalDateTime time3 = LocalDateTime.of(2025, 5, 25, 11, 0);

        SessionSlot slot1 = new SessionSlot(tutor, time1, time2);
        SessionSlot slot2 = new SessionSlot(tutor, time2, time3);

        //10:00~10:30 + 10:30~11:00
        List<SessionSlot> slots = Arrays.asList(slot1, slot2);

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateConsecutiveSlots(slots)
        );
    }

    @Test
    void 연속되지_않은_슬롯_검증_실패() {
        // given
        LocalDateTime time1 = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2025, 5, 25, 10, 30);
        LocalDateTime time3 = LocalDateTime.of(2025, 5, 25, 11, 30);
        LocalDateTime time4 = LocalDateTime.of(2025, 5, 25, 12, 0);

        SessionSlot slot1 = new SessionSlot(tutor, time1, time2);
        SessionSlot slot2 = new SessionSlot(tutor, time3, time4);

        //10:00~10:30 + 11:30~12:00
        List<SessionSlot> slots = Arrays.asList(slot1, slot2);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateConsecutiveSlots(slots)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("선택된 슬롯들이 연속되지 않습니다.");
    }

    @Test
    void 충분한_시간_슬롯_예약_검증_성공() {
        // given
        LocalDateTime requestStart = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime requestEnd = LocalDateTime.of(2025, 5, 25, 11, 0);
        int requestedDuration = 60;

        // 사용 가능한 슬롯들: 10:00-10:30, 10:30-11:00
        SessionSlot slot1 = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30)
        );
        SessionSlot slot2 = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 10, 30),
                LocalDateTime.of(2025, 5, 25, 11, 0)
        );

        List<SessionSlot> availableSlots = Arrays.asList(slot1, slot2);

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateBookingRequest(requestStart, requestEnd, requestedDuration, availableSlots)
        );
    }

    @Test
    void 충분하지_않은_시간_예약_검증_실패() {
        // given
        LocalDateTime requestStart = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime requestEnd = LocalDateTime.of(2025, 5, 25, 11, 0);
        int requestedDuration = 60;

        // 사용 가능한 슬롯이 10:00-10:30만 있을때 60분 실패
        SessionSlot slot1 = new SessionSlot(tutor,
                LocalDateTime.of(2025, 5, 25, 10, 0),
                LocalDateTime.of(2025, 5, 25, 10, 30)
        );

        List<SessionSlot> availableSlots = Arrays.asList(slot1);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateBookingRequest(requestStart, requestEnd, requestedDuration, availableSlots)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("요청한 시간대에 충분한 수업 슬롯이 없습니다.");
    }

    @Test
    void 단일_등록_90분_제한_실패() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(90);

        // when & then
        assertThatThrownBy(() ->
                timeValidationService.validateSlotTime(startTime, endTime)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("한 번의 등록 요청으로는 최대 60분까지만 등록 가능합니다.");
    }

    @Test
    void 연속_슬롯_등록_허용_확인() {
        // given
        Tutor tutor = new Tutor("튜터1", "튜터설명1");

        LocalDateTime time1 = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2025, 5, 25, 10, 30);
        LocalDateTime time3 = LocalDateTime.of(2025, 5, 25, 11, 0);
        LocalDateTime time4 = LocalDateTime.of(2025, 5, 25, 11, 30);

        //10:00~11:30까지 30분씩 3개 연속
        SessionSlot slot1 = new SessionSlot(tutor, time1, time2);
        SessionSlot slot2 = new SessionSlot(tutor, time2, time3);
        SessionSlot slot3 = new SessionSlot(tutor, time3, time4);

        List<SessionSlot> consecutiveSlots = Arrays.asList(slot1, slot2, slot3);

        // when & then
        assertThatNoException().isThrownBy(() ->
                timeValidationService.validateConsecutiveSlots(consecutiveSlots)
        );
    }
}