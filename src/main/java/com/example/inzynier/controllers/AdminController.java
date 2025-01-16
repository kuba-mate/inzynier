package com.example.inzynier.controllers;

import com.example.inzynier.models.*;
import com.example.inzynier.models.dto.TrainingDto;
import com.example.inzynier.models.enums.SportType;
import com.example.inzynier.models.form.AddTicketForm;
import com.example.inzynier.models.form.CoachForm;
import com.example.inzynier.models.form.TicketForm;
import com.example.inzynier.repositories.*;
import com.example.inzynier.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("")
    public String showMainSite(){
        return "admin_index";
    }

    @GetMapping("/trenerzy")
    public String showCoachesSite(final Model model){
        final List<Coach> coaches = coachRepository.findAll().stream().sorted(Comparator.comparingLong(Coach::getId)).toList();
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
    public Map<String, Object> getTicketDetails(@PathVariable Long id) {
        return adminService.getTicketDetailsInfo(id);
    }

    @PostMapping("/dodaj-karnet")
    public ResponseEntity addTicket(@ModelAttribute AddTicketForm addTicketForm){
        adminService.addTicketToDatabase(addTicketForm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/treningi")
    public String getAllTrainings(final Model model){
        final List<GroupTraining> groupTrainings = groupTrainingRepository.findAll();
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

        Map<String, Object> response = new HashMap<>();
        response.put("trainingDay", training.getTrainingDay());
        response.put("startHour", training.getStartHour());
        response.put("endHour", training.getEndHour());
        response.put("coach", Map.of(
                "name", training.getCoach().getName(),
                "lastName", training.getCoach().getLastName()
        ));
        response.put("sportDiscipline", training.getCoach().getSportDiscipline().getName());
        response.put("room", training.getRoom().getRoom_id());
        response.put("ticket", training.getGroupTicket().getId());
        response.put("currentGroupSize", training.getGroupSize());

        return response;
    }

    @PostMapping("/dodaj-trening")
    @ResponseBody
    public ResponseEntity<String> addTraining(@RequestBody TrainingDto trainingDTO) {
        try {
            adminService.addGroupTrainingToDatabase(trainingDTO);
            return ResponseEntity.ok("Trening został dodany.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd: " + e.getMessage());
        }
    }


    @GetMapping("/pobierz-pokoje")
    public ResponseEntity<List<Room>> getRooms(@RequestParam SportType sportType) {
        return ResponseEntity.ok(roomRepository.getRoomsBySportType(sportType));
    }

    @GetMapping("/pobierz-karnet")
    public ResponseEntity<List<GroupTicket>> getTickets(@RequestParam SportType sportType) {
        return ResponseEntity.ok(groupTicketRepository.findGroupTicketsBySportDiscipline_SportType(sportType));
    }

    @GetMapping("/brak-dostepu")
    public String accessDenied(){
        return "access-denied";
    }
}
