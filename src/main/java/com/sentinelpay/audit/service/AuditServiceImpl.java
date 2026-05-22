package com.sentinelpay.audit.service;

import com.sentinelpay.audit.dto.AuditEventCommand;
import com.sentinelpay.audit.entity.AuditEvent;
import com.sentinelpay.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    @Override
    public void recordEvent(AuditEventCommand command) {
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setActorUserId(command.actorUserId());
        auditEvent.setAction(command.action());
        auditEvent.setResourceType(command.resourceType());
        auditEvent.setResourceId(command.resourceId());
        auditEvent.setDetails(command.details());
        auditEvent.setCorrelationId(command.correlationId());
        auditEvent.setIpAddress(command.ipAddress());
        auditEvent.setUserAgent(command.userAgent());

        auditEventRepository.save(auditEvent);

        log.info(
                "Audit event recorded action={} resourceType={} resourceId={} actorUserId={} correlationId={}",
                command.action(),
                command.resourceType(),
                command.resourceId(),
                command.actorUserId(),
                command.correlationId()
        );
    }
}