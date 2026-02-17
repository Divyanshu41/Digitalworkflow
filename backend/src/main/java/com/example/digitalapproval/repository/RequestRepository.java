package com.example.digitalapproval.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.entity.User;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findByUser(User user, Pageable pageable);

    Page<Request> findByApprover(User approver, Pageable pageable);

    List<Request> findByStatus(Request.Status status);

    Page<Request> findAll(Pageable pageable);

    long countByStatus(Request.Status status);
}