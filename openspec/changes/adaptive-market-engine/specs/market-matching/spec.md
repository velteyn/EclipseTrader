## Purpose

Defines the matching behavior of the simulated market's limit order book, so orders execute the way a real continuous double auction does.

## ADDED Requirements

### Requirement: Price-time priority

Orders in the book SHALL queue by price then by arrival time: the best-priced order trades first, and among orders at the same price the earliest-arrived trades first.

#### Scenario: Better price trades first

- **WHEN** a new order can match multiple resting orders at different prices
- **THEN** it trades against the best-priced resting order before any worse-priced resting order

#### Scenario: Same-price orders trade in arrival order

- **WHEN** two resting orders share the same price
- **THEN** the one that arrived earlier is matched first

### Requirement: Full-depth execution

A single incoming order SHALL be able to trade against multiple resting orders across successive price levels until it is filled or no matching price remains.

#### Scenario: Large order sweeps multiple levels

- **WHEN** an incoming order's quantity exceeds the quantity available at the best price
- **THEN** the remainder continues to match the next price level, generating one trade per level crossed

### Requirement: Market orders sweep the book

A market order SHALL execute at whatever prices are available on the opposite side, filling as much quantity as the book provides.

#### Scenario: Market buy exhausts available asks

- **WHEN** a market buy order's quantity is larger than the resting ask quantity at the best price
- **THEN** it fills across the ask levels until the order is complete or the ask side is empty

#### Scenario: Unfilled market order is cancelled

- **WHEN** a market order cannot be fully filled because the opposite side is empty
- **THEN** its unfilled remainder is cancelled rather than resting in the book

### Requirement: Limit order remainder rests in the book

A limit order's unfilled quantity SHALL rest in the book at its limit price and remain eligible for future matching.

#### Scenario: Partially filled limit order rests

- **WHEN** a limit order is partially filled by an incoming opposite order
- **THEN** the unfilled quantity stays in the book at its limit price

### Requirement: Order amendment and cancellation

A resting order SHALL support cancellation and amendment of price and/or quantity, with amended orders re-queued according to the new price and, when the quantity increases, treated as a new arrival for the added quantity.

#### Scenario: Cancel removes a resting order

- **WHEN** a resting order is cancelled
- **THEN** it is removed from the book and no longer eligible for matching

#### Scenario: Price amendment re-queues the order

- **WHEN** a resting order's price is amended
- **THEN** it is re-queued at the new price with the amended quantity

### Requirement: Every match emits a trade

Each execution between a buy and a sell order SHALL produce a trade record carrying price, quantity, and the two counterparties.

#### Scenario: Trade record on a match

- **WHEN** an incoming order matches a resting order
- **THEN** a trade is emitted with the matched price, matched quantity, and the buy and sell counterparties

### Requirement: Stop orders

A stop order SHALL rest off-book until the market price crosses its trigger price, at which point it SHALL become a market order.

#### Scenario: Stop triggers on a falling market

- **WHEN** a sell stop order's trigger is crossed by a falling market price
- **THEN** the stop order becomes a market order and executes at the available price

#### Scenario: Stop stays inactive below trigger

- **WHEN** the market price has not crossed a stop order's trigger
- **THEN** the stop order does not rest in the visible book and does not trade

### Requirement: Stop-limit orders

A stop-limit order SHALL rest off-book until its trigger is crossed, then become a limit order at its specified limit price.

#### Scenario: Stop-limit becomes a limit order

- **WHEN** a stop-limit order's trigger is crossed
- **THEN** it enters the book as a limit order at its limit price and is matched under normal limit rules

### Requirement: Trailing stop orders

A trailing stop order SHALL maintain a trigger that follows the market by a fixed offset from the best price observed since activation, so the trigger moves in the favorable direction and stays put otherwise.

#### Scenario: Trailing stop follows a rising market

- **WHEN** a sell trailing stop is active and the market price rises
- **THEN** its trigger rises with the market while preserving the configured offset

#### Scenario: Trailing stop triggers on reversal

- **WHEN** the market reverses by more than the trailing offset from the best observed price
- **THEN** the trailing stop triggers and becomes a market order

### Requirement: Immediate-or-cancel orders

An immediate-or-cancel (IOC) order SHALL execute immediately against available liquidity and any unfilled remainder SHALL be cancelled.

#### Scenario: IOC partial fill

- **WHEN** an IOC order can be only partially filled from the book
- **THEN** the filled portion trades and the remainder is cancelled, never resting in the book

### Requirement: Fill-or-kill orders

A fill-or-kill (FOK) order SHALL execute immediately in its entirety or be cancelled with no fill; it SHALL NOT partially fill.

#### Scenario: FOK cancels when not fully fillable

- **WHEN** the book cannot fill an FOK order's full quantity immediately
- **THEN** the order is cancelled without any trade

#### Scenario: FOK fills fully

- **WHEN** the book can fill an FOK order's full quantity immediately
- **THEN** the entire quantity executes

### Requirement: Iceberg orders

An iceberg order SHALL expose only a display quantity in the book and SHALL replenish that display quantity from a hidden total as the visible portion is consumed, until the hidden total is exhausted.

#### Scenario: Iceberg replenishes after a fill

- **WHEN** the displayed quantity of an iceberg order is consumed by a trade
- **THEN** a new display quantity is revealed from the hidden total until it is exhausted

### Requirement: Pegged orders

A pegged order SHALL track a reference price (best bid, best ask, or mid) at a fixed offset and SHALL reprice when the reference moves.

#### Scenario: Pegged order reprices with the book

- **WHEN** the reference price of a pegged order changes
- **THEN** the pegged order's price is updated to the new reference plus its offset
