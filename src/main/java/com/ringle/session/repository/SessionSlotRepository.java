package com.ringle.session.repository;

import com.ringle.session.entity.SessionSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionSlotRepository extends JpaRepository<SessionSlot, Long> {
}
