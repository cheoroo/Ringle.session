package com.ringle.session.entity;

import com.ringle.session.repository.SessionBookingRepository;
import com.ringle.session.repository.SessionSlotRepository;
import com.ringle.session.repository.StudentRepository;
import com.ringle.session.repository.TutorRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class EntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private SessionSlotRepository sessionSlotRepository;

    @Autowired
    private SessionBookingRepository sessionBookingRepository;

    @Test
    void 학생_엔티티_생성_및_저장_테스트() {
        // given
        Student student = new Student("학생1", "student@example.com");

        // when
        Student savedStudent = studentRepository.save(student);

        // then
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedStudent.getName()).isEqualTo("학생1");
        assertThat(savedStudent.getEmail()).isEqualTo("student@example.com");
    }

    @Test
    void 튜터_엔티티_생성_및_저장_테스트() {
        // given
        Tutor tutor = new Tutor("튜터1", "튜터설명1");

        // when
        Tutor savedTutor = tutorRepository.save(tutor);

        // then
        assertThat(savedTutor.getId()).isNotNull();
        assertThat(savedTutor.getName()).isEqualTo("튜터1");
        assertThat(savedTutor.getBio()).isEqualTo("튜터설명1");
    }

    @Test
    void 세션슬롯_엔티티_생성_및_저장_테스트() {
        // given
        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        entityManager.persistAndFlush(tutor);

        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 11, 0);
        SessionSlot slot = new SessionSlot(tutor, startTime, endTime);

        // when
        SessionSlot savedSlot = sessionSlotRepository.save(slot);

        // then
        assertThat(savedSlot.getId()).isNotNull();
        assertThat(savedSlot.getTutor().getId()).isEqualTo(tutor.getId());
        assertThat(savedSlot.getStartTime()).isEqualTo(startTime);
        assertThat(savedSlot.getEndTime()).isEqualTo(endTime);
        assertThat(savedSlot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void 튜터로_세션슬롯_조회_테스트() {
        // given
        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        entityManager.persistAndFlush(tutor);

        LocalDateTime startTime1 = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime1 = LocalDateTime.of(2025, 5, 25, 10, 30);
        LocalDateTime startTime2 = LocalDateTime.of(2025, 5, 25, 11, 0);
        LocalDateTime endTime2 = LocalDateTime.of(2025, 5, 25, 11, 30);

        SessionSlot slot1 = new SessionSlot(tutor, startTime1, endTime1);
        SessionSlot slot2 = new SessionSlot(tutor, startTime2, endTime2);

        sessionSlotRepository.save(slot1);
        sessionSlotRepository.save(slot2);

        // when
        var slots = sessionSlotRepository.findByTutorIdAndStatus(tutor.getId(), SlotStatus.AVAILABLE);

        // then
        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getTutor().getId()).isEqualTo(tutor.getId());
        assertThat(slots.get(1).getTutor().getId()).isEqualTo(tutor.getId());
    }

    @Test
    void 기간으로_가능한_슬롯_조회_테스트() {
        // given
        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        entityManager.persistAndFlush(tutor);

        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 10, 30);
        SessionSlot slot = new SessionSlot(tutor, startTime, endTime);
        sessionSlotRepository.save(slot);

        // when
        LocalDateTime searchStart = LocalDateTime.of(2025, 5, 25, 0, 0);
        LocalDateTime searchEnd = LocalDateTime.of(2025, 5, 25, 23, 59);
        var slots = sessionSlotRepository.findAvailableSlotsBetween(searchStart, searchEnd);

        // then
        assertThat(slots).hasSize(1);
        assertThat(slots.get(0).getStartTime()).isEqualTo(startTime);
    }

    @Test
    void 세션예약_엔티티_생성_및_저장_테스트() {
        // given
        Student student = new Student("학생1", "student@example.com");
        Tutor tutor = new Tutor("튜터1", "튜터설명1");
        entityManager.persistAndFlush(student);
        entityManager.persistAndFlush(tutor);

        LocalDateTime startTime = LocalDateTime.of(2025, 5, 25, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 5, 25, 11, 0);
        SessionBooking booking = new SessionBooking(student, tutor, startTime, endTime, 60);

        // when
        SessionBooking savedBooking = sessionBookingRepository.save(booking);

        // then
        assertThat(savedBooking.getId()).isNotNull();
        assertThat(savedBooking.getStudent().getId()).isEqualTo(student.getId());
        assertThat(savedBooking.getTutor().getId()).isEqualTo(tutor.getId());
        assertThat(savedBooking.getStartTime()).isEqualTo(startTime);
        assertThat(savedBooking.getEndTime()).isEqualTo(endTime);
        assertThat(savedBooking.getDurationMinutes()).isEqualTo(60);
    }

}
