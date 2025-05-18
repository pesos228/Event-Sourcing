package org.bank.accountcommandservice.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record BaseEvent(
        UUID eventUUID,
        UUID accountId,
        LocalDateTime timestamp,
        int aggregateVersion
) {
    public static BaseEvent newBaseEvent(UUID accountId, int aggregateVersion) {
        return new BaseEvent(UUID.randomUUID(), accountId, LocalDateTime.now(), aggregateVersion);
    }
}
