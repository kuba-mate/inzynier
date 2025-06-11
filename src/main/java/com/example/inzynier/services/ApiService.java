package com.example.inzynier.services;

import com.example.inzynier.models.Admin;
import com.example.inzynier.models.Client;
import com.example.inzynier.models.Coach;
import com.example.inzynier.models.Person;
import com.example.inzynier.models.exception.LoginNotUniqueException;
import com.example.inzynier.models.exception.WrongEmailException;
import com.example.inzynier.models.exception.WrongLoginPasswordException;
import com.example.inzynier.models.exception.WrongPhoneNumberException;
import com.example.inzynier.repositories.AdminRepository;
import com.example.inzynier.repositories.ClientRepository;
import com.example.inzynier.repositories.CoachRepository;
import com.example.inzynier.repositories.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private PersonRepository personRepository;

    public ResponseEntity<Boolean> validateLogin(final String login, final String password, HttpServletRequest request) {
        final Boolean isClient = validateClient(login, password, request);
        final Boolean isAdmin = validateAdmin(login, password, request);
        final Boolean isCoach = validateCoach(login, password, request);
        return ResponseEntity.ok(isAdmin || isClient || isCoach);
    }

    public void checkLogin(final Client newClientInfo) throws Exception {
        if (newClientInfo.getLogin() == null || newClientInfo.getLogin().length() < 3) {
            throw new WrongLoginPasswordException();
        }
        final List<Person> persons = personRepository.findAll();
        for (final Person person : persons) {
            if (person.getLogin().equals(newClientInfo.getLogin())) {
                throw new LoginNotUniqueException();
            }
        }
        if (newClientInfo.getPassword() == null || newClientInfo.getPassword().length() < 3) {
            throw new WrongLoginPasswordException();
        }
        final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        final String email = newClientInfo.getEmail();
        if (email == null || !email.matches(emailRegex)) {
            throw new WrongEmailException();
        }
        final String phoneNumber = newClientInfo.getPhoneNumber();
        final String phoneRegex = "^\\+?[0-9]{9,15}$";
        if (phoneNumber == null || !phoneNumber.matches(phoneRegex)) {
            throw new WrongPhoneNumberException();
        }
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