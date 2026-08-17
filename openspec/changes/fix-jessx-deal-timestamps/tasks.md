## 1. Shared timestamp converter

- [x] 1.1 Add `JessxTime.toAbsoluteDate(long)` in the jessx bundle's internal package: values below the elapsed threshold (one-day constant) resolve to the arrival time, values at or above it are treated as epoch millis
- [x] 1.2 Replace the inline sub-2000 heuristic in `BrokerConnector` Deal handling with the shared converter
- [x] 1.3 Route `JessxTradeHistory` holding `PURCHASE_DATE` through the same converter
- [x] 1.4 Add unit tests for the converter (elapsed value → current-era date, epoch value → unchanged, boundary at the threshold) wired into the Maven reactor so they run in GitHub Actions CI

## 2. Verification

- [x] 2.1 Run `mvn package` on the branch (GitHub Actions `maven.yml` on JDK 21) and confirm `org.eclipsetrader.jessx` compiles and converter tests pass
- [ ] 2.2 Live-check in the running product (Codespaces virtual display or a local machine): start a JESSX simulation and confirm chart tooltips/summary show current-era timestamps, not 1970
- [ ] 2.3 Confirm a traded deal persists with a current-era purchase date in the portfolio holdings, and that chart and holdings agree (spec `jessx-time-handling`)
