package com.hrutvik.marketdata;

import java.util.concurrent.atomic.AtomicLong;

public final class ProcessingMetrics {
    private final AtomicLong processed = new AtomicLong();
    private final AtomicLong duplicates = new AtomicLong();
    private final AtomicLong stale = new AtomicLong();
    private final AtomicLong totalLatencyNanos = new AtomicLong();

    void processed(long latencyNanos) {
        processed.incrementAndGet();
        totalLatencyNanos.addAndGet(latencyNanos);
    }

    void duplicate() { duplicates.incrementAndGet(); }
    void stale() { stale.incrementAndGet(); }

    public long processedCount() { return processed.get(); }
    public long duplicateCount() { return duplicates.get(); }
    public long staleCount() { return stale.get(); }
    public double averageLatencyMicros() {
        long count = processed.get();
        return count == 0 ? 0.0 : totalLatencyNanos.get() / 1_000.0 / count;
    }
}
