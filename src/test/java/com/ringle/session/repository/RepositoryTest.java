package com.ringle.session.repository;

import com.ringle.session.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessionSlotRepository sessionSlotRepository;

    @Test
    void 겹치는_시간대_슬롯_조회_테스트() {
        // given
        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        entityManager.persistAndFlush(tutor);

        // 10:00-11:00 슬롯
        LocalDateTime slot1Start = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime slot1End = LocalDateTime.of(2025, 5, 25, 11, 0);
        SessionSlot slot1 = new SessionSlot(tutor, slot1Start, slot1End);
        sessionSlotRepository.save(slot1);

        // when: 10:30-11:30으로 겹치는 시간대 조회
        LocalDateTime searchStart = LocalDateTime.of(2025, 5, 25, 10, 30);
        LocalDateTime searchEnd = LocalDateTime.of(2025, 5, 25, 11, 30);

        List<SessionSlot> overlappingSlots = sessionSlotRepository
                .findOverlappingSlots(tutor.getId(), searchStart, searchEnd, SlotStatus.AVAILABLE);

        // then
        assertThat(overlappingSlots).hasSize(1);
        assertThat(overlappingSlots.get(0).getId()).isEqualTo(slot1.getId());
    }
}
