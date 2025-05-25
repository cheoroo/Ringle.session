package com.ringle.session.service;

import com.ringle.session.dto.response.AvailableTimeSlotResponse;
import com.ringle.session.dto.response.AvailableTutorsResponse;
import com.ringle.session.entity.SessionSlot;
import com.ringle.session.entity.Tutor;
import com.ringle.session.repository.SessionSlotRepository;
import com.ringle.session.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final SessionSlotRepository sessionSlotRepository;
    private final TimeValidationService timeValidationService;


    public Map<String, List<Object>> getAvailableTimes(LocalDate startDate, LocalDate endDate, int duration) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<SessionSlot> availableSlots = sessionSlotRepository.findAvailableSlotsBetween(startDateTime, endDateTime);

        List<Object> result = new ArrayList<>();

        if (duration == 30) {
            Map<String, List<SessionSlot>> groupedByTime = availableSlots.stream()
                    .filter(slot -> slot.getTutor() != null && slot.getTutor().getId() != null) // null 체크 추가
                    .collect(Collectors.groupingBy(slot ->
                            slot.getStartTime().toString() + "_" + slot.getEndTime().toString()
                    ));

            for (Map.Entry<String, List<SessionSlot>> entry : groupedByTime.entrySet()) {
                List<SessionSlot> slots = entry.getValue();
                if (!slots.isEmpty()) {
                    SessionSlot firstSlot = slots.get(0);
                    List<AvailableTimeSlotResponse.TutorInfo> tutors = slots.stream()
                            .map(slot -> new AvailableTimeSlotResponse.TutorInfo(
                                    slot.getTutor().getId(),
                                    slot.getTutor().getName(),
                                    slot.getTutor().getBio()
                            ))
                            .collect(Collectors.toList());

                    result.add(new AvailableTimeSlotResponse(
                            firstSlot.getStartTime(),
                            firstSlot.getEndTime(),
                            duration,
                            tutors
                    ));
                }
            }
        } else if (duration == 60) {
            Map<Long, List<SessionSlot>> groupedByTutor = availableSlots.stream()
                    .filter(slot -> slot.getTutor() != null && slot.getTutor().getId() != null) // null 체크 추가
                    .collect(Collectors.groupingBy(slot -> slot.getTutor().getId()));

            for (Map.Entry<Long, List<SessionSlot>> entry : groupedByTutor.entrySet()) {
                List<SessionSlot> tutorSlots = entry.getValue()
                        .stream()
                        .sorted(Comparator.comparing(SessionSlot::getStartTime))
                        .collect(Collectors.toList());

                for (int i = 0; i < tutorSlots.size() - 1; i++) {
                    SessionSlot current = tutorSlots.get(i);
                    SessionSlot next = tutorSlots.get(i + 1);

                    if (current.getEndTime().equals(next.getStartTime()) &&
                            current.getDurationMinutes() == 30 && next.getDurationMinutes() == 30) {

                        List<AvailableTimeSlotResponse.TutorInfo> tutors = Arrays.asList(
                                new AvailableTimeSlotResponse.TutorInfo(
                                        current.getTutor().getId(),
                                        current.getTutor().getName(),
                                        current.getTutor().getBio()
                                )
                        );

                        result.add(new AvailableTimeSlotResponse(
                                current.getStartTime(),
                                next.getEndTime(),
                                duration,
                                tutors
                        ));
                    }
                }
            }
        }

        Map<String, List<Object>> response = new HashMap<>();
        response.put("availableSlots", result);
        return response;
    }


    public Map<String, Object> getAvailableTutors(LocalDateTime startTime, int duration) {
        LocalDateTime endTime = startTime.plusMinutes(duration);

        List<SessionSlot> availableSlots = sessionSlotRepository.findAvailableSlotsBetween(startTime, endTime);

        List<AvailableTutorsResponse.TutorInfo> availableTutors = new ArrayList<>();

        if (duration == 30) {
            availableTutors = availableSlots.stream()
                    .filter(slot -> slot.getStartTime().equals(startTime) && slot.getEndTime().equals(endTime))
                    .map(slot -> new AvailableTutorsResponse.TutorInfo(
                            slot.getTutor().getId(),
                            slot.getTutor().getName(),
                            slot.getTutor().getBio()
                    ))
                    .collect(Collectors.toList());

        } else if (duration == 60) {
            Map<Long, List<SessionSlot>> groupedByTutor = availableSlots.stream()
                    .collect(Collectors.groupingBy(slot -> slot.getTutor().getId()));

            for (Map.Entry<Long, List<SessionSlot>> entry : groupedByTutor.entrySet()) {
                List<SessionSlot> tutorSlots = entry.getValue()
                        .stream()
                        .sorted(Comparator.comparing(SessionSlot::getStartTime))
                        .collect(Collectors.toList());

                if (timeValidationService.canAccommodateBooking(startTime, endTime, duration, tutorSlots)) {
                    if (!tutorSlots.isEmpty()) {
                        Tutor tutor = tutorSlots.get(0).getTutor();
                        availableTutors.add(new AvailableTutorsResponse.TutorInfo(
                                tutor.getId(),
                                tutor.getName(),
                                tutor.getBio()
                        ));
                    }
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("requestedTime", new AvailableTutorsResponse.RequestedTimeInfo(startTime, endTime, duration));
        response.put("availableTutors", availableTutors);
        return response;
    }
}
