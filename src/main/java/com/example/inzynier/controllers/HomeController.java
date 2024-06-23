package com.example.inzynier.controllers;

import com.example.inzynier.models.Client;
import com.example.inzynier.services.HomeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/")
    public String showMainSite(){
        return "index";
    }

    @GetMapping("/login")
    public String logIn(Model model) {
        model.addAttribute("client", new Client());
        return "login";
    }

    @GetMapping("/logout")
    public String logOut(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/";
    }

    @PostMapping("/login")
    public String postLogin() {
        return "redirect:/";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("client", new Client());
        return "register";
    }

    @PostMapping("/register")
    public String registerAndSaveNewPersonToDatabase(@ModelAttribute Client client) {
        homeService.addNewPersonToDatabase(client);
        return "redirect:/";
    }

}