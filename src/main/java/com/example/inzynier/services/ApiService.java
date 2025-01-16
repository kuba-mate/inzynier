package com.example.inzynier.services;

import com.example.inzynier.models.Admin;
import com.example.inzynier.models.Client;
import com.example.inzynier.models.Coach;
import com.example.inzynier.repositories.AdminRepository;
import com.example.inzynier.repositories.ClientRepository;
import com.example.inzynier.repositories.CoachRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private CoachRepository coachRepository;

    public ResponseEntity<Boolean> validateLogin(final String login, final String password, HttpServletRequest request) {
        final Boolean isClient = validateClient(login, password, request);
        final Boolean isAdmin = validateAdmin(login, password, request);
        final Boolean isCoach = validateCoach(login, password, request);
        return ResponseEntity.ok(isAdmin || isClient || isCoach);
    }

    public Boolean checkLogin(String login) {
        return clientRepository.existsByLogin(login);
    }

    public Boolean validateClient(final String login, final String password, HttpServletRequest request){
        final Client user = clientRepository.findByLoginAndPassword(login, password);
        if(user == null)
            return false;

        request.getSession().setAttribute("user", user);
        request.getSession().setAttribute("role", "user");
        return true;
    }

    public Boolean validateAdmin(final String login, final String password, HttpServletRequest request){
        final Admin admin = adminRepository.findByLoginAndPassword(login, password);
        if(admin == null)
            return false;

        request.getSession().setAttribute("user", admin);
        request.getSession().setAttribute("role", "administrator");
        return true;
    }

    public Boolean validateCoach(final String login, final String password, HttpServletRequest request){
        final Coach coach = coachRepository.findByLoginAndPassword(login, password);
        if(coach == null)
            return false;

        request.getSession().setAttribute("user", coach);
        request.getSession().setAttribute("role", "coach");
        return true;
    }

}