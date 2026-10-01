## Why

EclipseTrader's bundled JESSX simulator produces a thin, unrealistic market: its matcher only ever matches a single best order, its bots place non-crossing orders inside the spread, and its news is a static scenario script. The result is very few trades, no price trend, and a market that never adapts to events — which defeats the purpose of a trading game in which the player adapts to a real, news-reactive market.

## What Changes

- Add a new OSGi bundle (`org.eclipsetrader.market.sim`) containing a clean-room, full-depth limit-order-book matching engine (inspired by webcurvesim's core, written under EPL).
- Add a procedural market layer: a fundamental-value process with an emergent trend regime, procedurally generated news (sentiment + magnitude), and adaptive agents (a market maker plus informed traders) that quote and trade continuously.
- Add a simulated daily market calendar: each period is a trading day; orders and trades carry simulated daily timestamps that flow into the existing EclipseTrader feed seam so charts show meaningful day boundaries.
- Support signed positions and short selling: participants may sell assets they do not hold up to a borrow limit, and buys cover existing shorts.
- Support leveraged trading via cash loans: buying power beyond available cash, margin requirements, and forced liquidation when equity falls below the maintenance margin.
- Integrate the engine with the existing feed pipeline (`StreamingConnector`/`FeedSubscription`) so trades, depth, and quotes render in charts and the Level2 view.
- Leave the existing JESSX bundle intact in this change.

## Capabilities

### New Capabilities

- `market-matching`: the limit-order-book matching semantics — limit, market, stop, stop-limit, trailing-stop, IOC, FOK, iceberg, and pegged orders; price-time priority; full-depth execution; amend/cancel; VWAP; and price steps.
- `market-adaptation`: the adaptive market behavior — procedural news, the emergent trend regime, and the market-maker/informed agents that turn news and trend into price movement.
- `market-calendar`: the simulated daily calendar — daily periods, simulated timestamps, and how they map onto the feed.
- `market-positions`: position tracking and short selling — signed positions, short limits, and buy-to-cover semantics.
- `market-leverage`: leveraged trading — cash loans, buying power, margin requirements, and forced liquidation.

### Modified Capabilities

None.

## Impact

- **Code**: new bundle `org.eclipsetrader.market.sim` (matching engine, agents, news generator, calendar). Wiring into the existing feed seam that `StreamingConnector`/`FeedSubscription` already expose.
- **Behavior**: charts and Level2 show continuous, trending, news-reactive simulated market data with daily timestamps; player orders interact with the same book.
- **Dependencies**: none new (plain Java plus existing EclipseTrader feed APIs).
- **Related work**: supersedes the sparse-trade behavior of JESSX and the `fix-jessx-deal-timestamps` elapsed/epoch workaround (the new engine controls its own clock).
