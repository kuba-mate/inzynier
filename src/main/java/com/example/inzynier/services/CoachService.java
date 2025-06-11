package com.example.inzynier.services;

import com.example.inzynier.models.*;
import com.example.inzynier.models.enums.TicketType;
import com.example.inzynier.models.form.TrainingMonitorForm;
import com.example.inzynier.repositories.GroupTrainingRepository;
import com.example.inzynier.repositories.IndividualTrainingRepository;
import com.example.inzynier.repositories.ReservationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CoachService {

    @Autowired
    private IndividualTrainingRepository individualTrainingRepository;
    @Autowired
    private GroupTrainingRepository groupTrainingRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    public List<TrainingMonitorForm> setTrainingMonitorForm(final HttpServletRequest request){
        final Coach coach = (Coach) request.getSession().getAttribute("user");
        final List<TrainingMonitorForm> trainingMonitorForms = new ArrayList<>();
        final List<IndividualTraining> individualTrainings = individualTrainingRepository.findAllByCoach(coach);
        final List<GroupTraining> groupTrainings = groupTrainingRepository.findAllByCoach(coach);
        for(final IndividualTraining individualTraining : individualTrainings){
            final Reservation reservation = reservationRepository.getReservationByClientAndNumberOfEntriesLeftGreaterThan(individualTraining.getClient(), 0);
            if(reservation != null && individualTraining.getEndDate().isAfter(LocalDateTime.now())) {
                trainingMonitorForms.add(TrainingMonitorForm.builder()
                        .id(individualTraining.getTraining_id())
                        .date(formatIndividualTrainingFormatDate(individualTraining.getStartDate()))
                        .startHour(formatIndividualTrainingHour(individualTraining.getStartDate()))
                        .endHour(formatIndividualTrainingHour(individualTraining.getEndDate()))
                        .room(String.valueOf(individualTraining.getRoom().getRoom_id()))
                        .ticketType(TicketType.INDIVIDUAL)
                        .build());
            }
        }
        for(final GroupTraining groupTraining : groupTrainings){
            trainingMonitorForms.add(TrainingMonitorForm.builder()
                    .id(groupTraining.getTraining_id())
                    .date(groupTraining.getTrainingDay())
                    .startHour(groupTraining.getStartHour())
                    .endHour(groupTraining.getEndHour())
                    .room(String.valueOf(groupTraining.getRoom().getRoom_id()))
                    .ticketType(TicketType.GROUP)
                    .build());
        }
        return trainingMonitorForms.stream().sorted(Comparator.comparingLong(TrainingMonitorForm::getId)).toList();
    }

    public Map<String, Object> setDetailsMap(final Long trainingId){
        final Map<String, Object> detailsMap = new HashMap<>();
        if(individualTrainingRepository.existsById(trainingId)){
            final IndividualTraining individualTraining = individualTrainingRepository.getReferenceById(trainingId);
            final Client client = individualTraining.getClient();
            final Reservation reservation = reservationRepository.getReservationByClientAndNumberOfEntriesLeftGreaterThan(client, 0);
            detailsMap.put("id", trainingId);
            detailsMap.put("date", formatIndividualTrainingFormatDate(individualTraining.getStartDate()));
            detailsMap.put("startHour", formatIndividualTrainingHour(individualTraining.getStartDate()));
            detailsMap.put("endHour", formatIndividualTrainingHour(individualTraining.getEndDate()));
            detailsMap.put("room", String.valueOf(individualTraining.getRoom().getRoom_id()));
            detailsMap.put("type", TicketType.INDIVIDUAL);
            detailsMap.put("client", client.getName() + " " + client.getLastName());
            detailsMap.put("goal", reservation.getIndividualTrainingGoals());
            detailsMap.put("numberOfEntries", reservation.getNumberOfEntriesLeft());
            detailsMap.put("report", reservation.getIndividualTrainingReport());
            detailsMap.put("reservationId", reservation.getId());
        } else {
            final GroupTraining groupTraining = groupTrainingRepository.getReferenceById(trainingId);
            final GroupTicket groupTicket = groupTraining.getGroupTicket();
            detailsMap.put("id", trainingId);
            detailsMap.put("date", groupTraining.getTrainingDay());
            detailsMap.put("startHour", groupTraining.getStartHour());
            detailsMap.put("endHour", groupTraining.getEndHour());
            detailsMap.put("room", String.valueOf(groupTraining.getRoom().getRoom_id()));
            detailsMap.put("type", TicketType.GROUP);
            detailsMap.put("groupSize", groupTraining.getGroupSize());
            detailsMap.put("level", groupTicket.getLevelOfAdvancement());
        }
        return detailsMap;
    }

    public Boolean saveRaport(final Long id, final String raport){
        if(reservationRepository.existsById(id)){
            final Reservation reservation = reservationRepository.getReferenceById(id);
            reservation.setIndividualTrainingReport(raport);
            reservationRepository.save(reservation);
            return true;
        }
        return false;
    }

    private String formatIndividualTrainingFormatDate(final LocalDateTime date){
        final DayOfWeek dayOfWeek = date.getDayOfWeek();
        final String dayOfWeekInString = translateToPolish(dayOfWeek);
        final String formattedDate = addZeroIfValueUnderTen(date.getDayOfMonth()) + "." + addZeroIfValueUnderTen(date.getMonthValue()) + "." + date.getYear();
        return dayOfWeekInString + " (" + formattedDate + ")";
    }

    private String formatIndividualTrainingHour(final LocalDateTime date){
        final String hour = addZeroIfValueUnderTen(date.getHour());
        final String minute = addZeroIfValueUnderTen(date.getMinute());
        return hour + ":" + minute;
    }

    private String addZeroIfValueUnderTen(final Integer value){
        if(value < 10)
            return "0" + value;
        else
            return value.toString();
    }

    private String translateToPolish(final DayOfWeek dayOfWeek){
        switch (dayOfWeek){
            case MONDAY -> {
                return "poniedziałek";
            }
            case TUESDAY -> {
                return "wtorek";
            }
            case WEDNESDAY -> {
                return "środa";
            }
            case THURSDAY -> {
                return "czwartek";
            }
            case FRIDAY -> {
                return "piątek";
            }
            case SATURDAY -> {
                return "sobota";
            }
            case SUNDAY -> {
                return "niedziela";
            }
        }
        return null;
    }

}
