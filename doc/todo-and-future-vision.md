# Future Roadmap & Actionable TODO List

## Overview

This document outlines a strategic roadmap and actionable TODO list to transform EclipseTrader into a cutting-edge, AI-driven stock market simulation and trading research platform.

It addresses key architectural gaps and provides concrete design suggestions for:
1.  **Expanded Market Exchanges & Connectivity**
2.  **Smarter Trading Bots & Autonomous Agents**
3.  **Intelligent News Generation via Local LLMs (Ollama / llama.cpp)**
4.  **AI Market Game Master (GM) Orchestrator**
5.  **Simulation Historical Time-Series Database & Data Replay**
6.  **Technical Modernization Strategy (Java 21/25 & Eclipse e4)**

---

## Strategic Action TODO List

```
+-------------------------------------------------------------------------------+
|                       ECLIPSETRADER FUTURE ARCHITECTURE                       |
|                                                                               |
|  +-------------------------------------------------------------------------+  |
|  |                  AI MARKET GAME MASTER (LOCAL LLM)                      |  |
|  |   - Generates Dynamic Economic Scenarios, Shocks & News Events           |  |
|  |   - Monitors Market Microstructure & Agent Performance in Real-Time       |  |
|  +------------------------------------+------------------------------------+  |
|                                       |                                       |
|  +------------------------------------v------------------------------------+  |
|  |             SIMULATION HISTORICAL TIME-SERIES DATABASE                  |  |
|  |   - Persists Full Simulation Tick Data, Order Books, Trades & OHLC       |  |
|  |   - Replay & Analytics via Embedded Time-Series Storage (DuckDB/Timescale) |  |
|  +-------------------+--------------------------------+--------------------+  |
|                      |                                |                       |
|  +-------------------v-------------+    +-------------v--------------------+  |
|  |    SMART TRADING AGENTS         |    |   MULTI-EXCHANGE ENGINE          |  |
|  | - Sentiment & Technical Agents  |    | - Fix Protocol / WebSockets      |  |
|  | - RL / Local LLM Reasoning      |    | - Crypto & Forex Adapters        |  |
|  +---------------------------------+    +----------------------------------+  |
+-------------------------------------------------------------------------------+
```

---

### Task 1: Expanded Market Exchanges & Connectivity

#### Current Limitation
The application is currently bound to JESSX simulated order books or zero-network paper trading. Legacy connectors (`Directa`, `Yahoo`) are broken/deactivated.

#### Recommendations & TODO Items
* [ ] **Implement FIX Protocol Connector (`org.eclipsetrader.fix`)**:
  * Integrate QuickFIX/J library to connect with institutional brokers and crypto exchanges (e.g. Coinbase Advanced, Interactive Brokers FIX CTCI).
  * Map FIX execution reports (`ExecutionReport`) directly to core `IOrder` and `TradeStore` entities.
* [ ] **Modern REST & WebSocket Feed Adapters**:
  * Create a modular `org.eclipsetrader.feed.websocket` bundle using Java 11 `HttpClient` / WebSocket client.
  * Implement streaming connectors for free/low-cost crypto and equity APIs (Binance, Alpaca Markets, Polygon.io, Yahoo Finance v8 WS).
* [ ] **Multi-Currency & FX Exchange Support**:
  * Enhance `Market.java` to support cross-currency order settlement and live forex rate conversion feeds (`CurrencyConverter.java`).
* [ ] **Realistic Market Microstructure & Order Types**:
  * Support Iceberg orders, Stop-Loss/Take-Profit triggers, and Call Auction opening/closing crosses within `OrderMarket.java`.

---

### Task 2: Intelligent Trading Bots & Autonomous Agents

#### Current Limitation
Existing bots (`Discreet`, `NotDiscreet`) use simplistic random numbers or crude string matching on news headlines ("good" vs "bad").

#### Recommendations & TODO Items
* [ ] **Sentiment-Aware Local LLM Agent (`LLMTradingBot.java`)**:
  * Interface agents with a local LLM server (Ollama endpoint `http://localhost:11434/api/generate`) using lightweight JSON requests.
  * Pass news headlines, current portfolio status, and order book top-5 bid/ask depth into the LLM prompt.
  * Parse structured JSON decisions from the LLM:
    ```json
    {
      "action": "BUY",
      "quantity": 100,
      "limit_price": 154.50,
      "reasoning": "Positive earnings surprise indicates strong Q3 upside."
    }
    ```
* [ ] **Technical Indicator-Driven Bots**:
  * Implement strategy bots driven by technical indicators (`org.eclipsetrader.ui.charts.indicators`): Moving Average Crossover, RSI Oversold/Overbought, and Bollinger Band Breakout bots.
* [ ] **Reinforcement Learning (RL) Gym Bridge**:
  * Create a Python/Java socket bridge exposing JESSX simulation state as an `OpenAI Gym` / `Farama Gymnasium` environment for training Deep Q-Networks (DQN) or PPO trading policies.

---

### Task 3: Intelligent News System via Local LLM

#### Current Limitation
News items are static strings loaded from XML scenario files with predefined time offsets.

#### Recommendations & TODO Items
* [ ] **Local LLM News Engine (`LLMNewsProvider.java`)**:
  * Connect `INewsService` to an offline local LLM (e.g. `llama3:8b` or `mistral:7b` via Ollama).
  * Generate realistic financial news flashes, central bank interest rate announcements, and corporate quarterly reports on the fly based on current market volatility and sector trends.
* [ ] **Impact Vector & Sentiment Tags**:
  * Have the local LLM tag generated news with quantitative shock vectors:
    ```json
    {
      "headline": "Federal Reserve Cuts Benchmark Rate by 50bps",
      "affected_sectors": ["Banking", "Tech"],
      "sentiment_score": 0.85,
      "expected_volatility": "HIGH"
    }
    ```
* [ ] **News Propagation Delay Simulation**:
  * Simulate realistic information asymmetry by broadcasting news flashes to high-frequency institutional bots instantly while delaying transmission to retail player feeds by a configurable latency (e.g. 5-15 seconds).

---

### Task 4: AI Market Game Master (GM) Orchestrator

#### Current Limitation
The simulation state machine (`ExperimentManager.java`) only counts down static period seconds and ends.

#### Recommendations & TODO Items
* [ ] **AI Game Master Core (`MarketGameMaster.java`)**:
  * Implement an overarching AI orchestrator that acts as the "Narrator" and "Market Maker of Last Resort".
  * The Game Master monitors overall market order book liquidity, bid/ask spreads, price volatility, and trader P&L.
* [ ] **Dynamic Scenario Adaptation & Black Swan Events**:
  * If the market becomes stagnated or low-volume, the Game Master dynamically triggers macro events (e.g. "Geopolitical Tension Spikes Oil Prices", "Short Squeeze Initiated in Tech Sector").
  * Introduce circuit breakers and market halts if volatility exceeds safety thresholds.
* [ ] **Interactive GM UI Dashboard**:
  * Build an Eclipse RCP view (`GameMasterViewPart.java`) allowing human operators to prompt the Game Master in natural language (e.g. *"Introduce a tech sector crash in 30 seconds"*).

---

### Task 5: Simulation Historical Time-Series Database & Replay

#### Current Limitation
Chart and trade data generated during a JESSX simulation run are currently stored in-memory (`History.java`) or cleared when the application exits, preventing long-term historical analysis or backtesting across simulation runs.

#### Recommendations & TODO Items
* [ ] **Embedded Time-Series Database (`SimulationHistoryStore.java`)**:
  * Integrate an embedded high-performance storage engine (e.g. **DuckDB**, **SQLite / H2 TimeSeries**, or **TimescaleDB** adapter) to record tick-by-tick trades, order book depth snapshots, and OHLC candlestick bars generated during JESSX simulation runs.
* [ ] **Simulation History Export & Import**:
  * Allow exporting simulation run data to standardized Parquet / CSV / HDF5 files for quantitative research in Python (Pandas/Polars).
* [ ] **Simulation Replay Engine (`SimulationReplayConnector.java`)**:
  * Implement a playback feed connector capable of replaying previously recorded JESSX simulation runs at variable speeds (1x, 5x, 10x) to benchmark bot strategies offline.

---

### Task 6: Technical Modernization & Platform Upgrade

#### Recommendations & TODO Items
* [ ] **Upgrade Target Platform to Java 21 / 25 LTS**:
  * Resolve OSGi bundle resolution errors by adding required JDK incubator modules (`--add-modules jdk.incubator.vector`) to `eclipsetrader.product`.
  * Update Tycho compiler configuration in `pom.xml` to target Java 21 LTS.
* [ ] **Fix & Modernize Test Harness**:
  * Complete migration of legacy JUnit 3 tests in `org.eclipsetrader.core.tests` to JUnit 5 Jupiter in `org.eclipsetrader.core.modern.tests`.
  * Enable headless UI testing harness using Virtual Display (`xvfb`) for SWT bundles in GitHub Actions CI workflows (`maven.yml`).
* [ ] **Clean Up Legacy Code & Unused Bundles**:
  * Formally remove dead legacy connectors (`directa`, `yahoo`, `borsaitalia`) or move them to an `archived-plugins/` directory.
  * Restore custom UI presentation renderer (`TraderPresentationFactory.java`) using modern Eclipse e4 application model APIs.
