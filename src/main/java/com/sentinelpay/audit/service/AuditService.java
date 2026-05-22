package com.sentinelpay.audit.service;

import com.sentinelpay.audit.enums.AuditAction;
import com.sentinelpay.audit.enums.AuditResourceType;

import java.util.UUID;

public interface AuditService {

    void recordEvent(
            UUID actorUserId,
            AuditAction action,
            AuditResourceType resourceType,
            String resourceId,
            String details,
            String correlationId,
            String ipAddress,
            String userAgent
    );
}