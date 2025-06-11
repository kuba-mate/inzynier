package com.example.inzynier.controllers;

import com.example.inzynier.models.*;
import com.example.inzynier.models.dto.EditGroupTrainingDto;
import com.example.inzynier.models.dto.EditTicketDto;
import com.example.inzynier.models.dto.GroupTicketNamePriceDto;
import com.example.inzynier.models.dto.TrainingDto;
import com.example.inzynier.models.enums.SportType;
import com.example.inzynier.models.exception.CoachNotFoundException;
import com.example.inzynier.models.exception.GroupTicketNotFoundException;
import com.example.inzynier.models.exception.RoomNotFoundException;
import com.example.inzynier.models.exception.StartHourIsAfterEndHourException;
import com.example.inzynier.models.form.AddTicketForm;
import com.example.inzynier.models.form.CoachForm;
import com.example.inzynier.models.form.TicketForm;
import com.example.inzynier.repositories.*;
import com.example.inzynier.services.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private AdminService adminService;
    @Autowired
    private GroupTrainingRepository groupTrainingRepository;
    @Autowired
    private GroupTicketRepository groupTicketRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SportDisciplineRepository sportDisciplineRepository;

    private static final Map<String, Integer> DAY_ORDER = Map.of(
            "poniedziałek", 1,
            "wtorek", 2,
            "środa", 3,
            "czwartek", 4,
            "piątek", 5,
            "sobota", 6,
            "niedziela", 7
    );


    @GetMapping("")
    public String showMainSite(){
        return "admin_index";
    }

    @GetMapping("/trenerzy")
    public String showCoachesSite(final Model model){
        final List<Coach> coaches = coachRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Coach coach) -> coach.getName().toLowerCase())
                        .thenComparing(coach -> coach.getLastName().toLowerCase()))
                .toList();
        final List<SportType> sportDisciplines = List.of(SportType.GYM, SportType.MMA, SportType.BOX, SportType.KICKBOXING);
        model.addAttribute("coaches", coaches);
        model.addAttribute("sportDisciplines", sportDisciplines);
        return "admin_coaches";
    }

    @PostMapping("/dodaj-trenera")
    public void addCoach(@ModelAttribute final CoachForm coachForm) {
        adminService.addCoachToDatabase(coachForm);
    }

    @GetMapping("/trener/{id}")
    @ResponseBody
    public Map<String, Object> getCoachDetails(@PathVariable Long id) {
        final Coach coach = coachRepository.findById(id).get();

        final Map<String, Object> response = new HashMap<>();
        response.put("name", coach.getName());
        response.put("lastName", coach.getLastName());
        response.put("birthDate", coach.getBirthDate());
        response.put("email", coach.getEmail());
        response.put("phoneNumber", coach.getPhoneNumber());
        response.put("login", coach.getLogin());
        response.put("yearsOfExperience", coach.getYearsOfExperience());
        response.put("scholarships", coach.getScholarships());
        response.put("sportDiscipline", coach.getSportDiscipline().getName());

        return response;
    }

    @DeleteMapping("/trener/{id}")
    public String deleteCoach(@PathVariable final Long id){
        coachRepository.deleteById(id);
        return "admin_coaches";
    }

    @PostMapping("/karnet/{id}")
    public ResponseEntity<String> editTicket(@PathVariable final Long id, @ModelAttribute final EditTicketDto dto){
        adminService.editTicket(id, dto);
        return ResponseEntity.ok("admin_coaches");
    }

    @PostMapping("/trener/{id}")
    public ResponseEntity<String> editCoach(@PathVariable final Long id, @ModelAttribute final Coach coach){
        adminService.editCoach(id, coach);
        return ResponseEntity.ok("admin_coaches");
    }

    @PostMapping("/trening/{id}")
    public ResponseEntity<String> editTraining(@PathVariable final Long id, @ModelAttribute final EditGroupTrainingDto groupTraining){
        final GroupTraining training = groupTrainingRepository.findById(id).orElseThrow(() -> new RuntimeException("Trening nie znaleziony"));
        try {
            adminService.editTraining(training, groupTraining);
            return ResponseEntity.ok("redirect:/admin_group_training");
        } catch (StartHourIsAfterEndHourException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wprowadzono błędne daty początku i końca treningu");
        } catch (CoachNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wprowadzono błędnego trenera");
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wprowadzono błędną sale");
        } catch (GroupTicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wprowadzono błedną nazwę karnetu");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd");
        }
    }

    @DeleteMapping("/karnet/{id}")
    public String deleteTicket(@PathVariable final Long id){
        adminService.deleteTicketFromDatabase(id);
        return "admin_coaches";
    }

    @GetMapping("/karnet-info")
    public String getAllTickets(final Model model) {
        final List<TicketForm> forms = adminService.setTicketForm();
        model.addAttribute("forms", forms);
        return "admin_tickets";
    }

    @GetMapping("/karnet/{id}")
    @ResponseBody
    public Map<String, Object> getTicketDetails(@PathVariable final Long id) {
        return adminService.getTicketDetailsInfo(id);
    }

    @PostMapping("/dodaj-karnet")
    public ResponseEntity addTicket(@ModelAttribute final AddTicketForm addTicketForm){
        adminService.addTicketToDatabase(addTicketForm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/treningi")
    public String getAllTrainings(final Model model) {
        final List<GroupTraining> groupTrainings = groupTrainingRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((GroupTraining training) -> DAY_ORDER.get(training.getTrainingDay()))
                        .thenComparing(GroupTraining::getStartHour))
                .toList();

        final List<Coach> coaches = coachRepository.findAll();
        model.addAttribute("trainings", groupTrainings);
        model.addAttribute("coaches", coaches);
        return "admin_group_training";
    }


    @DeleteMapping("/trening/{id}")
    public String deleteTraining(@PathVariable Long id) {
        groupTrainingRepository.deleteById(id);
        return "admin_group_training";
    }

    @GetMapping("/trening/{id}")
    @ResponseBody
    public Map<String, Object> getTrainingDetails(@PathVariable Long id) {
        final GroupTraining training = groupTrainingRepository.findById(id).orElseThrow(() -> new RuntimeException("Trening nie znaleziony"));

        final Map<String, Object> response = new HashMap<>();
        response.put("trainingDay", training.getTrainingDay());
        response.put("startHour", training.getStartHour());
        response.put("endHour", training.getEndHour());
        response.put("coach", Map.of(
                "name", training.getCoach().getName(),
                "lastName", training.getCoach().getLastName()
        ));
        response.put("sportDiscipline", training.getCoach().getSportDiscipline().getName());
        response.put("sportType", training.getCoach().getSportDiscipline().getSportType());
        response.put("room", training.getRoom().getRoom_id());
        response.put("ticket", training.getGroupTicket().getName());
        response.put("currentGroupSize", training.getGroupSize());

        return response;
    }

    @PostMapping("/dodaj-trening")
    @ResponseBody
    public ResponseEntity<String> addTraining(@RequestBody final TrainingDto trainingDTO) {
        try {
            adminService.addGroupTrainingToDatabase(trainingDTO);
            return ResponseEntity.ok("Trening został dodany.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/pobierz-pokoje")
    public ResponseEntity<List<Room>> getRooms(@RequestParam final SportType sportType, @RequestParam final String trainingDate) {
        final List<Room> rooms = adminService.getAvailableRooms(sportType, trainingDate);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/pobierz-karnet")
    public ResponseEntity<List<GroupTicketNamePriceDto>> getTickets(@RequestParam final SportType sportType) {
        final List<GroupTicket> tickets = groupTicketRepository.findGroupTicketsBySportDiscipline_SportType(sportType);
        final List<GroupTicketNamePriceDto> result = new ArrayList<>();
        tickets.forEach(ticket -> result.add(GroupTicketNamePriceDto.builder()
                        .id(ticket.getId())
                        .name(ticket.getName())
                        .price(ticket.getPrice())
                        .build()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pobierz-trenerow")
    @ResponseBody
    public ResponseEntity<List<Coach>> getRooms(@RequestParam final SportType sportType,
                                                @RequestParam final String coachName, @RequestParam final String coachLastName) {
        final SportDiscipline sportDiscipline = sportDisciplineRepository.getSportDisciplineBySportType(sportType);
        final List<Coach> coaches = coachRepository.getCoachesBySportDisciplineIsIn(List.of(sportDiscipline));
        coaches.removeIf(coach ->
                coach.getName().equalsIgnoreCase(coachName) &&
                        coach.getLastName().equalsIgnoreCase(coachLastName)
        );
        return ResponseEntity.ok(coaches);
    }

    @GetMapping("/brak-dostepu")
    public String accessDenied(){
        return "access-denied";
    }
}
