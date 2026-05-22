package com.sentinelpay.audit.dto;

import com.sentinelpay.audit.enums.AuditAction;
import com.sentinelpay.audit.enums.AuditResourceType;

import java.util.UUID;

public record AuditEventCommand(
        UUID actorUserId,
        AuditAction action,
        AuditResourceType resourceType,
        String resourceId,
        String details,
        String correlationId,
        String ipAddress,
        String userAgent
) {
}