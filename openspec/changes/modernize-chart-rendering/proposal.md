## Why

The chart rendering pipeline dates to the 2004-era Eclipse Trader codebase. It draws to a pixel-sized offscreen image that is recreated only on resize and never re-synced to the display's zoom factor, so output can be stale or blurry when the device zoom changes (mixed-DPI monitors, OS scale changes). It scatters raw `RGB` literals across ~8 classes (no single palette to maintain, no path to dark mode), and rebuilds all point geometry on every repaint (janky on large histories). PR #18 only polished the surface (antialiasing + a one-off palette) without addressing these structural issues, so it has been closed and its durable bug fixes extracted separately.

## What Changes

- **HiDPI / device-zoom-aware rendering**: offscreen chart images are recreated when the display's zoom factor changes, so charts stay crisp across DPI/scale changes (SWT 4.31's image/GC pipeline already renders at native resolution).
- **Theme / palette service**: a theme provider centralizes all chart colors (line, positive/negative, outline, grid, background). The chart classes and `MainPropertiesPage` source their colors from it instead of inline `new RGB(...)`. Saved user color preferences continue to be honored. Dark mode becomes possible later without rework.
- **Performance: geometry caching + downsampling**: computed point geometry (candles/bars/points) is cached and invalidated only when data or bounds change; large series are downsampled (min/max binning) so the number of rendered points stays bounded by pixels.
- **Rendering quality**: antialiasing and text antialiasing enabled for all chart drawing (retained from the closed PR, re-applied on the new pipeline).
- Supersedes the cosmetic portions of the closed PR #18. Its durable bug fixes (Close-vs-High tooltips, `SummaryOHLCItem` resource disposal) land as a separate small bugfix PR first.

## Capabilities

### New Capabilities

- `chart-rendering`: the chart drawing pipeline — device-zoom-aware output, geometry caching, and dataset downsampling so charts render correctly and smoothly at any display scale and data size.
- `chart-theme`: the centralized color/theme provider that all chart objects and chart property pages source their colors from, including honoring user-saved color preferences.

### Modified Capabilities

None — this repo has no existing specs yet; both capabilities are new.

## Impact

- **Code**: `org.eclipsetrader.ui` charts package — `Graphics`, `ChartCanvas`, `CandleStickChart`, `BarChart`, `OHLCLineChart`, `HistogramAreaChart`, `HistogramBarChart`, `LineChart`, axis classes — plus `MainChartFactory`, `MainPropertiesPage`, and `org.eclipsetrader.ui.charts.indicators` (`Util`). Chart templates unchanged (they already default to candles).
- **API**: new `ChartTheme`/`ChartThemes` provider API; chart object constructors keep their signatures and fall back to the shared default theme, so existing callers keep compiling.
- **Dependencies**: none new — SWT only. Antialiasing and zoom awareness are standard SWT/GC features.
- **Persistence**: existing saved chart color preferences remain valid and override theme defaults.
- **Related work**: closed PR #18 is not merged; its extracted bug fixes ship as a separate small PR.
