package com.example.inzynier.controllers;

import com.example.inzynier.models.Client;
import com.example.inzynier.models.dto.MyAccountDto;
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
    public String logIn(final Model model) {
        model.addAttribute("client", new Client());
        return "login";
    }

    @GetMapping("/jak-zaczac")
    public String showHowToStartPage() {
        return "how-to-start";
    }

    @GetMapping("/logout")
    public String logOut(final HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/";
    }

    @GetMapping("/konto")
    public String showMyAccount(final HttpServletRequest request, final Model model) {
        final MyAccountDto myAccountDto = homeService.setMyAccount(request);
        model.addAttribute("dto", myAccountDto);
        return "my_account";
    }

    @PostMapping("/login")
    public String postLogin(final HttpServletRequest request) {
        final String role = (String) request.getSession().getAttribute("role");
        if("user".equals(role)) {
            return "redirect:/";
        } else if ("coach".equals(role)){
            return "redirect:/coach";
        }
        else return "redirect:/admin";
    }

    @GetMapping("/register")
    public String showRegisterForm(final Model model) {
        model.addAttribute("client", new Client());
        return "register";
    }

    @PostMapping("/register")
    public String registerAndSaveNewPersonToDatabase(@ModelAttribute final Client client) {
        homeService.addNewPersonToDatabase(client);
        return "redirect:/";
    }

    @PostMapping("/edycja-profilu")
    public String editProfile(@ModelAttribute final Client client, final HttpServletRequest request) {
        homeService.editProfile(client, request);
        return "redirect:/my_account";
    }

    @GetMapping("/brak-dostepu")
    public String accessDenied(){
        return "access-denied";
    }

}