package com.sentinelpay.audit.service;

import com.sentinelpay.audit.dto.AuditEventCommand;

public interface AuditService {

    void recordEvent(AuditEventCommand command);
}