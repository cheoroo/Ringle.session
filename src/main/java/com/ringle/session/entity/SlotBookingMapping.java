package com.ringle.session.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "slot_booking_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlotBookingMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private SessionSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private SessionBooking booking;

    public SlotBookingMapping(SessionSlot slot, SessionBooking booking) {
        this.slot = slot;
        this.booking = booking;
    }
}
