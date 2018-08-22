# (POC) SWALka - Scala Write Ahead Log

The project goal is to create fast and reliable general write ahead log implementation, for use cases such as - 
guaranteed side effect execution even in face of jvm or node crash. It can be used also for limited event sourcing usecases.

The Flow would be:
 1. [main app logic] -> [side effect description] -> [wal]
 2. [wal] -> [wal processor] -> [sideffect execution attempt] -> [offset commit]

## Benchmark
```text
Benchmark                                          Mode  Cnt    Score   Error   Units
ACoordinatorWriterBenchmark.walWriteWithFlush     thrpt        12.323          ops/ms
ACoordinatorWriterBenchmark.walWriteWithoutFlush  thrpt       170.620          ops/ms
AWriterBenchmark.walWriteWithFlush                thrpt        11.796          ops/ms
AWriterBenchmark.walWriteWithoutFlush             thrpt       173.709          ops/ms
```

## Data model
Each log is split in segments of configured size

segments - represents index of journal segments, format [int - segment number][long - timestamp when segment is closed]

journal.N - represents actual log, format [int - data length][long - crc32 checksum][bytes of lenght size of data]