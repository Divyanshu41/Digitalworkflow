package com.example.digitalapproval.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.digitalapproval.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
