package com.hrutvik.marketdata;

import java.time.Instant;
import java.util.Objects;

public record MarketEvent(
        String eventId,
        String symbol,
        long sequence,
        double bid,
        double ask,
        Instant occurredAt
) {
    public MarketEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(symbol);
        Objects.requireNonNull(occurredAt);
        if (eventId.isBlank() || symbol.isBlank()) throw new IllegalArgumentException("eventId and symbol are required");
        if (sequence < 0 || bid <= 0 || ask <= 0 || bid > ask) throw new IllegalArgumentException("invalid event");
    }
}
