package com.ringle.session.repository;

import com.ringle.session.entity.SessionBooking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionBookingRepository extends JpaRepository<SessionBooking, Long> {

    @Override
    @EntityGraph(attributePaths = {"student", "tutor"})
    Optional<SessionBooking> findById(Long id);

    @EntityGraph(attributePaths = {"tutor", "student"})
    List<SessionBooking> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"student", "tutor"})
    List<SessionBooking> findByTutorId(Long tutorId);
}