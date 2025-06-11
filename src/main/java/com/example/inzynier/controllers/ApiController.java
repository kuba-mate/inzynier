package com.example.inzynier.controllers;

import com.example.inzynier.models.Client;
import com.example.inzynier.models.exception.WrongEmailException;
import com.example.inzynier.models.exception.WrongLoginPasswordException;
import com.example.inzynier.models.exception.WrongPhoneNumberException;
import com.example.inzynier.services.ApiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @PostMapping("/checkLogin")
    public ResponseEntity<String> checkLogin(@ModelAttribute final Client client) {
        try {
            apiService.checkLogin(client);
        } catch (WrongLoginPasswordException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login lub hasło są zbyt krótkie");
        } catch (WrongEmailException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Adres email nie spełnia wymaganego formatu");
        } catch (WrongPhoneNumberException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Numer telefonu nie spełnia wymaganego formatu");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Użytkownik z takim loginem już istnieje");
        }
        return ResponseEntity.ok("redirect:/my_account");
    }

    @PostMapping("/validateLogin")
    public ResponseEntity<Boolean> validateLogin(@RequestParam final String login, @RequestParam final String haslo, final HttpServletRequest request) {
        return apiService.validateLogin(login, haslo, request);
    }

}