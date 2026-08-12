package com.hrutvik.marketdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryEventStore implements EventStore {
    private final ConcurrentMap<String, List<MarketEvent>> events = new ConcurrentHashMap<>();

    @Override
    public void append(MarketEvent event) {
        events.computeIfAbsent(event.symbol(), key -> java.util.Collections.synchronizedList(new ArrayList<>()))
              .add(event);
    }

    @Override
    public List<MarketEvent> eventsFrom(String symbol, long sequenceInclusive) {
        return all(symbol).stream()
                .filter(e -> e.sequence() >= sequenceInclusive)
                .sorted(Comparator.comparingLong(MarketEvent::sequence))
                .toList();
    }

    @Override
    public List<MarketEvent> all(String symbol) {
        List<MarketEvent> source = events.get(symbol);
        if (source == null) return List.of();
        synchronized (source) {
            return source.stream()
                    .sorted(Comparator.comparingLong(MarketEvent::sequence))
                    .toList();
        }
    }
}
