package com.example.digitalapproval.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.digitalapproval.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
