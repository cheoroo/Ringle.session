package com.ringle.session.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;

    public SessionSlot(Tutor tutor, LocalDateTime startTime, LocalDateTime endTime) {
        this.tutor = tutor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = SlotStatus.AVAILABLE;
    }

}
