## Purpose

Defines how the market tracks participant positions, including short positions, so a trader may sell an asset they do not currently hold.

## ADDED Requirements

### Requirement: Signed positions

A participant's position in an asset SHALL be a signed quantity: positive for a long position and negative for a short position.

#### Scenario: Position is negative when short

- **WHEN** a participant sells more of an asset than they hold
- **THEN** their position in that asset becomes negative

### Requirement: Short selling is permitted

A sell order SHALL be accepted and executed even when the participant holds insufficient or no assets, creating or increasing a short position.

#### Scenario: Sell without holdings

- **WHEN** a participant submits a sell order with no existing holdings
- **THEN** the order executes and opens a short position

### Requirement: Short position limit

A participant's short position in an asset SHALL be bounded by a configured limit, beyond which further sell orders are rejected.

#### Scenario: Short limit blocks excess selling

- **WHEN** a sell order would push a participant's short position past its limit
- **THEN** the order is rejected

### Requirement: Buys cover shorts

A buy order SHALL first reduce an existing short position before increasing a long position.

#### Scenario: Covering a short

- **WHEN** a participant with a short position buys the asset
- **THEN** their position moves toward (and past) zero, covering the short

### Requirement: Opening a long position

A buy order SHALL open or increase a long position when the participant has no short position to cover.

#### Scenario: Buying while flat opens a long

- **WHEN** a participant with no position buys an asset
- **THEN** their position becomes positive, opening a long position

### Requirement: Selling reduces a long position

A sell order SHALL reduce an existing long position before opening a short position.

#### Scenario: Selling from a long

- **WHEN** a participant sells an asset they hold
- **THEN** their long position decreases; selling more than held opens a short for the excess

### Requirement: Trades update both sides' positions

Each trade SHALL adjust the buyer's position by the traded quantity and the seller's position by the negative traded quantity.

#### Scenario: Position update on a trade

- **WHEN** a trade executes for a quantity
- **THEN** the buyer's position increases by that quantity and the seller's decreases by it
