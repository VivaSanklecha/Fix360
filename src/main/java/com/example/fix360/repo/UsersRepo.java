package com.example.fix360.repo;

import com.example.fix360.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepo extends JpaRepository<Users, Integer> {
    Users findByEmail(String email);
}