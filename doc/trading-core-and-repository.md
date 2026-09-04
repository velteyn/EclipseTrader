# Trading Core Domain Model & Persistence Layer

## Overview

The core domain model and persistence framework are defined in `org.eclipsetrader.core` and its repository provider plugins (`org.eclipsetrader.repository.local`, `org.eclipsetrader.repository.hibernate`).

```
                      +---------------------------------------+
                      |         IRepositoryService            |
                      +-------------------+-------------------+
                                          |
                +-------------------------+-------------------------+
                |                                                   |
    +-----------v-----------+                           +-----------v-----------+
    |    LocalRepository    |                           |   HibernateRepository |
    |    (XML Storage)      |                           |   (Derby / PostgreSQL)|
    +-----------+-----------+                           +-----------+-----------+
                |                                                   |
                +-------------------------+-------------------------+
                                          |
                        +-----------------v-----------------+
                        |        CORE DOMAIN MODELS         |
                        |  ISecurity, IOrder, IAccount,     |
                        |  IPortfolio, IMarket, IHistory    |
                        +-----------------------------------+
```

---

## Core Domain Models (`org.eclipsetrader.core`)

### 1. Security Abstraction (`ISecurity` & `Stock`)
*   Represents a tradable financial asset (Stock, ETF, Future, Forex).
*   Holds properties: `Identifier`, `Name`, `Currency` (`java.util.Currency`), `FeedProperties` (`ISecurity.getFeedProperties()`), and `OrderRoutes`.
*   Associated with historical price data via `IHistory`.

### 2. Account & Portfolio (`IAccount` & `IPortfolio`)
*   **Account (`Account.java`)**: Represents a trading account holding cash balance, currency, broker assignment, and linked portfolio.
*   **Portfolio (`Portfolio.java`)**: Manages real-time position holdings (`IPosition`). Fires granular change events (`PortfolioChangeEvent`) on position additions, modifications, or liquidations to update UI views (`PortfolioViewPart`).

### 3. Orders & Routing (`IOrder` & `IOrderRoute`)
*   **Order Object**: Captures trade parameters (`Side` BUY/SELL, `Type` LIMIT/MARKET, `Quantity`, `Price`, `Security`, `Account`).
*   **Order Routing**: Allows selecting specific execution routes (e.g., JESSX Institutions, Paper Trading engine) dynamically via `IOrderRoute`.

### 4. Markets & History (`IMarket` & `IHistory`)
*   **Market (`Market.java`)**: Defines trading schedules, open/close times, timezone, and calendar holiday rules.
*   **History (`History.java`)**: Read-only historical price storage holding arrays of `IOHLC` (Open, High, Low, Close, Volume) bar records. Updated dynamically during live feeds by re-allocating `IOHLC` bar arrays.

---

## Persistence Architecture (`IRepositoryService`)

EclipseTrader abstracts persistence through the `IRepositoryService` OSGi service interface:

### 1. Service Lookup & Execution
*   **Service Registration**: Repositories register via the `org.eclipsetrader.core.repositories` extension point.
*   **Safe Execution (`runInService`)**: All multi-step persistence writes (adding securities, saving trades) must be executed within `IRepositoryRunnable` closures passed to `repositoryService.runInService()` to ensure atomic transactions and prevent state lockups.

### 2. Local XML Repository (`org.eclipsetrader.repository.local`)
*   Default zero-dependency local file persistence engine.
*   Stores data in XML format in workspace metadata:
    *   `securities.xml`: Registered securities.
    *   `watchlists.xml`: User watchlists and custom groupings.
    *   `trades.xml`: Execution history and trade journals.
    *   `alerts.xml`: User price alert triggers.

### 3. Hibernate ORM Repository (`org.eclipsetrader.repository.hibernate`)
*   Relational database backend using Hibernate ORM.
*   **Database Fragments**:
    *   `org.eclipsetrader.repository.hibernate.derby`: Embedded Apache Derby database provider.
    *   `org.eclipsetrader.repository.hibernate.postgresql`: Enterprise PostgreSQL provider.
*   Mapped persistence entities: `TradeStore`, `SecurityStore`, `AccountStore`.
