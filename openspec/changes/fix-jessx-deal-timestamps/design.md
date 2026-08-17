## Context

- See `proposal.md` for motivation and `specs/jessx-time-handling/spec.md` for the behavioral requirements.
- JESSX sends deal/order timestamps as *elapsed milliseconds within the current period* (`ExperimentManager.getTimeInPeriod()` = `now - periodBeginning`). EclipseTrader currently feeds them straight into `new Date(...)`, producing 1970 dates.
- Two consumption points in `org.eclipsetrader.jessx`:
  1. `BrokerConnector` — streaming `Deal` → `Trade` + OHLC history (this is what the closed PR #18 patched inline with a sub-2000 heuristic).
  2. `JessxTradeHistory` — persisted holding `PURCHASE_DATE` (still broken, unchanged by PR #18).

## Goals / Non-Goals

**Goals:**
- Absolute, current-era dates for JESSX deals/orders at both consumption points.
- One shared conversion so streaming charts and persisted holdings can never diverge.
- Supersede PR #18's inline workaround.

**Non-Goals:**
- Changing what the JESSX server sends (the server is correct to send elapsed time; it is the client's interpretation that is wrong).
- Reconstructing the exact simulated wall-clock time from period start (live deals arrive in real time; arrival time is accurate enough).
- Migrating already-persisted 1970 holdings from old repositories.

## Decisions

### 1. Single shared converter with a magnitude-based interpretation

Add one helper (e.g., `JessxTime.toAbsoluteDate(long timestamp)` in a new internal utility class) used by both `BrokerConnector` and `JessxTradeHistory`:

- **Below threshold** (elapsed-in-period value, bounded by period duration, e.g. `< 86,400,000 ms` — one day): interpret as elapsed → return arrival time (`new Date(System.currentTimeMillis())`).
- **At or above threshold** (a plausible epoch millis): return `new Date(timestamp)` unchanged.

The threshold is the maximum plausible period duration. Unlike PR #18's "before year 2000" check, this correctly preserves genuine epoch values while catching all realistic elapsed values (which are at most a few hours, i.e. millions of ms, never billions).

- **Rationale**: deals are received live, so `arrival time ≈ periodStart + elapsed` within network latency. Tracking period start on the client would add state and message plumbing for no observable gain.
- **Alternative considered**: track `periodBeginning` on the client and compute `periodStart + elapsed`. Rejected — requires new state and event handling across the feed, and its output equals arrival time in practice.

### 2. Apply at both consumption points

- `BrokerConnector` Deal handling: replace the inline PR #18 heuristic with `JessxTime.toAbsoluteDate(...)`.
- `JessxTradeHistory`: route `finalDeal.getTimestamp()` through the same converter before `IPropertyConstants.PURCHASE_DATE` is set.

### 3. Keep OHLC bar-merging behavior as-is

The existing merge condition (`last.getDate().equals(tradeData.getTime())`) is left untouched. Consecutive deals get distinct arrival timestamps (ms precision), so each becomes its own bar — this matches current behavior and satisfies the spec's "current-era date" requirement.

## Risks / Trade-offs

- **[Risk] Threshold heuristic misclassifies an extreme value** → an elapsed period longer than one day would be misread as epoch. Mitigation: threshold is a single named constant, documented; simulation period durations in JESSX are far smaller. Can be tuned without API change.
- **[Risk] Arrival time instead of true sim time on persisted holdings** → purchase dates are "when the platform received the deal", not the simulated timestamp. Accepted for a live simulation; consistent between chart and holdings (which is what the spec requires).
- **[Risk] Pre-existing 1970 holdings remain in repositories** → out of scope; only new deals are corrected. Flagged in the migration plan so it is not mistaken for a regression.

## Migration Plan

- No data migration. Existing broken holdings stay as-is (documented limitation); newly saved deals get correct dates.
- Rollback: revert the change — the converter is additive and localized to the two call sites.
- This change is independent of `modernize-chart-rendering` and of the extracted PR #18 bug fixes.
