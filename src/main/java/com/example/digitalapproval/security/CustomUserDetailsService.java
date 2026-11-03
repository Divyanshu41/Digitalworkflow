package com.example.digitalapproval.security;

import java.util.List;
import java.util.Locale;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.digitalapproval.entity.Role;
import com.example.digitalapproval.entity.User;
import com.example.digitalapproval.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User appUser = userRepository.findByEmailWithRole(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        Role role = appUser.getRole();
        String roleName = role != null ? role.getName() : null;
        List<SimpleGrantedAuthority> authorities = roleName == null
            ? List.of()
            : List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase(Locale.ROOT)));

        return org.springframework.security.core.userdetails.User.builder()
            .username(appUser.getEmail())
            .password(appUser.getPasswordHash())
            .authorities(authorities)
            .build();
    }
}
