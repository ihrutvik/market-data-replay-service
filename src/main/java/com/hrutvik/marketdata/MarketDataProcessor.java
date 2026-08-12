package com.hrutvik.marketdata;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.*;

public final class MarketDataProcessor implements AutoCloseable {
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final ConcurrentMap<String, ExecutorService> executors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> processedIds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> lastSequence = new ConcurrentHashMap<>();
    private final ProcessingMetrics metrics = new ProcessingMetrics();

    public MarketDataProcessor(EventStore eventStore, SnapshotStore snapshotStore) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
    }

    public CompletableFuture<Boolean> submit(MarketEvent event) {
        ExecutorService executor = executors.computeIfAbsent(event.symbol(), symbol ->
                Executors.newSingleThreadExecutor(Thread.ofVirtual().name("market-" + symbol + "-", 0).factory()));
        return CompletableFuture.supplyAsync(() -> process(event), executor);
    }

    private boolean process(MarketEvent event) {
        long start = System.nanoTime();
        Set<String> ids = processedIds.computeIfAbsent(event.symbol(), key -> ConcurrentHashMap.newKeySet());
        if (!ids.add(event.eventId())) {
            metrics.duplicate();
            return false;
        }

        long previous = lastSequence.getOrDefault(event.symbol(), -1L);
        if (event.sequence() <= previous) {
            metrics.stale();
            return false;
        }

        eventStore.append(event);
        lastSequence.put(event.symbol(), event.sequence());
        MarketSnapshot snapshot = toSnapshot(event);
        snapshotStore.put(snapshot);
        metrics.processed(System.nanoTime() - start);
        return true;
    }

    static MarketSnapshot toSnapshot(MarketEvent event) {
        return new MarketSnapshot(
                event.symbol(),
                event.sequence(),
                event.bid(),
                event.ask(),
                (event.bid() + event.ask()) / 2.0,
                Instant.now());
    }

    public MarketSnapshot latest(String symbol) { return snapshotStore.get(symbol); }
    public ProcessingMetrics metrics() { return metrics; }

    @Override
    public void close() {
        executors.values().forEach(ExecutorService::shutdown);
    }
}
