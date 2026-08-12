package com.hrutvik.marketdata;

public interface SnapshotStore {
    void put(MarketSnapshot snapshot);
    MarketSnapshot get(String symbol);
}
