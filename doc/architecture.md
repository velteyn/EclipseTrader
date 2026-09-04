# System Architecture & Plugin Infrastructure

## Overview

EclipseTrader is built on top of the **Eclipse Rich Client Platform (RCP)** framework using OSGi bundle modularity. The application is composed of multiple OSGi plug-ins, features, and releng modules managed via a **Maven/Tycho** build reactor.

```
                  +-----------------------------------+
                  |   org.eclipsetrader.releng        |
                  | (Tycho Build & RCP Product Def)   |
                  +-----------------+-----------------+
                                    |
                  +-----------------v-----------------+
                  |     org.eclipsetrader.ui          |
                  |   org.eclipsetrader.ui.application|
                  +-------+-------------------+-------+
                          |                   |
       +------------------v--+             +--v------------------+
       | org.eclipsetrader.  |             | org.eclipsetrader.  |
       |       jessx         |             |       core          |
       +----------+----------+             +----------+----------+
                  |                                   |
                  |                                   |
       +----------v----------+             +----------v----------+
       |   org.jdom / XML    |             | org.eclipsetrader.  |
       | Network Sockets     |             | repository.hibernate|
       +---------------------+             +---------------------+
```

---

## Core Architecture Components

### 1. OSGi Plug-in Modular Architecture
The system decouples core trading abstractions, UI components, persistence mechanisms, and market feed connectors into distinct OSGi bundles (`META-INF/MANIFEST.MF`):

*   **Core Logic (`org.eclipsetrader.core`)**: Defines central abstractions (`ISecurity`, `IOrder`, `IAccount`, `IPortfolio`, `IMarket`, `IHistory`).
*   **UI Application (`org.eclipsetrader.ui`, `org.eclipsetrader.ui.application`)**: Standard Workbench layout, perspective definitions, command handlers, and JFace viewers.
*   **Simulation Engine (`org.eclipsetrader.jessx`)**: Embedded JESSX market simulation server, socket protocol handlers, scenario parsers, and trading bot engines.
*   **Persistence (`org.eclipsetrader.repository.local`, `org.eclipsetrader.repository.hibernate`)**: Data persistence abstractions using XML files or database backends (Derby, PostgreSQL).
*   **Legacy Connectors (`org.eclipsetrader.directa`, `org.eclipsetrader.yahoo`)**: Deactivated broker and market data feed implementations.

---

## Extension Point Architecture

EclipseTrader leverages Eclipse extension points (`plugin.xml`) to allow dynamic pluggability:

*   `org.eclipsetrader.core.repositories`: Registers storage backends (Local XML, Hibernate).
*   `org.eclipsetrader.core.brokers`: Registers order execution brokers (JessX, Paper Broker).
*   `org.eclipsetrader.core.launchers`: Registers startup/shutdown background services (e.g., `BrokerLauncher`, `FeedServiceLauncher`).
*   `org.eclipsetrader.core.charts`: Registers chart indicators, overlays, and rendering factories (`IChartObjectFactory`).
*   `org.eclipsetrader.core.markets`: Defines default exchange calendar, operating hours, and rules.

---

## Build System: Tycho & Target Platform

### Tycho Build reactor (`pom.xml`)
The repository uses **Eclipse Tycho** to build OSGi bundles directly from `MANIFEST.MF` and `feature.xml` definitions.

*   **Parent POM (`pom.xml`)**: Defines target environment platform rules (`<os>win32</os>`, `<ws>win32</ws>`, `<arch>x86_64</arch>`) and reactor modules.
*   **Target Platform (`org.eclipsetrader.releng/eclipsetrader.target`)**: Resolves dependencies against Eclipse SimRel (e.g. Eclipse Juno / 2024-03 p2 repositories).
*   **Product Definition (`org.eclipsetrader.releng/eclipsetrader.product`)**: Packages the RCP application into a standalone binary distribution containing `trader.exe`.

### Runtime Setup & Execution
*   **Java Runtime Compliance**: Compiled against Java 11 / Java 8 target platform configuration.
*   **Target Execution**: Executed via Equinox OSGi framework launcher (`org.eclipse.equinox.launcher`).
