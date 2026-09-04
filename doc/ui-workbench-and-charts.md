# UI Workbench & Charting System

## Overview

The EclipseTrader UI is implemented across `org.eclipsetrader.ui` and `org.eclipsetrader.ui.application`. Built on top of Eclipse RCP, SWT, and JFace, it provides customizable views, interactive technical analysis charts, and order entry dialogs.

```
+-------------------------------------------------------------------------------+
|                            ECLIPSETRADER WORKBENCH                            |
|                                                                               |
|  +--------------------+  +-------------------------------------------------+  |
|  | Watchlist View     |  | Chart View (ChartViewPart)                      |  |
|  | (Securities Table) |  | - Candlestick / Bar Charts                      |  |
|  |                    |  | - Technical Indicators (SMA, EMA, RSI, MACD)   |  |
|  +--------------------+  | - Draw2D Chart Objects & Overlays               |  |
|  | Level II View      |  +-------------------------------------------------+  |
|  | (Bid/Ask Depth)    |  | Portfolio View (Position Holdings & P&L)        |  |
|  +--------------------+  +-------------------------------------------------+  |
+-------------------------------------------------------------------------------+
```

---

## Core UI Components & Views

### 1. Portfolio View (`PortfolioViewPart.java`)
*   Displays active user positions, cost basis, current market value, and unrealized profit & loss (P&L).
*   Driven by `PortfolioContentProvider`: Listens to `PortfolioChangeEvent` from the core `Account` / `Portfolio` model to auto-refresh table rows on trade execution.

### 2. Level II / Market Depth View
*   Provides real-time visibility into the order book for the selected security.
*   Displays bid and ask depth queues, order sizes, and price levels received from the active feed (e.g., JESSX streaming socket).
*   Header area displays real-time summary data: Last Price, Volume, High, Low, Change.

### 3. Order Entry Dialog (`OrderDialog.java`)
*   Modal order dialog for submitting Buy/Sell orders to the broker engine.
*   Pulls tradable routes (`IOrderRoute`) from the selected broker connector (`getBrokerForSecurity`).
*   Auto-populates target security symbol using broker resolution (`getSymbolFromSecurity`).

---

## Charting Engine (`org.eclipsetrader.ui.charts.indicators`)

The charting engine provides technical analysis visualization and overlay management:

### 1. Chart View Part (`ChartViewPart.java`)
*   Asynchronously loads historical price data (`IOHLC[]`) from `IHistory` using background `ChartLoadJob`.
*   Listens to real-time property change events on `IHistory`: When live trade ticks arrive from `StreamingConnector`, new OHLC bar values update the chart in real time.

### 2. Chart Rendering Architecture
*   **Drawing Library**: Uses Draw2D / SWT GC graphics for rendering chart panels, gridlines, axes, and price bars.
*   **Extensible Chart Objects**: Uses `IChartObjectFactory` and `IChartObject` extension points to dynamically render:
    *   Price Series (Candlesticks, OHLC Bars, Line Charts).
    *   Volume Bars.
    *   Technical Indicators (SMA, EMA, RSI, MACD, Bollinger Bands, Stochastic).
    *   Chart Tooltips (`SummaryOHLCItem`): Displays high/low/open/close metrics on mouse hover.

---

## UI Framework Migration & Legacy Widget Status

*   **Nebula Widget Clean-up**: Historical dependencies on legacy Nebula SWT components (`pshelf`, `cwt`, `cdatetime`, `grid`) have been removed due to compatibility issues with modern Eclipse target platforms.
*   **SWT DateTime**: Date and time pickers rely on standard SWT `DateTime` widgets.
*   **Custom Presentation Factory (`TraderPresentationFactory.java`)**: Legacy custom workbench layout presentation factory was commented out due to reliance on internal non-public Eclipse 3.x APIs; standard Eclipse RCP workbench renderer is used instead.
