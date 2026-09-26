package com.bloodconnect.service;

import com.bloodconnect.domain.entity.AuditLog;
import com.bloodconnect.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Transactional
    public void logAction(String action, String entityType, Long entityId, String oldValues, String newValues, String status) {
        logAction(action, entityType, entityId, oldValues, newValues, status, null);
    }
    
    @Transactional
    public void logAction(String action, String entityType, Long entityId, String oldValues, String newValues, String status, String errorMessage) {
        AuditLog log = AuditLog.builder()
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .oldValues(oldValues)
            .newValues(newValues)
            .status(AuditLog.Status.valueOf(status))
            .errorMessage(errorMessage)
            .build();
        
        auditLogRepository.save(log);
    }
}
