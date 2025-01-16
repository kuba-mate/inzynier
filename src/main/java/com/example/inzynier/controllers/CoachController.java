package com.example.inzynier.controllers;

import com.example.inzynier.models.form.TrainingMonitorForm;
import com.example.inzynier.services.CoachService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/coach")
public class CoachController {

    @Autowired
    private CoachService coachService;

    @GetMapping("")
    public String showMainSite(){
        return "coach_index";
    }

    @GetMapping("/moje-treningi")
    public String showCoachTrainings(final Model model, final HttpServletRequest request){
        final List<TrainingMonitorForm> trainings = coachService.setTrainingMonitorForm(request);
        model.addAttribute("trainings", trainings);
        return "coach_trainings";
    }

    @GetMapping("/trening/{id}")
    @ResponseBody
    public Map<String, Object> getCoachDetails(@PathVariable final Long id){
        return coachService.setDetailsMap(id);
    }

    @PostMapping("/trening/{id}/raport")
    public ResponseEntity<String> addRaport(@PathVariable final Long id, @RequestBody Map<String, String> payload){
        final String report = payload.get("report");
        final Boolean saved = coachService.saveRaport(id, report);
        if(saved){
            return ResponseEntity.ok("Raport został zapisany");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Podczas zapisywania wystąpił błąd");
        }
    }

    @GetMapping("/brak-dostepu")
    public String accessDenied(){
        return "access-denied";
    }

}
