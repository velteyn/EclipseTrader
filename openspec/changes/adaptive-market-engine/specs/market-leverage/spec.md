## Purpose

Defines how participants borrow capital to trade with leverage and how margin is enforced, including buying power, margin requirements, and forced liquidation.

## ADDED Requirements

### Requirement: Leveraged buying power

A participant's maximum position size SHALL exceed their available cash by a configurable leverage multiplier.

#### Scenario: Buying beyond cash

- **WHEN** a participant has cash for one unit but a configured leverage multiplier of four
- **THEN** they may hold a position worth up to four times their cash

### Requirement: Borrowed capital

Buying beyond available cash SHALL be financed by a loan, leaving the participant's cash balance negative to represent the borrowed amount.

#### Scenario: Cash goes negative on a leveraged buy

- **WHEN** a leveraged buy exceeds available cash
- **THEN** the participant's cash balance becomes negative by the borrowed amount

### Requirement: Margin requirement

Opening a leveraged position SHALL require the participant to post margin equal to a configured fraction of the position's value.

#### Scenario: Insufficient margin rejects the order

- **WHEN** a position would require more margin than the participant can post
- **THEN** the order is rejected

### Requirement: Equity determines margin availability

A participant's equity SHALL be their cash plus the market value of long positions minus the market value of short positions, and it SHALL determine how much margin remains available.

#### Scenario: Equity falls as prices move against a position

- **WHEN** the price of a leveraged long position falls
- **THEN** the participant's equity falls by the same amount

### Requirement: Margin call and forced liquidation

When a participant's equity falls below a configured maintenance margin, their positions SHALL be force-liquidated until equity is restored.

#### Scenario: Forced liquidation on a margin call

- **WHEN** a leveraged position's loss drives equity below the maintenance margin
- **THEN** the engine force-closes positions to restore equity, realizing the loss

### Requirement: Loan interest

Borrowed cash SHALL accrue interest at a configured rate on a per-period basis.

#### Scenario: Interest accrues on a loan

- **WHEN** a participant holds a cash loan across a period boundary
- **THEN** interest is added to the borrowed amount
