package org.bank.accountcommandservice.domain.model;

import java.util.List;

public record EventStream(
        List<Object> events,
        int lastReadPartition,
        long lastReadOffset,
        int version
) {
    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }
}
