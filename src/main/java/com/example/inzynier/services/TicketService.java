package com.example.inzynier.services;

import com.example.inzynier.models.*;
import com.example.inzynier.models.dto.GroupTicketForm;
import com.example.inzynier.models.dto.TrainingManagerDtoHelper;
import com.example.inzynier.models.dto.TraningManagerDto;
import com.example.inzynier.models.enums.SportType;
import com.example.inzynier.models.enums.TicketType;
import com.example.inzynier.models.exception.GroupTicketAkreadyTakenException;
import com.example.inzynier.models.exception.MaxOneGymTicketException;
import com.example.inzynier.models.exception.MaxOneIndividualTicketException;
import com.example.inzynier.models.exception.NotAStudentException;
import com.example.inzynier.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private GymTicketRepository gymTicketRepository;
    @Autowired
    private GroupTicketRepository groupTicketRepository;
    @Autowired
    private GroupTrainingRepository groupTrainingRepository;
    @Autowired
    private IndividualTicketRepository individualTicketRepository;
    @Autowired
    private IndividualTrainingRepository individualTrainingRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private SportDisciplineRepository sportDisciplineRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private TrainingRepository trainingRepository;

    public List<Ticket> findAllTickets(){
        return ticketRepository.findAll();
    }
    public List<GymTicket> findAllGymTickets(){
        return gymTicketRepository.findAll();
    }
    public List<GroupTicket> findAllGroupTickets(){
        return groupTicketRepository.findAll();
    }
    public List<IndividualTicket> findAllIndividualTickets(){
        return individualTicketRepository.findAll();
    }
    public Ticket getTicketById(final Long id){
        return ticketRepository.getTicketById(id);
    }

    public void saveReservation(final Long ticketId, final HttpServletRequest request, final IndividualTraining individualTraining) throws Exception {
        final Optional<IndividualTicket> individualTicket = individualTicketRepository.findById(ticketId);
        if(individualTicket.isPresent() && individualTraining != null){
            saveNonPeriodicReservation(individualTicket.get(), request, individualTraining);
        } else {
            savePeriodicReservation(ticketId, request);
        }
    }
    public void savePeriodicReservation(final Long ticketId, HttpServletRequest request) throws Exception {
        final Client client = (Client) request.getSession().getAttribute("user");
        final TicketType ticketType = checkTicketsType(ticketId);
        if(ticketType.equals(TicketType.GYM)){
            saveGymTicketReservation(ticketId, client);
        } else if(ticketType.equals(TicketType.GROUP)) {
            saveGroupTicketReservation(ticketId, client);
        }
    }

    private void saveGroupTicketReservation(final Long ticketId, final Client client) throws Exception {
        final GroupTicket groupTicket = groupTicketRepository.findById(ticketId).get();
        if(!validateGroupTicketReservation(client, ticketId)){
            throw new GroupTicketAkreadyTakenException();
        }
        final Reservation reservation = new Reservation();
        reservation.setEndDate(LocalDate.now().plusMonths(1));
        reservation.setClient(client);
        reservation.setNumberOfEntriesLeft(null);
        reservation.setTicket(groupTicket);
        for(final GroupTraining groupTraining : groupTicket.getGroupTrainings()){
            groupTraining.increaseGroupSizeByOne();
            groupTrainingRepository.save(groupTraining);
        }
        reservationRepository.save(reservation);
    }

    private void saveGymTicketReservation(final Long ticketId, final Client client) throws Exception {
        final GymTicket gymTicket = gymTicketRepository.getReferenceById(ticketId);
        if(gymTicket.isOnlyStudent() && !client.getIsStudent()){
            throw new NotAStudentException();
        }
        if(!validateGymTicketReservation(client)){
            throw new MaxOneGymTicketException();
        }
        final Reservation reservation = new Reservation();
        reservation.setEndDate(LocalDate.now().plusMonths(1));
        reservation.setClient(client);
        reservation.setNumberOfEntriesLeft(null);
        reservation.setTicket(gymTicket);
        reservationRepository.save(reservation);
    }

    private boolean validateGymTicketReservation(final Client client){
        final List<Reservation> reservations = reservationRepository.getReservationsByClientAndNumberOfEntriesLeftNullAndEndDateIsAfter(client, LocalDate.now());
        for (final Reservation reservation : reservations){
            final TicketType ticketType = checkTicketsType(reservation.getTicket().getId());
            if(ticketType.equals(TicketType.GYM)){
                return false;
            }
        }
        return true;
    }

    private Boolean validateIndividualReservations(final Client client){
        final Reservation reservation = reservationRepository.getReservationByClientAndNumberOfEntriesLeftGreaterThan(client, 0);
        if(reservation != null){
            return false;
        }
        return true;
    }

    private Boolean validateGroupTicketReservation(final Client client, final Long ticketId){
        final List<Reservation> reservations = reservationRepository.getReservationsByClientAndNumberOfEntriesLeftNullAndEndDateIsAfter(client, LocalDate.now());
        for (final Reservation reservation : reservations){
            if(ticketId.equals(reservation.getTicket().getId())){
                return false;
            }
        }
        return true;
    }

    public void saveNonPeriodicReservation(final IndividualTicket individualTicket, final HttpServletRequest request, final IndividualTraining individualTraining) throws Exception {
        final Client client = (Client) request.getSession().getAttribute("user");
        if(!validateIndividualReservations(client)){
            throw new MaxOneIndividualTicketException();
        }
        final Reservation reservation = new Reservation();
        final LocalDateTime startDate = individualTraining.getStartDate();
        final LocalDateTime endDate = individualTraining.getEndDate();
        reservation.setEndDate(null);
        reservation.setClient(client);
        reservation.setNumberOfEntriesLeft(individualTicket.getNumberOfEntries());
        reservation.setTicket(individualTicket);
        reservation.setIndividualTrainingGoals(individualTraining.getClientsGoal());
        individualTraining.setTrainingDay(translateToPolish(startDate.getDayOfWeek()));
        individualTraining.setStartHour(formatIndividualTrainingHour(startDate));
        individualTraining.setEndHour(formatIndividualTrainingHour(endDate));
        reservationRepository.save(reservation);
        individualTrainingRepository.save(individualTraining);
    }

    public TraningManagerDto prepareTrainingManagerDto(final Long ticketId){
        final IndividualTicket individualTicket = individualTicketRepository.findById(ticketId).get();
        final TrainingManagerDtoHelper trainingManagerDtoHelper = setTrainingManagerDtoHelper(individualTicket.getSportDiscipline());
        final List<SportDiscipline> sportDisciplines = trainingManagerDtoHelper.getSportDisciplines();
        final List<Coach> coaches = trainingManagerDtoHelper.getCoaches();

        return TraningManagerDto.builder()
                .ticketId(ticketId)
                .sportDisciplines(sportDisciplines)
                .coaches(coaches)
                .numberOfEntries(individualTicket.getNumberOfEntries())
                .build();
    }

    private TrainingManagerDtoHelper setTrainingManagerDtoHelper(final SportDiscipline sportDiscipline){
        final TrainingManagerDtoHelper trainingManagerDtoHelper = TrainingManagerDtoHelper.builder().build();
        switch(sportDiscipline.getSportType()){
            case ALL -> trainingManagerDtoHelper.setSportDisciplines(sportDisciplineRepository.findAll());
            case MARTIAL_ARTS -> trainingManagerDtoHelper.setSportDisciplines(sportDisciplineRepository.getSportDisciplinesBySportTypeIsIn(List.of(SportType.MMA, SportType.BOX, SportType.KICKBOXING)));
            default -> trainingManagerDtoHelper.setSportDisciplines(sportDisciplineRepository.getSportDisciplinesBySportTypeIsIn(List.of(sportDiscipline.getSportType())));
        }
        trainingManagerDtoHelper.setCoaches(coachRepository.getCoachesBySportDisciplineIsIn(trainingManagerDtoHelper.getSportDisciplines()));
        return trainingManagerDtoHelper;
    }

    public IndividualTraining prepareIndividualTraining(final Long ticketId, final Long selectedCoach, final String trainingDate, final String trainingGoal, final Long roomId, final HttpServletRequest request){
        final Client client = (Client) request.getSession().getAttribute("user");
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        final LocalDateTime formattedTrainingDate = LocalDateTime.parse(trainingDate, formatter);
        final SportType ticketsSportType = ticketRepository.getTicketById(ticketId).getSportDiscipline().getSportType();
        final Room room = roomRepository.getReferenceById(roomId);
        final IndividualTraining individualTraining = new IndividualTraining();
        individualTraining.setCoach(coachRepository.getCoachById(selectedCoach));
        individualTraining.setStartDate(formattedTrainingDate);
        individualTraining.setEndDate(formattedTrainingDate.plusHours(1));
        individualTraining.setClientsGoal(trainingGoal);
        individualTraining.setClient(client);
        individualTraining.setRoom(room);
        return individualTraining;
    }

    public List<GroupTicketForm> prepareGroupTicketForm(){
        final List<GroupTicketForm> groupTicketFormList = new ArrayList<>();
        final List<GroupTicket> groupTickets = findAllGroupTickets();
        for(GroupTicket groupTicket : groupTickets) {
            final List<GroupTraining> groupTrainings = groupTrainingRepository.getGroupTrainingsByGroupTicket(groupTicket);
            if (!groupTrainings.isEmpty()) {
                final Coach coach = groupTrainings.get(0).getCoach();
                final Room room = groupTrainings.get(0).getRoom();
                groupTicketFormList.add(GroupTicketForm.builder()
                        .groupTicket(groupTicket)
                        .groupTrainings(groupTrainings)
                        .coach(coach)
                        .room(room).build());
            }
        }
        return groupTicketFormList;
    }

    private TicketType checkTicketsType(final Long ticketId){
        if(individualTicketRepository.existsById(ticketId))
            return TicketType.INDIVIDUAL;
        else if(groupTicketRepository.existsById(ticketId))
            return TicketType.GROUP;
        else return TicketType.GYM;
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

    public Long validateTrainingInfo(final Long selectedCoach, final String trainingDate){
        final Coach coach = coachRepository.getCoachById(selectedCoach);
        final SportType ticketsSportType = coach.getSportDiscipline().getSportType();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        final LocalDateTime formattedTrainingDate = LocalDateTime.parse(trainingDate, formatter);
        if(formattedTrainingDate.isAfter(LocalDate.now().plusMonths(1).atStartOfDay())){
            return -1L;
        }
        final List<Training> trainings = trainingRepository.getTrainingsByCoach(coach);
        final List<LocalDateTime> startTrainingDates = new ArrayList<>();
        final List<LocalDateTime> endTrainingDates = new ArrayList<>();
        for(final Training training : trainings){
            startTrainingDates.addAll(getAllDatesForDayAndTime(training.getTrainingDay(), training.getStartHour()));
            endTrainingDates.addAll(getAllDatesForDayAndTime(training.getTrainingDay(), training.getEndHour()));
        }

        for (int i = 0; i < startTrainingDates.size(); i++) {
            final LocalDateTime startDate = startTrainingDates.get(i);
            final LocalDateTime endDate = endTrainingDates.get(i);

            if (!formattedTrainingDate.isBefore(startDate) && !formattedTrainingDate.isAfter(endDate)) {
                return -2L;
            }
        }
        List<Room> rooms;
        if (SportType.GYM.equals(ticketsSportType)) {
            rooms = roomRepository.getRoomsBySportType(ticketsSportType);
        } else {
            rooms = roomRepository.getRoomsBySportTypeIsIn(List.of(ticketsSportType, SportType.MARTIAL_ARTS));
        }
        for (final Room room : rooms) {
            final List<Training> allTrainings = trainingRepository.getTrainingsByRoom(room);
            Boolean isAvailable = true;
            final List<LocalDateTime> startAllTrainingDates = new ArrayList<>();
            final List<LocalDateTime> endAllTrainingDates = new ArrayList<>();
            for (final Training training : allTrainings) {
                startAllTrainingDates.addAll(getAllDatesForDayAndTime(training.getTrainingDay(), training.getStartHour()));
                endAllTrainingDates.addAll(getAllDatesForDayAndTime(training.getTrainingDay(), training.getEndHour()));
            }
            for (int i = 0; i < startAllTrainingDates.size(); i++) {
                final LocalDateTime startDate = startAllTrainingDates.get(i);
                final LocalDateTime endDate = endAllTrainingDates.get(i);

                if (!formattedTrainingDate.isBefore(startDate) && !formattedTrainingDate.isAfter(endDate)) {
                    isAvailable = false;
                }
            }
            if (isAvailable)
                return room.getRoom_id();
        }
        return -3L;
    }

    public static List<LocalDateTime> getAllDatesForDayAndTime(final String day, final String startHour) {
        final DayOfWeek targetDay = mapDayToDayOfWeek(day);
        final Integer year = LocalDate.now().getYear();

        final LocalTime time = LocalTime.parse(startHour, DateTimeFormatter.ofPattern("HH:mm"));

        final List<LocalDateTime> result = new ArrayList<>();

        final LocalDate start = LocalDate.of(year, LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth());
        final LocalDate end = LocalDate.of(year, LocalDate.now().getMonth().plus(1), LocalDate.now().getDayOfMonth());

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek().equals(targetDay)) {
                result.add(LocalDateTime.of(date, time));
            }
        }

        return result;
    }

    private static DayOfWeek mapDayToDayOfWeek(String day) {
        switch (day) {
            case "poniedziałek":
                return DayOfWeek.MONDAY;
            case "wtorek":
                return DayOfWeek.TUESDAY;
            case "środa":
                return DayOfWeek.WEDNESDAY;
            case "czwartek":
                return DayOfWeek.THURSDAY;
            case "piątek":
                return DayOfWeek.FRIDAY;
            case "sobota":
                return DayOfWeek.SATURDAY;
            case "niedziela":
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("Nieprawidłowy dzień tygodnia: " + day);
        }
    }

}
