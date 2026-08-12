package com.hrutvik.marketdata;

import java.util.List;

public interface EventStore {
    void append(MarketEvent event);
    List<MarketEvent> eventsFrom(String symbol, long sequenceInclusive);
    List<MarketEvent> all(String symbol);
}
