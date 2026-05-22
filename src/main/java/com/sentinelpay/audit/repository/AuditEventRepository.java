package com.sentinelpay.audit.repository;

import com.sentinelpay.audit.entity.AuditEvent;
import com.sentinelpay.audit.enums.AuditAction;
import com.sentinelpay.audit.enums.AuditResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    List<AuditEvent> findByActorUserId(UUID actorUserId);

    List<AuditEvent> findByAction(AuditAction action);

    List<AuditEvent> findByResourceTypeAndResourceId(
            AuditResourceType resourceType,
            String resourceId
    );

    List<AuditEvent> findByCorrelationId(String correlationId);
}