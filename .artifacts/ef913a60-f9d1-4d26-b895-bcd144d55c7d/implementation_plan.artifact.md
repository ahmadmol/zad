# Implementation Plan - Fix Gradle/Kotlin Memory Issues

Resolve the "Could not connect to Kotlin compile daemon" and "Unable to allocate bitmaps" errors by optimizing JVM memory settings.

## User Review Required

> [!IMPORTANT]
> The build is failing because it's trying to allocate 4GB of memory while the system has very little free RAM (approx. 117MB). I will reduce the memory limits to allow the build to proceed.

## Proposed Changes

### Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/WIN%2010/Desktop/New%20folder%20(2)/Sdk/Sdk/mol/gradle.properties)
- Reduce `org.gradle.jvmargs` heap from `4096m` to `2048m`.
- Add `kotlin.daemon.jvmargs` with a `1536m` limit and `-XX:-UseParallelGC` (as suggested by the error log) to reduce the memory footprint of the Kotlin compiler.

## Verification Plan

### Automated Tests
- Run `./gradlew --stop` to clear existing hung daemons.
- Run `./gradlew help` to verify the daemon starts correctly with new settings.
- Run `./gradlew :app:assembleDebug` (if memory allows) to confirm compilation succeeds.

### Manual Verification
- Monitor system memory to ensure the machine remains responsive.
