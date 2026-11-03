package com.example.digitalapproval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.entity.User;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByUser(User user);

    List<Request> findByStatus(String status);
}
