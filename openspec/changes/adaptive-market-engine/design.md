## Context

EclipseTrader already has a feed seam (`StreamingConnector` / `FeedSubscription` with `setTrade`, `setBook`, `setQuote`, `setTodayOHL`) that the JESSX `BrokerConnector` currently populates by parsing XML messages. The new engine only needs to produce the same kinds of events; it does not need to reuse JESSX's server, sockets, or bots.

## Goals / Non-Goals

**Goals:**

- A self-contained, in-process market engine with a full-depth matcher, an adaptive price process, and a simulated daily clock.
- Deterministic, replayable runs via a seeded random generator.
- Emit trade/depth/quote events into the existing feed seam so charts and Level2 work unchanged.

**Non-Goals:**

- FIX or other external protocol connectivity.
- Retiring or modifying the existing `org.eclipsetrader.jessx` bundle in this change.
- Game scoring, portfolio valuation, or player UI — those build on top of the engine and are tracked separately.

## Decisions

### 1. Clean-room reimplementation, not a port of webcurvesim

Reimplement the matching and market-making ideas from webcurvesim from scratch under EPL.

- **Rationale**: webcurvesim's license carries a commercial-registration clause and bundles LGPL 2.1, which is awkward for an EPL project; its code is Java 5/6 style (`Vector`, `Hashtable`, slf4j 1.5). The matching logic is well understood and small enough to rewrite cleanly with modern generics and immutable events.
- **Alternative considered**: vendor webcurvesim's `exchange`/`common` classes. Rejected for licensing and modernization reasons.

### 2. In-process engine, no sockets

Run the engine in the same JVM as EclipseTrader and deliver events via listener callbacks.

- **Rationale**: the game is single-player in one process; JESSX's `NetworkCore`/XML-over-socket machinery exists only for its multi-machine lab origin and adds latency and complexity for no benefit.
- **Alternative considered**: keep a socket boundary between engine and UI. Rejected as unnecessary.

### 3. New bundle `org.eclipsetrader.market.sim`

Put the engine in its own OSGi bundle rather than modifying `org.eclipsetrader.jessx`.

- **Rationale**: keeps JESSX intact as a fallback while the new engine proves out; isolates the new code with its own lifecycle and tests.
- **Alternative considered**: replace JESSX's `OrderMarket`/`Order` in place. Rejected — higher blast radius and loses the working JESSX path.

### 4. Price model: fundamental value with emergent trend

Maintain per-asset `Fₜ = Fₜ₋₁ + driftₜ + newsₜ + noiseₜ`, where `driftₜ` is estimated by agents from recent trades (momentum) rather than set as a fixed parameter, and `newsₜ` is a procedural shock proportional to sentiment × magnitude.

- **Rationale**: the emergent drift makes "the market adapts to news and trend" a genuine mechanism rather than a scripted curve; the market maker anchors liquidity around `Fₜ`.
- **Alternative considered**: scripted per-scenario drift. Rejected — user wants emergent, not scripted.

### 5. Agents: market maker + informed traders

A market maker quotes `bid/ask = Fₜ ± spread/2` (spread widened by volatility/trading volume), and informed agents tilt quotes toward incoming news and the estimated trend.

- **Rationale**: the market maker guarantees continuous trades (fixing the "few deals" problem); informed agents create the news/trend reaction the player observes.
- **Alternative considered**: reuse JESSX's `Discreet`/`NotDiscreet` bots on the new engine. Rejected — their spread-only quoting produces no trades.

### 6. Simulated daily calendar

Each period is a trading day on a simulated clock; trades are timestamped from that clock. The day's real-time length and the start date are engine parameters.

- **Rationale**: gives charts meaningful day boundaries and removes the elapsed-vs-epoch ambiguity the JESSX path had (`fix-jessx-deal-timestamps`).
- **Alternative considered**: wall-clock time with fast-forward. Rejected — no clean day boundaries.

### 7. Determinism

All randomness (news timing, sentiment, magnitudes, agent noise) comes from a single seeded PRNG so a scenario can be replayed identically.

- **Rationale**: essential for testing the engine and for reproducible game scenarios.
- **Alternative considered**: unseeded `Math.random()`. Rejected — not replayable.

### 8. Feed integration

Engine listeners push `Trade`/`Book`/`Quote`/`TodayOHL` into the existing `FeedSubscription` setter path (the same seam `BrokerConnector.objectReceived` uses), marshaling onto the SWT display thread for UI-safe delivery.

- **Rationale**: no new feed plumbing; charts and Level2 behave as they do today.
- **Alternative considered**: a new `IFeedConnector`. Rejected — heavier, and the subscription seam already exists.

### 9. Full order-type set with contingent (stop) orders

Support LIMIT, MARKET, and the modern set: STOP, STOP-LIMIT, TRAILING-STOP, IOC, FOK, ICEBERG, and PEGGED.

- **Contingent orders** (stop, stop-limit, trailing-stop) rest in a separate off-book trigger queue; a trigger monitor checks each trade/quote against every active trigger and submits the resulting market/limit order when crossed. Trailing stops update their trigger from the best observed price.
- **Time-in-force modifiers** (IOC, FOK) are handled by the matching loop: IOC cancels the unfilled remainder after the sweep; FOK verifies the full quantity is available before executing (no partial fill).
- **Iceberg** keeps a hidden total and a separate display quantity that replenishes as the visible portion fills.
- **Pegged** orders are re-priced on every book change relative to a reference (best bid/ask/mid) plus an offset.

- **Rationale**: a trading game needs risk-management (stop/trailing) and execution-control (IOC/FOK) tools; iceberg and pegged add realistic depth without changing the core matcher.
- **Alternative considered**: limit + market only. Rejected — the game should expose the modern order types a real trading platform offers.

### 10. Signed positions with short selling

Model positions as a signed quantity per participant per asset (positive = long, negative = short). Buys open or increase long positions (and cover shorts first); sells reduce long positions (and open shorts beyond that) up to a configured per-participant borrow limit. Borrow availability is simplified to a numeric limit rather than a locate/borrow-cost model.

- **Rationale**: short selling is a core expectation of a realistic trading game and pairs with the stop/trailing risk tools; a numeric limit keeps the engine simple.
- **Alternative considered**: forbid shorts (JESSX's current behavior, where asks are rejected without sufficient holdings). Rejected — the game should let players go short.

### 11. Leverage via cash loans with margin enforcement

Track each participant's cash balance as signed (negative = borrowed). Buying power = cash × a configurable leverage multiplier; positions require posting margin (a fraction of notional). Equity = cash + long market value − short market value. When equity drops below a maintenance-margin threshold, the engine force-liquidates positions (submits market orders to close) until equity is restored. Borrowed cash accrues interest per period. This composes with short selling: shorts borrow the asset (bounded by the borrow limit in `market-positions`), while leverage borrows cash (bounded by the margin/equity model).

- **Rationale**: leverage is the "high risk" half of high-risk trading — the margin-call/liquidation loop is the payoff. A numeric equity/threshold model keeps it deterministic and testable.
- **Alternative considered**: unlimited leverage with no margin enforcement. Rejected — no risk, no game.

## Risks / Trade-offs

- [Risk] Forced liquidation feedback loop — liquidation sells move the price, which can trigger further margin calls. → Mitigation: evaluate and liquidate in a single pass per price update, and treat cascades as an intended, gameable dynamic rather than a bug.

- [Risk] Momentum can run away into an unrealistic bubble/crash → Mitigation: cap drift magnitude and add a mean-reversion term toward a long-run anchor.
- [Risk] Engine thread and SWT UI thread synchronization (listener callbacks touching the display) → Mitigation: marshal all feed updates through `Display.asyncExec`, matching the existing connector pattern.
- [Risk] Overly aggressive informed agents flatten the spread and stop the market maker → Mitigation: size agent orders as fractions of displayed liquidity and let the market maker dominate quoting.
- [Risk] Determinism is broken by wall-clock dependencies (e.g., `System.currentTimeMillis()` in timestamps) → Mitigation: the simulated clock is the only time source inside the engine.

## Migration Plan

- Additive: a new bundle plus a small wiring point. JESSX remains selectable, so rollback is reverting the wiring change or not enabling the new engine.
- No data migration; the engine is stateless across restarts except for the seed and any scenario parameters.

## Open Questions

- Real-time duration of a simulated day (e.g., 60s vs 300s) — a tunable parameter, does not change specs or task breakdown.
