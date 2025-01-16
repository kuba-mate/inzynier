package com.example.inzynier.repositories;

import com.example.inzynier.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin,Long> {

    Admin findByLoginAndPassword(final String login, final String password);
}
