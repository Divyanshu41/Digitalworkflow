package com.example.digitalapproval.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.digitalapproval.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByNameIgnoreCase(String name);
}
