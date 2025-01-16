package com.example.inzynier.services;

import com.example.inzynier.models.*;
import com.example.inzynier.models.dto.TrainingDto;
import com.example.inzynier.models.enums.SportType;
import com.example.inzynier.models.enums.TicketType;
import com.example.inzynier.models.form.AddTicketForm;
import com.example.inzynier.models.form.CoachForm;
import com.example.inzynier.models.form.TicketForm;
import com.example.inzynier.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

        training.setCoach(coachRepository.findById(trainingDTO.getCoachId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono trenera o ID: " + trainingDTO.getCoachId())));
        training.setRoom(roomRepository.findById(trainingDTO.getRoom())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sali o ID: " + trainingDTO.getRoom())));
        training.setGroupTicket(groupTicketRepository.findById(trainingDTO.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono karnetu o ID: " + trainingDTO.getTicketId())));

        groupTrainingRepository.save(training);

    }


    public List<TicketForm> setTicketForm(){
        final List<Ticket> tickets = ticketRepository.findAll().stream().sorted(Comparator.comparingLong(Ticket::getId)).toList();
        final List<TicketForm> ticketForms = new ArrayList<>();
        for(final Ticket ticket : tickets){
            ticketForms.add(
                    TicketForm.builder()
                            .ticket(ticket)
                            .ticketType(checkTicketsType(ticket))
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

}
