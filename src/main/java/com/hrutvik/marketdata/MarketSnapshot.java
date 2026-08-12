package com.hrutvik.marketdata;

import java.time.Instant;

public record MarketSnapshot(
        String symbol,
        long sequence,
        double bid,
        double ask,
        double mid,
        Instant updatedAt
) {}
