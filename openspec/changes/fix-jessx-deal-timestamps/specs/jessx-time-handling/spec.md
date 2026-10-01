## Purpose

Defines how JESSX deal and order timestamps are interpreted and converted to absolute dates, consistently across streaming charts and persisted trade history.

## ADDED Requirements

### Requirement: Absolute deal timestamps

JESSX deal and order timestamps that represent elapsed time within the current period SHALL be converted to absolute dates when consumed by the platform, so trades and OHLC bars carry real-world dates.

#### Scenario: Live deal with elapsed timestamp

- **WHEN** a JESSX Deal message carries an elapsed-in-period timestamp
- **THEN** the resulting trade and OHLC bar carry an absolute date near the time the deal was received rather than the epoch (1970)

#### Scenario: Already-absolute timestamp

- **WHEN** a JESSX message carries a value that is already a valid absolute epoch millis
- **THEN** the value is used as-is without conversion

### Requirement: Consistent interpretation across consumption paths

The same timestamp interpretation SHALL apply to the streaming trade feed and to persisted trade history.

#### Scenario: Persisted holding purchase date

- **WHEN** a JESSX deal is saved to the repository as a holding
- **THEN** the purchase date is an absolute date consistent with the same deal's streaming chart time

#### Scenario: Chart and holdings agree

- **WHEN** the same JESSX deal appears both on a chart and in the portfolio holdings
- **THEN** both show the same absolute timestamp

### Requirement: No epoch dates from elapsed timestamps

The platform SHALL NOT display or persist dates derived from JESSX elapsed-in-period timestamps as 1970 epoch dates.

#### Scenario: Chart shows current-era date

- **WHEN** a JESSX deal is shown on a chart
- **THEN** the tooltip and summary bar show a date in the current era, not 1970
