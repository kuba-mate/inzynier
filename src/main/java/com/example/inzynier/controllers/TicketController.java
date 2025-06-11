package com.example.inzynier.controllers;

import com.example.inzynier.models.*;
import com.example.inzynier.models.dto.GroupTicketForm;
import com.example.inzynier.models.dto.TraningManagerDto;
import com.example.inzynier.models.exception.GroupTicketAkreadyTakenException;
import com.example.inzynier.models.exception.MaxOneGymTicketException;
import com.example.inzynier.models.exception.MaxOneIndividualTicketException;
import com.example.inzynier.models.exception.NotAStudentException;
import com.example.inzynier.repositories.IndividualTicketRepository;
import com.example.inzynier.services.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/karnety")
public class TicketController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private IndividualTicketRepository individualTicketRepository;

    @GetMapping()
    public String getTickets(final Model model){
        final List<Ticket> tickets = ticketService.findAllTickets();
        model.addAttribute("tickets", tickets);
        return "tickets";
    }

    @GetMapping("/{id}/rezerwacja")
    public String showReservationSite(@PathVariable("id") final Long ticketId, final Model model,
                                      @RequestParam(name = "selectedCoach", required = false) final Long selectedCoach,
                                      @RequestParam(name = "trainingDate", required = false) final String trainingDate,
                                      @RequestParam(name = "trainingGoal", required = false) final String trainingGoal) {
        final Ticket selectedTicket = ticketService.getTicketById(ticketId);
        final Optional<IndividualTicket> individualTicket = individualTicketRepository.findById(ticketId);
        if(individualTicket.isPresent()){
            model.addAttribute("validTo", "-");
            model.addAttribute("selectedCoach", selectedCoach);
            model.addAttribute("trainingDate", trainingDate);
            model.addAttribute("trainingGoal", trainingGoal);
        } else {
            model.addAttribute("validTo", LocalDate.now().plusMonths(1));
            model.addAttribute("selectedCoach", null);
            model.addAttribute("trainingDate", null);
            model.addAttribute("trainingGoal", null);
        }
        model.addAttribute("selectedTicket", selectedTicket);
        model.addAttribute("validFrom", LocalDate.now());
        return "zakup";
    }

    @GetMapping("/silownia")
    public String getGymTickets(final Model model){
        final List<GymTicket> gymTickets = ticketService.findAllGymTickets();
        model.addAttribute("tickets", gymTickets);
        return "gym_tickets";
    }

    @GetMapping("/grupowe")
    public String getGroupTickets(final Model model){
        final List<GroupTicketForm> groupTicketForm = ticketService.prepareGroupTicketForm();
        model.addAttribute("tickets", groupTicketForm);
        return "group_tickets";
    }

    @GetMapping("/indywidualne")
    public String getIndividualTickets(final Model model){
        final List<IndividualTicket> individualTickets = ticketService.findAllIndividualTickets();
        model.addAttribute("tickets", individualTickets);
        return "individual_tickets";
    }

    @GetMapping("/indywidualne/{id}")
    public String getIndividualTicketById(@PathVariable("id") final Long ticketId, final Model model){
        final TraningManagerDto traningManagerDto = ticketService.prepareTrainingManagerDto(ticketId);
        model.addAttribute("dto", traningManagerDto);
        return "individual_ticket_id";
    }

    @PostMapping("/{id}/rezerwacja")
    public ResponseEntity<String> postReservationForm(@RequestParam("ticketId") final Long ticketId,
                                              @RequestParam(name = "selectedCoach", required = false) final Long selectedCoach,
                                              @RequestParam(name = "trainingDate", required = false) final String trainingDate,
                                              @RequestParam(name = "trainingGoal", required = false) final String trainingGoal,
                                              @RequestParam(name = "hiddenAvailable", required = false) final Long availableRoomId,
                                              final HttpServletRequest request) {
        if(selectedCoach == null && trainingDate.isEmpty() && trainingGoal.isEmpty()){
            try {
                ticketService.saveReservation(ticketId, request, null);
            } catch (MaxOneGymTicketException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Można mieć maksymalnie 1 aktywny karnet na siłownie");
            } catch (GroupTicketAkreadyTakenException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nie można mieć dwóch takich samych aktywnych karnetów");
            } catch (NotAStudentException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Aby zarezerwować ten karnet potrzebny jest status studenta");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd");
            }
            return ResponseEntity.ok("redirect:/");
        } else {
            final IndividualTraining individualTraining = ticketService.prepareIndividualTraining(ticketId, selectedCoach, trainingDate, trainingGoal, availableRoomId, request);
            try {
                ticketService.saveReservation(ticketId, request, individualTraining);
            } catch (MaxOneIndividualTicketException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Można mieć maksymalnie 1 aktywną indywidualną rezerwacje");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd");
            }
            return ResponseEntity.ok("redirect:/");
        }
    }

    @GetMapping("/walidacja")
    public ResponseEntity<Map<String, Object>> validateTraining(
            @RequestParam("selectedCoach") final Long selectedCoach,
            @RequestParam("trainingDate") final String trainingDate) {

        Long isAvailable = validateTrainingInfo(selectedCoach, trainingDate);

        final Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    private Long validateTrainingInfo(final Long selectedCoach, final String trainingDate){
        return ticketService.validateTrainingInfo(selectedCoach, trainingDate);
    }

}
