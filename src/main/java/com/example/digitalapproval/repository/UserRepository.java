package com.example.digitalapproval.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.digitalapproval.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("select u from User u left join fetch u.role where u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Query("""
        select u.id as id, u.email as email, coalesce(r.name, 'UNKNOWN') as roleName
        from User u
        left join u.role r
        where u.email = :email
        """)
    Optional<UserProfileView> findProfileByEmail(@Param("email") String email);

    interface UserProfileView {
        Long getId();
        String getEmail();
        String getRoleName();
    }
}
