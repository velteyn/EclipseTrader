## Purpose

Provides a single source of truth for chart colors so the palette is maintainable, dark mode is possible, and user-saved color preferences keep working.

## ADDED Requirements

### Requirement: Centralized chart palette

All chart colors (positive, negative, outline, line, grid, and background) SHALL be sourced from a theme provider rather than inline color literals inside chart objects.

#### Scenario: Default-theme chart

- **WHEN** a chart object is created without explicitly configured colors
- **THEN** it uses the colors defined by the active theme

#### Scenario: Theme palette change

- **WHEN** the active theme's palette is updated
- **THEN** charts rendering with theme colors reflect the new colors on their next repaint

### Requirement: User color preferences override theme

User-saved chart color preferences SHALL take precedence over theme defaults for the configured chart.

#### Scenario: Custom candle colors

- **WHEN** a user has saved custom candle colors for a chart
- **THEN** the chart renders with exactly those colors regardless of the active theme

#### Scenario: Reverting to theme colors

- **WHEN** a chart has no custom colors saved
- **THEN** it renders with the active theme's colors

### Requirement: Theme covers every renderer

The theme provider SHALL supply colors for all supported chart renderings: candlesticks, bars, OHLC lines, and histograms.

#### Scenario: Style switch keeps theme colors

- **WHEN** a user switches a chart's rendering style (for example from candles to bars)
- **THEN** the new style uses the theme's colors appropriate to that renderer
