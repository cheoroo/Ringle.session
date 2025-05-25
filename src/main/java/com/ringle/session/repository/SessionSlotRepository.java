package com.ringle.session.repository;

import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.SlotStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionSlotRepository extends JpaRepository<SessionSlot, Long> {

    @Override
    @EntityGraph(attributePaths = {"tutor"})
    Optional<SessionSlot> findById(Long id);

    @Query("SELECT s FROM SessionSlot s JOIN FETCH s.tutor WHERE s.tutor.id = :tutorId AND s.status = :status")
    List<SessionSlot> findByTutorIdAndStatus(@Param("tutorId") Long tutorId, @Param("status") SlotStatus status);

    @Query("SELECT s FROM SessionSlot s JOIN FETCH s.tutor WHERE s.status = 'AVAILABLE' " +
            "AND s.startTime < :endTime AND s.endTime > :startTime " +
            "ORDER BY s.startTime")
    List<SessionSlot> findAvailableSlotsBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT s FROM SessionSlot s JOIN FETCH s.tutor WHERE s.tutor.id = :tutorId " +
            "AND s.status = :status " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<SessionSlot> findOverlappingSlots(
            @Param("tutorId") Long tutorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") SlotStatus status
    );

    @Query("SELECT s FROM SessionSlot s JOIN FETCH s.tutor WHERE s.tutor.id = :tutorId " +
            "AND s.status = 'AVAILABLE' " +
            "AND s.startTime < :endTime AND s.endTime > :startTime " +
            "ORDER BY s.startTime")
    List<SessionSlot> findAvailableSlotsByTutorAndTimeRange(
            @Param("tutorId") Long tutorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

}