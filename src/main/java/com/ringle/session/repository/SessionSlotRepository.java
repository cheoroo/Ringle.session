package com.ringle.session.repository;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionSlotRepository extends JpaRepository<SessionSlot, Long> {

    List<SessionSlot> findByTutorIdAndStatus(Long tutorId, SlotStatus status);

    @Query("SELECT s FROM SessionSlot s WHERE s.status = 'AVAILABLE' " +
            "AND s.startTime >= :startTime AND s.endTime <= :endTime " +
            "ORDER BY s.startTime")
    List<SessionSlot> findAvailableSlotsBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT s FROM SessionSlot s WHERE s.tutor.id = :tutorId " +
            "AND s.status = :status " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<SessionSlot> findOverlappingSlots(
            @Param("tutorId") Long tutorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") SlotStatus status
    );

}
