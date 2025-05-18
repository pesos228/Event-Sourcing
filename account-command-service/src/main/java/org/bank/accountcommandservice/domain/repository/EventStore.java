package org.bank.accountcommandservice.domain.repository;


import org.bank.accountcommandservice.domain.model.EventStream;

import java.util.List;
import java.util.UUID;

public interface EventStore {
    long saveEvents(UUID accountId, List<Object> events);
    EventStream loadEventStream(UUID aggregateId);
    EventStream loadEventStreamAfter(UUID aggregateId, long offset, int version);
    EventStream loadEventStreamUpToVersion(UUID aggregateId, int targetVersionExclusive);
}
