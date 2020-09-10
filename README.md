# Kotlin Coroutines performance comparison

### Overview
This is a sample project to test different configurations for Kotlin Coroutines regarding the CoroutineContexts and Dispatchers

The use case scenario is a script that has to make multiple intensive I/O operations to a remote services. To have a normalized latency, there is also an implementation mock I/O operation to emulate the remote call.

### Results
The results of testing the mock IO operation with the 3 approaches: GlobalScope(or Dispatcher.Default), Dispatcher.IO (default pool of 64) and a Custom Dispatcher with a pool size of 128, and the following params:`

- `Mocked IO Operation Dely = 100ms`
- `# Of concurrent IO Operations = 200`

```
Stats for CustomDispatcher - mocked IO: ExecutionStats(totalElapsedTime=341, individualTimesSum=21630, individualTimes=...)
Stats for DispatcherIO - mocked IO: ExecutionStats(totalElapsedTime=436, individualTimesSum=20887, individualTimes=...)
Stats for GlobalScope - mocked IO: ExecutionStats(totalElapsedTime=5223,individualTimesSum=20859, individualTimes=...)
```
The conclusions are:
- There is a very significant improvement (+10x) in using Dispatcher.IO or a custom Dispatcher compared with GlobalScope. This is expected as the pool size in GlobalScope is based on the amount of CPU cores.
- Using a custom Dispatcher with the double of pool size capacity (64 -> 128) does not represent a significant improvement in the overall elapsed time.

### Build & run
Gradle build
```
./gradlew clean build
```
To run
```
./gradlew run
```
