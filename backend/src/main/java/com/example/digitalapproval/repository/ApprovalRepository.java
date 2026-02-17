package com.example.digitalapproval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.digitalapproval.entity.Approval;
import com.example.digitalapproval.entity.Request;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByRequest(Request request);
}