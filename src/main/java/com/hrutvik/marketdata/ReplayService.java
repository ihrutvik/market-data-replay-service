package com.hrutvik.marketdata;

public final class ReplayService {
    private final EventStore eventStore;

    public ReplayService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public MarketSnapshot replay(String symbol, long sequenceInclusive) {
        MarketSnapshot snapshot = null;
        for (MarketEvent event : eventStore.eventsFrom(symbol, sequenceInclusive)) {
            snapshot = MarketDataProcessor.toSnapshot(event);
        }
        return snapshot;
    }
}
