package com.example.inzynier.controllers;

import com.example.inzynier.models.Ticket;
import com.example.inzynier.services.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/karnety")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping()
    public String getTickets(Model model){
        List<Ticket> tickets = ticketService.findAllTickets();
        model.addAttribute("tickets", tickets);
        return "tickets";
    }

    @GetMapping("/{id}/rezerwacja")
    public String showReservationSite(@PathVariable("id") final Long ticketId, Model model) {
        Ticket selectedTicket = ticketService.getTicketById(ticketId);
        model.addAttribute("selectedTicket", selectedTicket);
        model.addAttribute("validFrom", LocalDate.now());
        model.addAttribute("validTo", LocalDate.now().plusMonths(1));
        return "zakup";
    }

    @PostMapping("/{id}/rezerwacja")
    public String postReservationForm(@RequestParam("ticketId") final Long ticketId, HttpServletRequest request) {
        ticketService.saveReservation(ticketId, request);
        return "redirect:/";
    }

}
