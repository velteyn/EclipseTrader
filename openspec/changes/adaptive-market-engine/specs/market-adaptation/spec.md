## Purpose

Defines the adaptive market behavior: how a fundamental value, procedurally generated news, an emergent trend, and trading agents combine so the simulated market produces continuous, news-reactive, trending prices.

## ADDED Requirements

### Requirement: Fundamental value drives quoting

The market SHALL maintain a fundamental value for each asset, and quoted prices SHALL be derived from that value rather than from disconnected random levels.

#### Scenario: Quotes track the fundamental value

- **WHEN** the fundamental value of an asset changes
- **THEN** subsequent quotes and trades move toward the new value

### Requirement: Procedurally generated news

News events SHALL be generated procedurally during the simulation, each carrying an asset, a sentiment (positive or negative), and a magnitude.

#### Scenario: News event is generated

- **WHEN** the simulation is running
- **THEN** news events appear over time with an associated asset, sentiment, and magnitude

### Requirement: News reprices the market

A news event SHALL shift the fundamental value of its asset by an amount proportional to its sentiment and magnitude, so the market visibly reacts.

#### Scenario: Positive news moves the price up

- **WHEN** a positive news event for an asset is generated
- **THEN** the asset's fundamental value increases and its traded price trends upward

#### Scenario: Negative news moves the price down

- **WHEN** a negative news event for an asset is generated
- **THEN** the asset's fundamental value decreases and its traded price trends downward

### Requirement: Emergent trend

The market SHALL exhibit a price trend that emerges from agent behavior (momentum and news reaction) rather than being a constant offset, and that trend SHALL be able to persist and later reverse.

#### Scenario: Trend persists across many trades

- **WHEN** the market enters an upward trend
- **THEN** prices remain elevated over many successive trades rather than reverting immediately

#### Scenario: News reverses the trend

- **WHEN** a sufficiently strong opposite news event arrives during a trend
- **THEN** the market can reverse direction

### Requirement: Continuous market-making liquidity

A market maker SHALL continuously quote both a bid and an ask around the current value so that trades occur without the player having to initiate every transaction.

#### Scenario: Trades occur without player action

- **WHEN** the simulation is running and no external order is present
- **THEN** trades still occur between market participants at a regular rate

### Requirement: Informed agents adapt to news and trend

Informed trading agents SHALL adjust their quoted prices in the direction of incoming news and the current trend, amplifying the market's reaction.

#### Scenario: Agents lean with the trend

- **WHEN** the market is trending upward
- **THEN** informed agents quote higher prices than they would in a flat market
