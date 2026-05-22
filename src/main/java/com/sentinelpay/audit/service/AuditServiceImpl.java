package com.sentinelpay.audit.service;

import com.sentinelpay.audit.entity.AuditEvent;
import com.sentinelpay.audit.enums.AuditAction;
import com.sentinelpay.audit.enums.AuditResourceType;
import com.sentinelpay.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    @Override
    public void recordEvent(
            UUID actorUserId,
            AuditAction action,
            AuditResourceType resourceType,
            String resourceId,
            String details,
            String correlationId,
            String ipAddress,
            String userAgent
    ) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setActorUserId(actorUserId);
        auditEvent.setAction(action);
        auditEvent.setResourceType(resourceType);
        auditEvent.setResourceId(resourceId);
        auditEvent.setDetails(details);
        auditEvent.setCorrelationId(correlationId);
        auditEvent.setIpAddress(ipAddress);
        auditEvent.setUserAgent(userAgent);

        auditEventRepository.save(auditEvent);

        log.info(
                "Audit event recorded action={} resourceType={} resourceId={} actorUserId={}",
                action,
                resourceType,
                resourceId,
                actorUserId
        );
    }
}