package com.hrutvik.marketdata;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataProcessorTest {
    @Test
    void processesOrderedEventsAndBuildsSnapshot() {
        InMemoryEventStore eventStore = new InMemoryEventStore();
        InMemorySnapshotStore snapshotStore = new InMemorySnapshotStore();
        try (MarketDataProcessor processor = new MarketDataProcessor(eventStore, snapshotStore)) {
            assertTrue(processor.submit(event("1", 1, 99.0, 101.0)).join());
            assertTrue(processor.submit(event("2", 2, 100.0, 102.0)).join());
            MarketSnapshot snapshot = processor.latest("TEST");
            assertEquals(2, snapshot.sequence());
            assertEquals(101.0, snapshot.mid());
            assertEquals(2, eventStore.all("TEST").size());
        }
    }

    @Test
    void rejectsDuplicateAndStaleEvents() {
        try (MarketDataProcessor processor = new MarketDataProcessor(new InMemoryEventStore(), new InMemorySnapshotStore())) {
            assertTrue(processor.submit(event("1", 2, 100.0, 102.0)).join());
            assertFalse(processor.submit(event("1", 3, 101.0, 103.0)).join());
            assertFalse(processor.submit(event("2", 1, 98.0, 100.0)).join());
            assertEquals(1, processor.metrics().processedCount());
            assertEquals(1, processor.metrics().duplicateCount());
            assertEquals(1, processor.metrics().staleCount());
        }
    }

    @Test
    void replaysEventHistoryDeterministically() {
        InMemoryEventStore store = new InMemoryEventStore();
        store.append(event("1", 1, 99.0, 101.0));
        store.append(event("2", 2, 100.0, 102.0));
        store.append(event("3", 3, 101.0, 103.0));

        MarketSnapshot snapshot = new ReplayService(store).replay("TEST", 2);
        assertEquals(3, snapshot.sequence());
        assertEquals(102.0, snapshot.mid());
    }

    private MarketEvent event(String id, long sequence, double bid, double ask) {
        return new MarketEvent(id, "TEST", sequence, bid, ask, Instant.now());
    }
}
