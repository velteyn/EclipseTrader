# Future Roadmap, Domain Clarification & Actionable TODO List

## Overview & Core Philosophy: Simulation vs. Live Exchange Trading

To build the future of this project, it is essential to understand its **inner core**: **EclipseTrader with JESSX is fundamentally an Agent-Based Experimental Market Simulator**, NOT a live broker execution interface or automated retail trading terminal.

---

### Clarifying the Concepts: Live Protocols vs. Market Simulation

#### 1. What is the FIX Protocol & Crypto Exchange Integration?
*   **FIX Protocol (Financial Information eXchange)**: An electronic communications protocol used by real-world institutional brokerages, investment banks, and stock exchanges (e.g. NYSE, NASDAQ, CME) to transmit real-time trade requests, fills, and order status over private networks.
*   **Crypto Exchange APIs (WebSockets / REST)**: Real-time public data streams and private execution channels provided by commercial cryptocurrency exchanges (e.g. Binance, Coinbase) to trade real assets on live public order books.
*   **Why Live Trading Protocols are Secondary**: While external exchange connectors allow a terminal to view real live quotes or route real money to external venues, **they do not create a market**. They only observe or consume liquidity provided by external third parties.

#### 2. What is JESSX Market Simulation & Why is it the Inner Core?
*   **Agent-Based Market Microstructure**: JESSX is a self-contained, closed-loop financial laboratory. It creates a **synthetic market ecosystem** from scratch:
    *   **The Matching Engine (`OrderMarket.java`)**: Maintains continuous double auction order books, matching bid and ask limit/market orders internally.
    *   **Multi-Agent Economy**: Populates the market with synthetic participant personas (`PlayerType` / `Robot`), such as institutional market makers, noise traders, dividend investors, and human traders.
    *   **Controlled Economic Scenarios**: Defines fundamental asset values, interest rates, news events, and initial asset allocations (`scenario.xml`).
*   **The Goal**: JESSX is used to research **how markets behave under controlled conditions**: testing how human emotion, automated trading algorithms, liquidity shocks, and news rumors impact price discovery, volatility, bid-ask spreads, and systemic crashes.

---

## Strategic Architecture for Advanced Market Simulation

```
+-------------------------------------------------------------------------------+
|                      ADVANCED JESSX SIMULATION ENGINE                         |
|                                                                               |
|  +-------------------------------------------------------------------------+  |
|  |                  AI MARKET GAME MASTER (LOCAL LLM)                      |  |
|  |   - Generates Dynamic Macroeconomic Events, Rumors & Systemic Shocks    |  |
|  |   - Governs Circuit Breakers, Dividend Rules & Market Regulators        |  |
|  +------------------------------------+------------------------------------+  |
|                                       |                                       |
|  +------------------------------------v------------------------------------+  |
|  |             SIMULATION HISTORICAL TIME-SERIES DATABASE                  |  |
|  |   - Persists Full Simulation Tick Data, Order Books, Trades & OHLC       |  |
|  |   - Replay & Analytics via Embedded Time-Series Storage (DuckDB/Timescale) |  |
|  +-------------------+--------------------------------+--------------------+  |
|                      |                                |                       |
|  +-------------------v-------------+    +-------------v--------------------+  |
|  |     SMART SIMULATED BOTS        |    |   SYNTHETIC MARKET MECHANICS     |  |
|  | - Local LLM Reasoning Agents    |    | - Call Auctions, Continuous Depth|  |
|  | - RL Gym Agents & Liquidity Bots|    | - Margin Calls, Leverage, Shorting|  |
|  +---------------------------------+    +----------------------------------+  |
+-------------------------------------------------------------------------------+
```

---

## Strategic Action TODO List for Market Simulation

### Task 1: Enhanced Synthetic Market Engine & Microstructure

#### Goal
Expand the capabilities of `OrderMarket.java` and `BusinessCore.java` to simulate advanced exchange matching mechanics and market structures beyond simple spot trading.

#### TODO Items
* [ ] **Multi-Venue Synthetic Exchanges & Cross-Market Arbitrage**:
  * Allow running multiple JESSX matching engines simultaneously simulating fragmented stock exchanges (e.g. "Exchange A" and "Exchange B" for the same asset).
  * Introduce arbitrage bots that exploit price discrepancies between simulated venues.
* [ ] **Realistic Market Order Types & Microstructure Rules**:
  * Implement Call Auction opening/closing crosses, Stop-Loss triggers, Iceberg orders, and Fill-or-Kill (FOK) execution conditions in `OrderMarket.java`.
* [ ] **Leverage, Margin Calls & Short Selling Engine**:
  * Add margin accounts and borrowing mechanics (`MarginAccount.java`): Allow bots and human players to short sell assets or trade on leverage, with automatic forced liquidations when margin limits are breached.
* [ ] **Transaction Fees, Slippage & Dark Pools**:
  * Simulate exchange fee schedules, variable maker/taker rebates, and dark pool execution venues to analyze institutional execution costs.

---

### Task 2: Intelligent Trading Bots & Autonomous Simulation Agents

#### Goal
Replace primitive random bots (`NotDiscreet`) with intelligent agents capable of realistic reasoning, technical analysis, and sentiment analysis.

#### TODO Items
* [ ] **Local LLM-Driven Reasoning Agent (`LLMTradingBot.java`)**:
  * Connect simulated trading bots to a local LLM server (Ollama endpoint `http://localhost:11434/api/generate`) using structured JSON prompts.
  * Bots analyze market depth, news sentiment, and portfolio P&L to decide trading actions:
    ```json
    {
      "action": "BUY",
      "quantity": 150,
      "limit_price": 42.50,
      "reasoning": "Central bank interest rate cuts favor high-growth equities."
    }
    ```
* [ ] **Quantitative & Technical Indicator Bots**:
  * Implement bots driven by quantitative indicators (`org.eclipsetrader.ui.charts.indicators`): Mean Reversion, Momentum/RSI, Order Book Imbalance (OBI), and Market Making spread bots.
* [ ] **Reinforcement Learning (RL) Gym Bridge**:
  * Create a Python/Java socket bridge exposing JESSX simulation state as an `OpenAI Gym` / `Farama Gymnasium` environment for training Deep Q-Networks (DQN) or PPO trading policies against human players.

---

### Task 3: Intelligent News Engine via Local LLM (Ollama)

#### Goal
Transform static `<Information>` XML tags into an interactive, dynamic news generation system.

#### TODO Items
* [ ] **Local LLM News Generator (`LLMNewsProvider.java`)**:
  * Integrate an offline local LLM (`llama3:8b` or `mistral:7b` via Ollama) into `INewsService`.
  * Generate dynamic financial news flashes, corporate earning releases, and central bank announcements based on real-time simulation price action and volatility.
* [ ] **Information Asymmetry & Delayed Propagation**:
  * Simulate insider trading and news propagation delays by broadcasting news flashes to institutional bots first, delaying transmission to retail player feeds by configurable latency offsets.

---

### Task 4: AI Market Game Master (GM) Orchestrator

#### Goal
Create an overarching AI orchestrator that acts as the scenario manager, regulator, and catalyst for the simulated economy.

#### TODO Items
* [ ] **AI Market Game Master Core (`MarketGameMaster.java`)**:
  * Monitor market order book liquidity, bid/ask spreads, and trader P&L in real time.
  * Dynamically trigger economic shocks (e.g. "Geopolitical Crisis", "Short Squeeze", "Earnings Surprise") when market activity slows down.
* [ ] **Circuit Breakers & Regulatory Interventions**:
  * Implement automated market regulators that halt trading when price volatility exceeds safety thresholds.
* [ ] **Interactive GM Operator Dashboard**:
  * Build an Eclipse RCP view (`GameMasterViewPart.java`) allowing human experimenters to issue natural language commands to the Game Master (e.g., *"Inject panic selling in the tech sector"*).

---

### Task 5: Simulation Historical Time-Series Database & Replay

#### Goal
Persist complete market simulation data for offline research, chart analysis, and strategy backtesting.

#### TODO Items
* [ ] **Embedded Time-Series Storage (`SimulationHistoryStore.java`)**:
  * Integrate an embedded high-performance time-series database engine (e.g., **DuckDB**, **SQLite / H2 TimeSeries**) to record tick-by-tick trades, order book snapshot depth, and OHLC candlestick bars generated during JESSX simulation runs.
* [ ] **Data Export & Python Research Bridge**:
  * Support exporting simulation run data to standardized Parquet / CSV files for quantitative analysis in Pandas/Polars.
* [ ] **Simulation Replay Engine (`SimulationReplayConnector.java`)**:
  * Implement a playback feed connector capable of replaying previously recorded JESSX simulation runs at variable speeds (1x, 5x, 10x) to benchmark bot strategies offline.

---

### Task 6: Platform & Technical Modernization

#### TODO Items
* [ ] **Target Platform Upgrade to Java 21 / 25 LTS**:
  * Add `--add-modules jdk.incubator.vector` JVM arguments to `eclipsetrader.product` to resolve Lucene OSGi dependencies.
* [ ] **CI Test Automation**:
  * Enable headless UI test harness using Virtual Display (`xvfb`) for SWT bundles in GitHub Actions CI workflows (`maven.yml`).
