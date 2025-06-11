package com.example.inzynier.services;

import com.example.inzynier.models.*;
import com.example.inzynier.models.dto.EditGroupTrainingDto;
import com.example.inzynier.models.dto.EditTicketDto;
import com.example.inzynier.models.dto.TrainingDto;
import com.example.inzynier.models.enums.SportType;
import com.example.inzynier.models.enums.TicketType;
import com.example.inzynier.models.exception.CoachNotFoundException;
import com.example.inzynier.models.exception.GroupTicketNotFoundException;
import com.example.inzynier.models.exception.RoomNotFoundException;
import com.example.inzynier.models.exception.StartHourIsAfterEndHourException;
import com.example.inzynier.models.form.AddTicketForm;
import com.example.inzynier.models.form.CoachForm;
import com.example.inzynier.models.form.TicketForm;
import com.example.inzynier.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.inzynier.services.TicketService.getAllDatesForDayAndTime;

@Service
public class AdminService {

    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private SportDisciplineRepository sportDisciplineRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private GymTicketRepository gymTicketRepository;
    @Autowired
    private GroupTicketRepository groupTicketRepository;
    @Autowired
    private IndividualTicketRepository individualTicketRepository;
    @Autowired
    private GroupTrainingRepository groupTrainingRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private TrainingRepository trainingRepository;
    @Autowired
    private TicketService ticketService;

    public void addCoachToDatabase(final CoachForm coachForm){
        final Coach coach = coachForm.getCoach();
        final SportDiscipline sportDiscipline = sportDisciplineRepository.getSportDisciplineBySportType(coachForm.getSportType());
        coach.setSportDiscipline(sportDiscipline);
        coachRepository.save(coach);
    }

    public void addTicketToDatabase(final AddTicketForm ticketForm) {
        switch (ticketForm.getType()) {
            case GYM:
                GymTicket gymTicket = new GymTicket();
                gymTicket.setName(ticketForm.getName());
                gymTicket.setPrice(ticketForm.getPrice());
                gymTicket.setDescription(ticketForm.getDescription());
                gymTicket.setSportDiscipline(sportDisciplineRepository.getSportDisciplineBySportType(SportType.GYM));
                gymTicket.setOnlyStudent(ticketForm.getStudentsOnly());
                gymTicketRepository.save(gymTicket);
                break;
            case GROUP:
                GroupTicket groupTicket = new GroupTicket();
                groupTicket.setName(ticketForm.getName());
                groupTicket.setPrice(ticketForm.getPrice());
                groupTicket.setDescription(ticketForm.getDescription());
                groupTicket.setSportDiscipline(sportDisciplineRepository.getSportDisciplineBySportType(ticketForm.getSportType()));
                groupTicket.setLevelOfAdvancement(ticketForm.getGroupLevel());
                ticketRepository.save(groupTicket);
                break;
            case INDIVIDUAL:
                IndividualTicket individualTicket = new IndividualTicket();
                individualTicket.setName(ticketForm.getName());
                individualTicket.setPrice(ticketForm.getPrice());
                individualTicket.setDescription(ticketForm.getDescription());
                individualTicket.setSportDiscipline(sportDisciplineRepository.getSportDisciplineBySportType(ticketForm.getSportType()));
                individualTicket.setNumberOfEntries(ticketForm.getEntriesCount());
                ticketRepository.save(individualTicket);
                break;
            default:
                throw new IllegalArgumentException("Nieznany typ karnetu: " + ticketForm.getType());
        }
    }

    public void addGroupTrainingToDatabase(final TrainingDto trainingDTO){
        final GroupTraining training = new GroupTraining();
        training.setTrainingDay(trainingDTO.getDayOfWeek());
        training.setStartHour(trainingDTO.getStartTime());
        training.setEndHour(trainingDTO.getEndTime());
        training.setGroupSize(0);

        if (trainingDTO.getStartTime() != null && trainingDTO.getEndTime() != null) {
            try {
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                final LocalTime startTime = LocalTime.parse(trainingDTO.getStartTime(), formatter);
                final LocalTime endTime = LocalTime.parse(trainingDTO.getEndTime(), formatter);
                if (startTime.isAfter(endTime)) {
                    throw new IllegalArgumentException("Data rozpoczęcia treningu jest późniejsza niż data jego zakończenia");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Wprowadzono błędne godziny treningu");
            }
        }
        training.setCoach(coachRepository.findById(trainingDTO.getCoachId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono trenera o ID: " + trainingDTO.getCoachId())));
        training.setRoom(roomRepository.findById(trainingDTO.getRoom())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sali o ID: " + trainingDTO.getRoom())));
        training.setGroupTicket(groupTicketRepository.findById(trainingDTO.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono karnetu o ID: " + trainingDTO.getTicketId())));

        groupTrainingRepository.save(training);

    }


    public List<TicketForm> setTicketForm() {
        final List<Ticket> tickets = ticketRepository.findAll().stream()
                .sorted(Comparator.comparing(ticket -> ticket.getName().toLowerCase()))
                .toList();

        final List<TicketForm> ticketForms = new ArrayList<>();
        for (final Ticket ticket : tickets) {
            boolean hasTrainings;
            if (TicketType.GROUP.equals(checkTicketsType(ticket))) {
                hasTrainings = groupTrainingRepository.existsByGroupTicket((GroupTicket) ticket);
            } else {
                hasTrainings = true;
            }
            ticketForms.add(
                    TicketForm.builder()
                            .ticket(ticket)
                            .ticketType(checkTicketsType(ticket))
                            .hasTrainings(hasTrainings)
                            .build()
            );
        }
        return ticketForms;
    }

    private TicketType checkTicketsType(final Ticket ticket){
        final Long ticketId = ticket.getId();
        if(individualTicketRepository.existsById(ticketId))
            return TicketType.INDIVIDUAL;
        else if(groupTicketRepository.existsById(ticketId))
            return TicketType.GROUP;
        else return TicketType.GYM;
    }

    public Map<String, Object> getTicketDetailsInfo(final Long id){
        final Ticket ticket = ticketRepository.getTicketById(id);
        final TicketType ticketType = checkTicketsType(ticket);

        final Map<String, Object> response = new HashMap<>();

        response.put("id", ticket.getId());
        response.put("name", ticket.getName());
        response.put("type", ticketType.name());
        response.put("price", ticket.getPrice());
        response.put("description", ticket.getDescription());

        switch (ticketType) {
            case GYM:
                GymTicket gymTicket = gymTicketRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("GymTicket not found"));
                response.put("studentsOnly", gymTicket.isOnlyStudent());
                break;

            case GROUP:
                GroupTicket groupTicket = groupTicketRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("GroupTicket not found"));
                response.put("sportType", groupTicket.getSportDiscipline().getSportType());
                response.put("groupLevel", groupTicket.getLevelOfAdvancement());
                response.put("groupTrainingIds", groupTicket.getGroupTrainings()
                        .stream()
                        .map(GroupTraining::getTraining_id)
                        .collect(Collectors.toList()));
                break;

            case INDIVIDUAL:
                IndividualTicket individualTicket = individualTicketRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("IndividualTicket not found"));
                response.put("sportType", individualTicket.getSportDiscipline().getSportType());
                response.put("entriesCount", individualTicket.getNumberOfEntries());
                break;

            default:
                throw new IllegalArgumentException("Unknown ticket type");
        }
        return response;
    }

    public void deleteTicketFromDatabase(final Long ticketId){
        final TicketType ticketType = checkTicketsType(ticketRepository.getTicketById(ticketId));
        switch (ticketType){
            case GYM -> gymTicketRepository.deleteById(ticketId);
            case GROUP -> groupTicketRepository.deleteById(ticketId);
            case INDIVIDUAL -> individualTicketRepository.deleteById(ticketId);
        }
    }

    public List<Room> getAvailableRooms(final SportType sportType, final String trainingDate){
        List<Room> rooms;
        List<Room> result = new ArrayList<>();
        final List<String> response = Arrays.stream(trainingDate.split("-")).toList();
        final String trainingDay = response.get(0);
        final String startTime = response.get(1);
        final String endTime = response.get(2);
        if (SportType.GYM.equals(sportType)) {
            rooms = roomRepository.getRoomsBySportType(sportType);
        } else {
            rooms = roomRepository.getRoomsBySportTypeIsIn(List.of(sportType, SportType.MARTIAL_ARTS));
        }
        for (final Room room : rooms) {
            final List<Training> allTrainings = trainingRepository.getTrainingsByRoom(room);
            Boolean isAvailable = true;
            for (final Training training : allTrainings) {
                if (trainingDay.equals(training.getTrainingDay())) {
                    if (isOverlapping(startTime, endTime, training.getStartHour(), training.getEndHour())) {
                        isAvailable = false;
                    }
                }
            }
            if (isAvailable)
                result.add(room);
        }
        return result;
    }

    public static boolean isOverlapping(final String start1, final String end1, final String start2, final String end2) {
        final LocalTime s1 = LocalTime.parse(start1);
        final LocalTime e1 = LocalTime.parse(end1);
        final LocalTime s2 = LocalTime.parse(start2);
        final LocalTime e2 = LocalTime.parse(end2);

        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    public void editCoach(final Long coachId, final Coach newCoachInfo){
        final Coach oldCoachInfo = coachRepository.getCoachById(coachId);

        if (newCoachInfo.getName() != null && !newCoachInfo.getName().equals(oldCoachInfo.getName())) {
            oldCoachInfo.setName(newCoachInfo.getName());
        }

        if (newCoachInfo.getLastName() != null && !newCoachInfo.getLastName().equals(oldCoachInfo.getLastName())) {
            oldCoachInfo.setLastName(newCoachInfo.getLastName());
        }

        if (newCoachInfo.getEmail() != null && !newCoachInfo.getEmail().equals(oldCoachInfo.getEmail())) {
            oldCoachInfo.setEmail(newCoachInfo.getEmail());
        }

        if (newCoachInfo.getPhoneNumber() != null && !newCoachInfo.getPhoneNumber().equals(oldCoachInfo.getPhoneNumber())) {
            oldCoachInfo.setPhoneNumber(newCoachInfo.getPhoneNumber());
        }

        if (newCoachInfo.getLogin() != null && !newCoachInfo.getLogin().equals(oldCoachInfo.getLogin())) {
            oldCoachInfo.setLogin(newCoachInfo.getLogin());
        }

        if (newCoachInfo.getYearsOfExperience() != oldCoachInfo.getYearsOfExperience()) {
            oldCoachInfo.setYearsOfExperience(newCoachInfo.getYearsOfExperience());
        }

        if (newCoachInfo.getScholarships() != null && !newCoachInfo.getScholarships().equals(oldCoachInfo.getScholarships())) {
            oldCoachInfo.setScholarships(newCoachInfo.getScholarships());
        }

        coachRepository.save(oldCoachInfo);
    }

    public void editTicket(final Long id, final EditTicketDto dto){
        final Ticket oldTicketInfo = ticketRepository.findById(id).get();

        if (dto.getName() != null && !dto.getName().equals(oldTicketInfo.getName())) {
            oldTicketInfo.setName(dto.getName());
        }

        if (dto.getPrice() != null && !dto.getPrice().equals(oldTicketInfo.getPrice())) {
            oldTicketInfo.setPrice(dto.getPrice());
        }

        if (dto.getDescription() != null && !dto.getDescription().equals(oldTicketInfo.getDescription())) {
            oldTicketInfo.setDescription(dto.getDescription());
        }

        ticketRepository.save(oldTicketInfo);
    }

    public void editTraining(final GroupTraining oldTrainingInfo, final EditGroupTrainingDto newTrainingInfo) throws Exception {
        if (newTrainingInfo.getDay() != null && !newTrainingInfo.getDay().equals(oldTrainingInfo.getTrainingDay())) {
            oldTrainingInfo.setTrainingDay(newTrainingInfo.getDay());
        }

        if (newTrainingInfo.getStartHour() != null && newTrainingInfo.getEndHour() != null) {
            try {
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                final LocalTime startTime = LocalTime.parse(newTrainingInfo.getStartHour(), formatter);
                final LocalTime endTime = LocalTime.parse(newTrainingInfo.getEndHour(), formatter);
                if (startTime.isAfter(endTime)) {
                    throw new StartHourIsAfterEndHourException();
                }
            } catch (DateTimeParseException e) {
                throw new StartHourIsAfterEndHourException();
            }
        }


        if (newTrainingInfo.getStartHour() != null && !newTrainingInfo.getStartHour().equals(oldTrainingInfo.getStartHour())) {
            oldTrainingInfo.setStartHour(newTrainingInfo.getStartHour());
        }

        if (newTrainingInfo.getEndHour() != null && !newTrainingInfo.getEndHour().equals(oldTrainingInfo.getEndHour())) {
            oldTrainingInfo.setEndHour(newTrainingInfo.getEndHour());
        }

        if (newTrainingInfo.getCoach() != null && !newTrainingInfo.getCoach().equals(oldTrainingInfo.getCoach().getName())) {
            final List<String> coachNameAndLastName = List.of(newTrainingInfo.getCoach().split(" "));
            if (coachNameAndLastName.size() != 2) {
                throw new CoachNotFoundException();
            }
            final Coach coach = coachRepository.getCoachByNameAndLastName(coachNameAndLastName.get(0), coachNameAndLastName.get(1));
            if (coach == null) {
                throw new CoachNotFoundException();
            }
            oldTrainingInfo.setCoach(coach);
        }

        if (newTrainingInfo.getRoom() != null && !newTrainingInfo.getRoom().equals(oldTrainingInfo.getRoom().getRoom_id())) {
            final Optional<Room> room = roomRepository.findById(newTrainingInfo.getRoom());
            if (room.isEmpty()) {
                throw new RoomNotFoundException();
            }
            oldTrainingInfo.setRoom(room.get());
        }

        if (newTrainingInfo.getTicket() != null && !newTrainingInfo.getTicket().equals(oldTrainingInfo.getGroupTicket().getName())) {
            final GroupTicket groupTicket = groupTicketRepository.getGroupTicketByName(newTrainingInfo.getTicket());
            if (groupTicket == null) {
                throw new GroupTicketNotFoundException();
            }
            oldTrainingInfo.setGroupTicket(groupTicket);
        }

        groupTrainingRepository.save(oldTrainingInfo);
    }

}
