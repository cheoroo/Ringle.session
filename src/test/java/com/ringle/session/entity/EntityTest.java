package com.ringle.session.entity;

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

}
