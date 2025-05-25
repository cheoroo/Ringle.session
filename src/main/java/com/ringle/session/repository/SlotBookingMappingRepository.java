package com.ringle.session.repository;

import com.ringle.session.entity.SlotBookingMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlotBookingMappingRepository extends JpaRepository<SlotBookingMapping, Long> {

    List<SlotBookingMapping> findByBookingId(Long bookingId);

    List<SlotBookingMapping> findBySlotId(Long slotId);
}
