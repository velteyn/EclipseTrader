# Unfulfilled Roadmap & Missing Features

## Overview

Over the course of the project's development, numerous features, architectural modernization efforts, and fixes were requested and attempted—often using AI coding assistants. However, many of these initiatives remained incomplete, unmerged, or stalled in dead git branches due to compilation breakages, complex OSGi runtime failures, or limitations in automated reasoning.

This document synthesizes these **always-requested but never-achieved features**, mapping out what was attempted in dead branches, why AI/automated attempts performed poorly or failed, and what remains missing from the system.

---

## Analysis of Dead & Unmerged Branches

A forensic examination of the repository's git branches reveals a history of abandoned features and half-implemented fixes:

| Branch Name | Attempted Feature / Fix | Status & Root Cause of Failure / Abandonment |
| :--- | :--- | :--- |
| `origin/feature/modernize-chart-rendering` | **Adaptive Market Engine & Advanced Simulation**: Matching engine, position leverage, daily calendar, adaptive agents, chart rendering refactor. | **Unmerged / Abandoned**. Introduced heavy unverified changes (over 3,500 files touched/generated, uncommitted workspace locks) without complete test coverage or OSGi runtime validation. |
| `origin/attempt-upgrade-eclipse` | **Migration to Modern Eclipse RCP / Java 21+**: Upgrading Target Platform and Java compiler compliance. | **Failed / Abandoned**. Commit message explicitly states: *"first attempt, not compiling"*. Broke Eclipse RCP 3.x compatibility, missing e4 workbench bundles (`InjectionException`, missing `IEventBroker`). |
| `origin/feature/ai-gm-news-integration` | **AI Game Master & Dynamic News Generator**: Integrating LLM/AI-driven news events into JESSX simulation engine. | **Unmerged / Stalled**. Merged intermediate startup fixes (`jules-fix-jessx-startup`), but the actual AI Game Master LLM integration logic was never fully wired to `ExperimentManager` or `MessageTimer`. |
| `origin/origin-bot-attempt` | **Hibernate Persistence & Dependency Restoration**: Repairing Hibernate database dependencies and Databinding. | **Unmerged**. AI agent downloaded JARs manually into `org.hibernate`, but build stalled on Databinding compilation errors (`IObservableValue` bindings in UI). |
| `origin/bugfix/jessx-startup-errors` | **Startup Error Patching**: Manual patches for JESSX startup race conditions. | **Superseded / Partially Unmerged**. Contained ad-hoc `.mvn` configs and hardcoded paths that broke cross-platform execution. |
| `origin/fix/jessx-startup-race-condition` | **Concurrent Client Login & Threading**: Solving connection storm when bots connect simultaneously. | **Partially Merged**. Fixed core server socket concurrency, but left lingering timing delays (`Thread.sleep`) instead of robust event listeners. |
| `origin/jules-fix-play-button` | **Play Button Activation & User Scenarios**: Wire "Play" feed action to user-editable JESSX scenario XML files. | **Merged into master**, but exposed deeper flaws in scenario switching UI and missing preference persistence. |

---

## Detailed List of Missing & Incomplete Features

### 1. Modern Adaptive Market Engine & Leverage Mechanics
*   **Requested Vision**: A full-featured market engine supporting leverage, margin calls, position liquidations, short selling, and realistic order book matching rules (continuous auction vs. call market).
*   **Current Reality**: JESSX only supports basic spot order matching without leverage or margin requirements. The code in `origin/feature/modernize-chart-rendering` attempted this but remains unmerged and unverified.

### 2. Intelligent AI Trading Bots (Beyond Naive Heuristics)
*   **Requested Vision**: Smart algorithmic agents utilizing reinforcement learning, sentiment analysis, or local LLMs to evaluate market context and adjust strategies dynamically.
*   **Current Reality**: Existing bots (`Discreet`, `DiscreetIT`, `NotDiscreet`) use extremely simple rule-based heuristics (e.g. checking if news string contains "good" or "bad" and placing fixed-offset limit orders). They lack market depth analysis, momentum indicators, or risk management.

### 3. AI Game Master (GM) & Dynamic News Generator
*   **Requested Vision**: An AI Game Master that acts as a market catalyst, generating narrative news headlines, macroeconomic shocks, and corporate earnings releases using local LLMs (e.g., Ollama / llama.cpp), and observing agent reactions in real time.
*   **Current Reality**: News items are static XML tags (`<Information>`) hardcoded into `scenario.xml` files with predetermined trigger times. The branch `origin/feature/ai-gm-news-integration` failed to deliver a working LLM orchestrator.

### 4. Modern Eclipse Target Platform & Modern Java (Java 21 / 25)
*   **Requested Vision**: Migrate the codebase from ancient Java 8 / Eclipse Juno (3.8) abstractions to modern Java 21+ LTS and Eclipse e4 platform architecture.
*   **Current Reality**: The application is stuck in legacy Eclipse 3.x compatibility mode. Attempts to upgrade (`origin/attempt-upgrade-eclipse`) caused fatal runtime exceptions (`InjectionException`, missing e4 application context, bundle resolution deadlocks due to Lucene/incubator modules).

### 5. Modern UI Widgets & Responsive Presentation
*   **Requested Vision**: A modern UI with responsive charts, dark mode support, fluid layout docking, and updated Nebula widgets.
*   **Current Reality**: Modern Nebula widgets (`pshelf`, `cwt`, `cdatetime`, `grid`) had to be completely removed from `feature.xml` and target platforms because they crashed with modern SWT. `TraderPresentationFactory.java` remains commented out because it relied on deleted internal Eclipse APIs.

### 6. Automated Testing Suite & CI/CD Verification
*   **Requested Vision**: A comprehensive, fast unit test suite verifying core trading logic, order matching, and UI event propagation in CI.
*   **Current Reality**: The legacy JUnit 3 suite (`org.eclipsetrader.core.tests`) fails in headless environments ("No more handles" SWT error). Tests must be skipped with `-DskipTests` to build the repository.

---

## Why AI Coding Attempts Failed or Delivered Poor Results

Analyzing past AI interactions and commits reveals recurring patterns that led to poor outcomes:

1.  **Symptom Patching Over Root-Cause Analysis**: AI agents repeatedly introduced brittle delays (`Thread.sleep(1000)`) to hide race conditions between `StreamingConnector` and `BrokerConnector`, rather than implementing proper state synchronization (`CountDownLatch`, event listeners).
2.  **Destructive Refactoring & Unchecked File Changes**: In branches like `feature/modernize-chart-rendering`, thousands of generated or binary files (Mylyn task indexes, Eclipse workspace locks) were committed, polluting git history without validating runtime execution.
3.  **OSGi Dependency Misunderstandings**: AI models struggled with OSGi runtime dynamics—editing POM files without updating `META-INF/MANIFEST.MF`, or introducing hardcoded JAR paths (`C:/...`) that failed on non-Windows developer machines.
4.  **Inability to Perform Interactive Graphical Testing**: Because the RCP product produces a native GUI executable (`trader.exe` / SWT), headless AI agents could not visually inspect or test interactive UI flows, leading to hidden runtime NullPointerExceptions.
