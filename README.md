# Distributed Market Data Replay & Snapshot Service

Java 21 service demonstrating ordered market-data ingestion, append-only event storage, idempotent consumption, atomic snapshots, and deterministic replay.

## What it demonstrates

- per-instrument event ordering
- idempotent processing and duplicate rejection
- append-only event history
- atomic latest-state snapshots
- deterministic replay from sequence checkpoints
- consumer-lag, throughput, and latency metrics
- failure recovery without corrupting state

## Architecture

```text
MarketEventProducer
       |
       v
PartitionedEventBus (symbol -> single-thread partition)
       |
       +--> AppendOnlyEventStore
       |
       +--> SnapshotStore
       |
       +--> Metrics

ReplayService --> EventStore --> rebuild snapshot deterministically
```

The local implementation uses in-memory adapters so the core behaviour can be run without external infrastructure. The interfaces are intentionally shaped so Kafka, PostgreSQL, and Redis adapters can be added later.

## Tech

Java 21, Maven, JUnit 5, Docker, GitHub Actions

## Run

```bash
mvn clean test
mvn exec:java
```

## Roadmap

- Kafka topic partitioning by instrument
- PostgreSQL append-only event persistence
- Redis latest-snapshot distribution
- Docker Compose for Kafka/Postgres/Redis
- p95/p99 latency histograms and backpressure tests

## Author

Hrutvik Nagrale — https://github.com/ihrutvik
