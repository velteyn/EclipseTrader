## Scope
- Port all remaining tests from `org.eclipsetrader.core.tests` into `org.eclipsetrader.core.modern.tests`.
- Keep builds stable on GitHub (Linux) without changing your existing workflow or parent build beyond already-restored bundles.

## Priorities
- ATS Engines: JavaScriptEngine/Instrument, LimitOrderFunction, MarketOrderFunction, IndicatorFunction, HasPositionFunction.
- Trading Services: AlertServiceTest, TradingServiceTest.
- Remaining Markets/Repository/View tests not yet covered.

## Approach
- Use only public APIs; avoid protected/package-private or OSGi internals.
- Replace EasyMock with simple stubs and event assertions.
- Make tests environment-neutral (no locale/timezone-sensitive expectations, numeric tolerances where needed).
- Reuse existing modern test harness (JUnit 5 + JUnit4 bridge) already configured.

## Build Stability (GitHub)
- Do not modify your existing CI workflow.
- Keep dependency resolution stable by relying on already-present workspace IUs (`org.easymock.bundle`, `org.objenesis.bundle`).
- If a specific test fails on Linux, adjust that test only (tolerance, ordering, time assumptions) to restore green.

## Delivery Steps
1. Inventory remaining legacy tests and group by feature.
2. Port ATS engine tests: implement small test doubles for pricing/order flow; assert public effects.
3. Port trading service tests: drive service via public interfaces and verify events/results.
4. Port any remaining markets/repository/view tests with the same patterns.
5. Validate locally (`mvn -DskipTests=false -pl org.eclipsetrader.core.modern.tests -am clean verify -V`).
6. If GitHub reports failures, inspect `surefire-reports` and fix affected tests.

## Acceptance Criteria
- All ported tests pass locally and in GitHub CI.
- No changes to your workflow beyond test additions.
- Product build remains successful with `jessx` included.