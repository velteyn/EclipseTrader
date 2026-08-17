## 1. Bundle scaffold

- [ ] 1.1 Create `org.eclipsetrader.market.sim` OSGi bundle (MANIFEST.MF, build.properties, pom.xml, plugin activator) and add it to the Maven reactor
- [ ] 1.2 Add the bundle to the product feature/`plugin_customization.ini` so it is included in builds

## 2. Matching engine

- [ ] 2.1 Implement `Order`/`Trade` value types with side, type, quantity, price, trigger, display quantity, and fill state
- [ ] 2.2 Implement the order book with price-time priority insertion (binary search) for bids and asks
- [ ] 2.3 Implement full-depth matching: a crossing order sweeps successive price levels, emitting one trade per level (spec `market-matching`)
- [ ] 2.4 Implement market-order semantics (sweep until filled, cancel unfilled remainder) (spec `market-matching`)
- [ ] 2.5 Implement cancel and amend (price and quantity re-queue rules) (spec `market-matching`)
- [ ] 2.6 Compute per-asset book statistics: best bid/ask, level volume, last trade, VWAP
- [ ] 2.7 Implement IOC and FOK time-in-force execution (immediate-or-cancel, fill-or-kill with no partial) (spec `market-matching`)
- [ ] 2.8 Implement stop and stop-limit orders with an off-book trigger monitor (spec `market-matching`)
- [ ] 2.9 Implement trailing stop orders (offset that follows the best observed price) (spec `market-matching`)
- [ ] 2.10 Implement iceberg orders (display quantity replenished from a hidden total) (spec `market-matching`)
- [ ] 2.11 Implement pegged orders (reprice to best bid/ask/mid plus offset) (spec `market-matching`)

## 3. Positions and short selling

- [ ] 3.1 Implement signed position accounting (long/short) updated on every trade (spec `market-positions`)
- [ ] 3.2 Implement short-selling validity: sells beyond holdings allowed up to a configured short limit, rejected beyond it (spec `market-positions`)
- [ ] 3.3 Implement buy-to-cover semantics: buys reduce shorts before opening longs (spec `market-positions`)
- [ ] 3.4 Implement the symmetric long path: buys open/increase longs, sells reduce longs before shorting (spec `market-positions`)

## 4. Leverage and margin

- [ ] 4.1 Implement a signed cash balance with borrowed-capital tracking (negative cash = loan) (spec `market-leverage`)
- [ ] 4.2 Implement leveraged buying power and margin-requirement checks (spec `market-leverage`)
- [ ] 4.3 Implement equity computation (cash + long market value − short market value) (spec `market-leverage`)
- [ ] 4.4 Implement margin-call detection and forced liquidation to restore equity (spec `market-leverage`)
- [ ] 4.5 Implement loan interest accrual per period (spec `market-leverage`)

## 5. Price process, news, and agents

- [ ] 5.1 Implement the fundamental value process `Fₜ = Fₜ₋₁ + driftₜ + newsₜ + noiseₜ` with a seeded PRNG (spec `market-adaptation`)
- [ ] 5.2 Implement procedural news generation: asset, sentiment, and magnitude drawn from the seeded PRNG on a configurable schedule
- [ ] 5.3 Implement the market maker that quotes `bid/ask = Fₜ ± spread/2` and sizes orders from displayed liquidity (spec `market-adaptation`)
- [ ] 5.4 Implement informed agents that tilt quotes toward recent trend and incoming news (spec `market-adaptation`)
- [ ] 5.5 Implement emergent drift estimation from recent trades with magnitude cap and mean reversion (spec `market-adaptation`)

## 6. Simulated daily calendar

- [ ] 6.1 Implement the simulated clock and daily-period scheduler (open/close, day advance) (spec `market-calendar`)
- [ ] 6.2 Timestamp all orders/trades/quotes from the simulated clock (no `System.currentTimeMillis()` in the engine) (spec `market-calendar`)
- [ ] 6.3 Implement day-boundary behavior: stop order entry, advance the day, reopen for the next day

## 7. Feed integration

- [ ] 7.1 Emit `Trade`/`Book`/`Quote`/`TodayOHL` events from engine listeners into the existing `FeedSubscription` seam
- [ ] 7.2 Marshal feed updates onto the SWT display thread (`Display.asyncExec`) for UI safety
- [ ] 7.3 Wire the engine lifecycle (start/stop with the simulated day) and expose a seeded scenario entry point

## 8. Tests and verification

- [ ] 8.1 Unit-test matching: price-time priority, full-depth sweep, market exhaust/cancel, cancel/amend, stop/stop-limit triggers, trailing stop, IOC/FOK, iceberg replenishment, pegged repricing (spec `market-matching`)
- [ ] 8.2 Unit-test positions: signed positions, long open/close, short selling, short limit rejection, buy-to-cover (spec `market-positions`)
- [ ] 8.3 Unit-test leverage: buying power, negative cash loan, margin rejection, margin call + forced liquidation, loan interest (spec `market-leverage`)
- [ ] 8.4 Unit-test adaptation: news moves the price, trend persists and reverses, trades occur without player action (spec `market-adaptation`)
- [ ] 8.5 Unit-test calendar: simulated timestamps, no 1970 dates, day boundary close/reopen (spec `market-calendar`)
- [ ] 8.6 Wire `org.eclipsetrader.market.sim.tests` into the Maven reactor so tests run in GitHub Actions CI
- [ ] 8.7 Run `mvn package` and confirm the new bundle compiles and tests pass
- [ ] 8.8 Visually verify charts show continuous trending trades with daily timestamps (simulated via the devcontainer virtual display)
