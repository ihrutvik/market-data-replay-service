package com.hrutvik.marketdata;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public final class InMemorySnapshotStore implements SnapshotStore {
    private final ConcurrentMap<String, AtomicReference<MarketSnapshot>> snapshots = new ConcurrentHashMap<>();

    @Override
    public void put(MarketSnapshot snapshot) {
        snapshots.computeIfAbsent(snapshot.symbol(), key -> new AtomicReference<>()).set(snapshot);
    }

    @Override
    public MarketSnapshot get(String symbol) {
        AtomicReference<MarketSnapshot> reference = snapshots.get(symbol);
        return reference == null ? null : reference.get();
    }
}
