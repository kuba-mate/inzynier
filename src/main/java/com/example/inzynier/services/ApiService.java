package com.example.inzynier.services;

import com.example.inzynier.models.Client;
import com.example.inzynier.repositories.ClientRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

    @Autowired
    private ClientRepository clientRepository;

    public ResponseEntity<Boolean> validateLogin(final String login, final String password, HttpServletRequest request) {
        final Client user = clientRepository.findByLoginAndPassword(login, password);
        if(user == null)
            return ResponseEntity.ok(false);

        request.getSession().setAttribute("user", user);
        request.getSession().setAttribute("role", "user");
        return ResponseEntity.ok(true);
    }

    public Boolean checkLogin(String login) {
        return clientRepository.existsByLogin(login);
    }

}