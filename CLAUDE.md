# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./gradlew build        # Compile and run tests
./gradlew test         # Run tests only
./gradlew clean        # Clean build artifacts
./gradlew run          # Run the application (requires application plugin)
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Project Overview

**L2Agent** is a Java project scaffolded with Gradle 9.0.0, targeting JDK 17. It is currently a skeleton —
`src/main/java/ru/che/lcp/Main.java` is the only source file and contains only template code.

- **Group:** `ru.che.lcp`
- **Build:** Gradle 9.0.0 (wrapper at `gradle/wrapper/`)
- **Testing:** JUnit 5 (Jupiter), no tests written yet
- **Dependencies:** Maven Central; test-only dependencies (JUnit BOM 5.10.0)

## Architecture

There is no meaningful architecture yet. The entry point is `ru.che.lcp.Main`. New code should be placed under
`src/main/java/ru/che/lcp/` and tests under `src/test/java/ru/che/lcp/`.
