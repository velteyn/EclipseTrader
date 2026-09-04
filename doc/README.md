# EclipseTrader Technical Documentation Index

Welcome to the comprehensive technical documentation for **EclipseTrader** and its core market simulation engine, **JESSX**.

This documentation hub provides an in-depth analysis of the system's architecture, core domain models, simulation protocols, UI components, legacy branch history, and future development roadmap.

---

## Technical Documentation Index

| Document | Description |
| :--- | :--- |
| **[System Architecture & Plugin Infrastructure](architecture.md)** | Overview of Eclipse RCP architecture, OSGi bundle modularity, extension point system, Tycho reactor, and target platform build setup. |
| **[JESSX Market Simulator Engine](jessx-market-simulator.md)** | Complete technical breakdown of the JESSX simulation plugin (`org.eclipsetrader.jessx`), concurrent socket server architecture (port 6290), scenario XML parser, automated bot strategies (`Discreet`, `NotDiscreet`), streaming protocol, and experiment state machine. |
| **[Trading Core Domain Model & Persistence Layer](trading-core-and-repository.md)** | Deep dive into core domain entities (`ISecurity`, `IOrder`, `IAccount`, `IPortfolio`, `IHistory`), the `IRepositoryService` abstraction, Local XML storage, and Hibernate ORM backends (Derby / PostgreSQL). |
| **[UI Workbench & Charting System](ui-workbench-and-charts.md)** | Details on RCP views (Portfolio View, Level II Market Depth View, Order Dialog), Draw2D charting engine (`ChartViewPart`, `IChartObject`), technical indicator extensions, and SWT widget cleanup. |
| **[Broker & Feed Connector System](broker-and-feed-connectors.md)** | Guide to connector extension points, active brokers (`JessX`, `Paper`), and status of legacy deactivated connectors (`Directa`, `Yahoo Finance`, `Borsa Italiana`). |
| **[Unfulfilled Roadmap & Missing Features](missing-features-and-unfulfilled-roadmap.md)** | Forensic analysis of dead git branches (`feature/modernize-chart-rendering`, `attempt-upgrade-eclipse`, `feature/ai-gm-news-integration`, `origin-bot-attempt`), failed AI coding attempts, missing market features, and compiler/runtime limitations. |
| **[Strategic Roadmap, Domain Vision & TODO List](todo-and-future-vision.md)** | Actionable TODO roadmap clarifying Market Simulation vs Live Execution, smart LLM-driven trading bots, intelligent news via Ollama/llama.cpp, an AI Market Game Master (GM) orchestrator, simulation time-series databases, and Java 21/25 migration strategy. |

---

## About EclipseTrader & JESSX Market Simulator

**EclipseTrader with JESSX** is fundamentally an **Agent-Based Market Simulator and Experimental Economics Laboratory**. Rather than acting merely as a terminal for executing trades on external live brokerages, its inner core is a self-contained, closed-loop stock exchange.

The centerpiece of the platform is the **JESSX Plugin**, an embedded multi-agent market simulation engine. JESSX enables financial researchers, algorithmic developers, and market microstructure enthusiasts to execute simulated trading sessions, deploy autonomous trading bots, stream real-time Level II order books and candlestick charts, and analyze agent behavior under controlled economic scenarios.

```
+-------------------------------------------------------------------------------+
|                        JESSX SIMULATION SYSTEM                                |
|                                                                               |
|  +--------------------+  +-----------------------+  +----------------------+  |
|  | Core Platform      |  | JESSX Simulator       |  | Technical Charts     |  |
|  | (OSGi, Repositories|  | (Socket Server 6290,  |  | (Draw2D, Indicators, |  |
|  |  Securities, P&L)  |  |  Scenario XML, Bots)  |  |  Real-time OHLC)     |  |
|  +--------------------+  +-----------------------+  +----------------------+  |
+-------------------------------------------------------------------------------+
```

### Key Technical Highlights
*   **Target Platform & Build Reactor**: Built with Maven and Eclipse Tycho, targeting Java 11 / Java 8 compliance and packaged into a standalone native application executable (`trader.exe`).
*   **Extensible Extension Points**: Custom brokers, market data feeds, charting indicators, storage backends, and startup services plug into the core via OSGi extension points (`plugin.xml`).
*   **High-Throughput Socket Server**: Concurrent socket listener supporting multi-client login storms and real-time binary market data streaming.
