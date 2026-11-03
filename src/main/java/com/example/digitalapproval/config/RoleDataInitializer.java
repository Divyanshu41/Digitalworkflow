package com.example.digitalapproval.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.digitalapproval.entity.Role;
import com.example.digitalapproval.repository.RoleRepository;

@Configuration
public class RoleDataInitializer {

    @Bean
    public CommandLineRunner seedDefaultRoles(RoleRepository roleRepository) {
        return args -> {
            createRoleIfMissing(roleRepository, "USER", "Default user role");
            createRoleIfMissing(roleRepository, "APPROVER", "Approver role");
            createRoleIfMissing(roleRepository, "ADMIN", "Administrator role");
        };
    }

    private void createRoleIfMissing(RoleRepository roleRepository, String roleName, String description) {
        roleRepository.findByNameIgnoreCase(roleName)
            .orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(description);
                return roleRepository.save(role);
            });
    }
}
