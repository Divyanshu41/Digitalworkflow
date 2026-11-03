package com.example.digitalapproval.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Unauthorized\"}");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        DaoAuthenticationProvider authenticationProvider,
        AuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/dashboard.html", "/style.css", "/script.js", "/css/**", "/js/**", "/error", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/requests").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/requests").hasAnyRole("USER", "APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/requests/pending").hasAnyRole("APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/requests/*/approve").hasRole("APPROVER")
                .requestMatchers(HttpMethod.PUT, "/requests/*/reject").hasRole("APPROVER")
                .anyRequest().hasRole("ADMIN"))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
            .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(authenticationEntryPoint));
        return http.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
