package com.example.inzynier.services;

import com.example.inzynier.models.Client;
import com.example.inzynier.models.Client;
import com.example.inzynier.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    @Autowired
    private ClientRepository clientRepository;
    public void addNewPersonToDatabase(Client client) {
        clientRepository.save(client);
    }

}