package com.ringle.session.repository;

import com.ringle.session.entity.SessionBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionBookingRepository extends JpaRepository<SessionBooking, Long> {

    List<SessionBooking> findByStudentId(Long studentId);

    List<SessionBooking> findByTutorId(Long tutorId);
}
