## Purpose

Defines how chart rendering behaves across display scales and dataset sizes: crisp device-zoom-aware output, cached geometry, and bounded work per repaint on large histories.

## ADDED Requirements

### Requirement: Device-zoom-aware rendering

The chart rendering SHALL recreate the offscreen image when the display's zoom factor (DPI scaling) changes, so chart output stays crisp across display-scale changes.

#### Scenario: High-zoom display

- **WHEN** a chart is displayed on a device with a zoom factor greater than 100% (e.g., 200%)
- **THEN** candles, bars, lines, grid, and axis text render at the correct scaled size without blur or aliasing artifacts

#### Scenario: Zoom change after image creation

- **WHEN** the display zoom factor changes while a chart is displayed (e.g., the window moves to a monitor with a different scale)
- **THEN** the offscreen image is recreated at the new zoom and the chart renders crisply

#### Scenario: Standard display unchanged

- **WHEN** a chart is displayed on a device with a zoom factor of 100%
- **THEN** rendering is equivalent in content and layout to current behavior

### Requirement: Geometry caching

Chart objects SHALL reuse cached point geometry across repaints and SHALL recompute geometry only when the underlying data or visible bounds change.

#### Scenario: Repaint without data change

- **WHEN** a chart repaints (for example on focus change or redraw request) without new data or a bounds change
- **THEN** the previously computed point geometry is reused and no full geometry rebuild occurs

#### Scenario: Data or bounds change invalidates cache

- **WHEN** new data arrives or the visible date range changes
- **THEN** the cached geometry is invalidated and recomputed on the next repaint

### Requirement: Downsampling of large series

When the number of data points in the visible range exceeds the available horizontal pixels, the renderer SHALL aggregate points per pixel column so the number of drawn elements stays bounded by the chart width and rendering remains responsive.

#### Scenario: Zoomed-out large history

- **WHEN** a series with more points than horizontal pixels is displayed
- **THEN** the chart draws at most one aggregate element (candle, bar, or point) per pixel column using a min/max style aggregation

#### Scenario: Small history unchanged

- **WHEN** the visible range contains fewer points than horizontal pixels
- **THEN** every data point is drawn individually without aggregation

### Requirement: Antialiased drawing

All chart drawing SHALL be performed with shape and text antialiasing enabled.

#### Scenario: Chart rendering quality

- **WHEN** a chart is drawn
- **THEN** diagonal lines and text edges are antialiased rather than jagged
