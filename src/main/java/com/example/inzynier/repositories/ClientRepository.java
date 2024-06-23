package com.example.inzynier.repositories;

import com.example.inzynier.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findByLoginAndPassword(final String login, final String password);
    Boolean existsByLogin(final String login);
}
