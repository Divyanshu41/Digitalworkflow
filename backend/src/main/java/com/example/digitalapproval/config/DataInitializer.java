package com.example.digitalapproval.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.digitalapproval.entity.Role;
import com.example.digitalapproval.service.UserService;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;

    @Value("${app.bootstrap-demo-users:true}")
    private boolean bootstrapDemoUsers;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        if (!bootstrapDemoUsers) {
            return;
        }
        ensureUser("demo-admin", "demo-admin@example.com", "Admin@123", Role.ADMIN);
        ensureUser("demo-approver", "demo-approver@example.com", "Approver@123", Role.APPROVER);
        ensureUser("demo-user", "demo-user@example.com", "User@123", Role.USER);
    }

    private void ensureUser(String username, String email, String password, Role role) {
        userService.ensureUser(username, email, password, role);
        LOGGER.info("Ensured bootstrap user {} ({})", username, role);
    }
}
