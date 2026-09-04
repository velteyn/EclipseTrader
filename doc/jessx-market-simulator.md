# JESSX Market Simulator (`org.eclipsetrader.jessx`)

## Overview

The **JESSX Market Simulator** (`org.eclipsetrader.jessx`) is the primary simulation engine in EclipseTrader. Originally integrated from the standalone JESSX project, it provides an end-to-end stock market simulator capable of running scripted trading sessions with multi-agent trading bots, order matching, real-time Level II order book streaming, and news propagation.

```
+-------------------------------------------------------------------------+
|                         JESSX SIMULATOR ENGINE                          |
|                                                                         |
|  +------------------------+             +----------------------------+  |
|  |   Scenario XML File    |             |  ExperimentManager         |  |
|  | (Assets, News, Bots)   |------------>|  (Periods, State Machine)  |  |
|  +------------------------+             +--------------+-------------+  |
|                                                        |                |
|  +------------------------+             +--------------v-------------+  |
|  | ClientConnectionPoint  |<----------->| NetworkCore & Server Socket|  |
|  |  (Concurrent Logins)   |             |       (Port 6290)          |  |
|  +-----------+------------+             +--------------+-------------+  |
|              |                                         |                |
|  +-----------v------------+             +--------------v-------------+  |
|  | Automated Trading Bots |             |  BrokerConnector / Feed    |  |
|  | (Discreet, NotDiscreet)|             |  (Level II, Trades, Charts)|  |
|  +------------------------+             +----------------------------+  |
+-------------------------------------------------------------------------+
```

---

## Core Components & Architecture

### 1. BrokerConnector (`BrokerConnector.java`)
The central orchestrator for the server side in EclipseTrader.
*   **Initialization & Scenario Copying**: On first execution, checks `<workspace>/.metadata/.plugins/org.eclipsetrader.jessx/` for scenario files. Copies default scenario template from plugin resources if missing.
*   **Startup Synchronization**: Uses a `CountDownLatch` to signal when the `ClientConnectionPoint` server socket is bound and listening before client connectors attempt login.
*   **Server Lifecycle**: Spins up the server socket thread, connects the human player (`ThePlayer`), loads automated trading bots, and triggers `ExperimentManager.beginExperiment()`.
*   **XML Message Dispatcher**: Implements `objectReceived(Object obj)` to parse JDOM XML packets from the JESSX server:
    *   `Portfolio`: Balance & asset holding updates.
    *   `OrderBook`: Level II bid/ask depth updates.
    *   `Trade`: Real-time execution transactions (updates Level II header & chart OHLC history).
    *   `Quote`: Real-time bid/ask quote updates.
    *   `TodayOHL`: High/Low summary statistics.

### 2. Networking Engine (`ClientConnectionPoint.java` & `NetworkCore.java`)
*   **Server Socket Binding**: Listens on TCP port `6290`.
*   **Concurrent Login Handler**: Runs a dedicated thread pool for incoming player logins via `PreConnectionClient` (runnable) to avoid connection storm bottlenecks when tens of bots connect simultaneously.
*   **Player Registry**: Manages player instances (`Player.java`), assigns categories/personas, tracks readiness states (`arePlayersReady()`), and routes incoming order operations.

### 3. Scenario Loader (`Scenario.java` & `BusinessCore.java`)
Loads scenario XML files defining market parameters:
*   **`<Asset>` & `<Institution>`**: Defines tradable securities, quoted currencies, initial prices, tick sizes, dividend policies, and order book matching rules (`OrderMarket.java`).
*   **`<PlayerType>`**: Defines agent personas (e.g., "Poor", "Investment Group", "Big Institution") with initial capital, asset holdings, and max order volumes.
*   **`<Information>`**: Defines scripted news items, press releases, and announcements scheduled for specific period offsets.

### 4. Experiment Manager (`ExperimentManager.java`)
Manages trading simulation state and timing:
*   **State Machine**: Cycles through periods (`waiting`, `active`, `ended`).
*   **Period Timer**: Configurable duration per trading period (e.g., 300 seconds).
*   **News Dispatcher**: Transmits scheduled `<Information>` items to connected players during trading sessions based on time triggers (`MessageTimer.java`).
*   **Readiness Sync**: Automatically transitions trading periods once all bots and players report ready.

---

## Automated Trading Bots (`org.eclipsetrader.jessx.trobot`)

Trading agents run in dedicated background threads and execute autonomous strategy logic via `MyAct()`:

### 1. Base Agent (`Robot.java`)
*   Extends `Thread` and maintains an internal `Portfolio` and `OrderBook` local cache.
*   Includes `reactToNews(String institution)`: Reads sentiment attributes (`good`, `bad`, `neutral`) from news items and scales buy/sell decisions based on assigned persona volume limits.

### 2. Strategy Implementations
*   **`Discreet`**: A market-making strategy that places limit orders around the current best bid/ask spread to provide market liquidity.
*   **`DiscreetIT`**: An institutional portfolio rebalancing bot that periodically adjusts portfolio exposure based on price trends.
*   **`NotDiscreet`**: A random liquidity taker that issues aggressive market/limit orders within random price ranges to simulate noise trading.

---

## Data Streaming Protocol (`StreamingConnector.java` & `CreaMsg.java`)

*   **Binary Protocol**: Uses a low-level binary socket connection (`CreaMsg.java`) to manage feed subscriptions.
*   **Subscription Messages**: Sends binary commands (`PORT_ADD`, `PORT_MOD`, `PORT_DEL`) to request real-time market updates for specific securities.
*   **UI Push**: Received trade trades and quotes are passed to `FeedSubscription.java`, triggering `wakeupNotifyThread()` to push updates directly to SWT viewers and Chart objects on the UI thread.
