package com.hrutvik.marketdata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MarketDataApplication {
    public static void main(String[] args) {
        InMemoryEventStore eventStore = new InMemoryEventStore();
        InMemorySnapshotStore snapshotStore = new InMemorySnapshotStore();

        try (MarketDataProcessor processor = new MarketDataProcessor(eventStore, snapshotStore)) {
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();
            for (long sequence = 1; sequence <= 1_000; sequence++) {
                futures.add(processor.submit(event("AAPL", sequence, 190.0 + sequence / 10_000.0)));
                futures.add(processor.submit(event("MSFT", sequence, 421.0 + sequence / 10_000.0)));
            }
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            MarketSnapshot latest = processor.latest("AAPL");
            ReplayService replayService = new ReplayService(eventStore);
            MarketSnapshot replayed = replayService.replay("AAPL", 900);

            System.out.printf("latest=%s seq=%d mid=%.4f%n", latest.symbol(), latest.sequence(), latest.mid());
            System.out.printf("replayedFrom900 seq=%d mid=%.4f%n", replayed.sequence(), replayed.mid());
            System.out.printf("processed=%d duplicate=%d stale=%d avgLatencyMicros=%.2f%n",
                    processor.metrics().processedCount(), processor.metrics().duplicateCount(),
                    processor.metrics().staleCount(), processor.metrics().averageLatencyMicros());
        }
    }

    private static MarketEvent event(String symbol, long sequence, double mid) {
        return new MarketEvent(UUID.randomUUID().toString(), symbol, sequence,
                mid - 0.05, mid + 0.05, Instant.now());
    }
}
