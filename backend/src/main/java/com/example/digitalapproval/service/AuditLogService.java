package com.example.digitalapproval.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.digitalapproval.dto.AuditLogDTO;
import com.example.digitalapproval.entity.AuditLog;
import com.example.digitalapproval.repository.AuditLogRepository;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogService.class);

    public void recordAction(Long actorId, String action, String details) {
        try {
            auditLogRepository.save(new AuditLog(actorId, action, details));
        } catch (Exception ex) {
            LOGGER.warn("Failed to persist audit log for action {}: {}", action, ex.getMessage());
        }
    }

    public Page<AuditLogDTO> getRecentLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable).map(AuditLogDTO::new);
    }
}
