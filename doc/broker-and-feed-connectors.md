# Broker & Feed Connector System

## Overview

EclipseTrader decouples market data ingestion and order execution using two extension points: `org.eclipsetrader.core.brokers` (order execution) and `org.eclipsetrader.core.launchers` / feeds (streaming quotes and trades).

```
                      +---------------------------------+
                      |     CORE TRADING ENGINE         |
                      +--------+---------------+--------+
                               |               |
             +-----------------+               +-----------------+
             |                                                   |
 +-----------v-----------+                           +-----------v-----------+
 |    JessX Broker       |                           |   Paper Broker        |
 | (org.eclipsetrader.   |                           | (org.eclipsetrader.   |
 |        jessx)         |                           |    brokers.paper)     |
 +-----------+-----------+                           +-----------+-----------+
             |                                                   |
 +-----------v-----------+                           +-----------v-----------+
 | Socket Server (6290)  |                           | Local Simulated       |
 | Full Matching Engine  |                           | Order Matching        |
 +-----------------------+                           +-----------------------+
```

---

## Active Connectors

### 1. JESSX Broker & Streaming Feed (`org.eclipsetrader.jessx`)
*   **Role**: Primary active simulation broker and feed connector in this repository.
*   **Broker Implementation (`BrokerConnector.java`)**: Translates `IOrder` requests into JDOM `<Operation>` elements (`LimitOrder`, `MarketOrder`, `DeleteOrder`) and sends them to the JESSX simulation server.
*   **Launcher Integration (`BrokerLauncher.java`)**: Implements `ILauncher` to control JESSX server lifecycle from the UI "Play" button (`StartFeedAction`).
*   **Feed Connector (`StreamingConnector.java`)**: Maintains TCP streaming binary socket connection to deliver live order book updates, quotes, trade executions, and chart updates.

### 2. Paper Trading Broker (`org.eclipsetrader.brokers.paper`)
*   **Role**: Zero-network simulated broker for offline testing.
*   **Execution Engine**: Simulates order fills locally against last known market prices or user-defined bid/ask quotes without external server connection.

---

## Deactivated Legacy Connectors

The repository contains several legacy broker and feed connectors from the original EclipseTrader project. These connectors are deactivated in `pom.xml` and `feature.xml` but retained in the source tree for reference:

### 1. Directa Broker & Feed (`org.eclipsetrader.directa`, `org.eclipsetrader.directaworld`)
*   **Status**: Deactivated.
*   **Description**: Legacy integration for Italian broker Directa (FlashBook, Darwin API). Uses proprietary socket streaming and HTML parsing.

### 2. Yahoo Finance Feed (`org.eclipsetrader.yahoo`)
*   **Status**: Deactivated.
*   **Description**: Legacy market feed that fetched delayed quotes and historical CSV data from Yahoo Finance HTTP endpoints. Deactivated due to API deprecation by Yahoo Finance.

### 3. Borsa Italiana & Archipelago (`org.eclipsetrader.borsaitalia`, `org.eclipsetrader.archipelago`)
*   **Status**: Deactivated.
*   **Description**: Regional quote scrapers and feed adapters for Borsa Italiana and Archipelago ECN.
