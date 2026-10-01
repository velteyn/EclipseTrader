## Why

The JESSX trading game sends deal/order timestamps as elapsed milliseconds within the current period (`ExperimentManager.getTimeInPeriod()` = `now - periodBeginning`), not epoch milliseconds. EclipseTrader misreads them as epoch millis and feeds them into `new Date(...)`, so streaming trades and persisted holdings show 1970 dates on charts. The closed PR #18 patched only the streaming path in `BrokerConnector` with a "before year 2000 → use now" heuristic; the persistence path (`JessxTradeHistory`) still writes 1970 purchase dates.

## What Changes

- Introduce a single, shared interpretation of JESSX deal/order timestamps so elapsed-in-period values are converted to absolute dates instead of being misread as epoch millis.
- Apply the conversion consistently at both consumption points:
  - `BrokerConnector` streaming trade feed (charts/OHLC history).
  - `JessxTradeHistory` persisted holdings (portfolio purchase dates).
- Absorb and replace the PR #18 workaround; the heuristic moves into the shared converter (or is superseded by a cleaner derivation) rather than living inline in `BrokerConnector`.

## Capabilities

### New Capabilities

- `jessx-time-handling`: how JESSX deal/order timestamps are interpreted and converted to absolute times, applied consistently across the streaming feed and persisted trade history.

### Modified Capabilities

None — this repo has no existing specs yet; this capability is new.

## Impact

- **Code**: `org.eclipsetrader.jessx` — `BrokerConnector` (streaming `Deal` handling) and `JessxTradeHistory` (holding persistence). A shared timestamp converter utility.
- **Behavior**: charts no longer show 1970 timestamps; portfolio holdings get correct purchase dates. No change for non-JESSX data feeds.
- **Dependencies**: none new.
- **Related work**: independent of the chart modernization change; this stays out of the merged PR #18 bug-fix extraction.
