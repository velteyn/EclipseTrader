## Context

- See `proposal.md` for motivation. See `specs/chart-rendering/spec.md` and `specs/chart-theme/spec.md` for the behavioral requirements this design satisfies.
- The chart renders by drawing onto an offscreen `Image` (`ChartCanvas.onPaint`, `DateScaleCanvas`, vertical scale canvas), then blits it to the canvas. The image is recreated only on resize, and `Image.getBounds()` returns *logical* size, so a zoom change alone never triggers recreation — an image created at one zoom stays stale when the device zoom changes (mixed-DPI monitors, OS scale changes).
- Chart colors are inline `new RGB(...)` literals scattered across the charts package, `MainChartFactory`, and `MainPropertiesPage`.
- Geometry is partially cached already (`valid`/`pointArray` pooling in `CandleStickChart`, etc.), but `setDataBounds` re-filters the full series O(n) and there is no downsampling, so zoomed-out large histories draw every point.
- Target platform is **Eclipse 2024-03 / SWT 4.31, Java 21** (see `org.eclipsetrader.releng/eclipsetrader.target`). On SWT 4.31, `new Image(device, w, h)` already creates a device-zoom-aware backing image: width/height are logical points auto-scaled to native pixels in the constructor, and `getBounds()` reports logical size. Per-monitor zoom is `Monitor.getZoom()`; the zoom captured at image creation is `DPIUtil.getDeviceZoom()`. `GC.setAntialias()`/`GC.setTextAntialias()` are available. (`Display.getZoom()` and `Image.setScaleFactor()` are **not** in 4.31 — they landed in SWT 3.104 / 2024-09, one release after the target.)

## Goals / Non-Goals

**Goals:**
- Crisp chart output on any display zoom (HiDPI) with minimal change to chart-object drawing code.
- One source of truth for chart colors, with user-saved color preferences still winning.
- Bounded rendering work per frame regardless of history size.

**Non-Goals:**
- Dark mode UI / theme switching in the preferences UI (the theme layer enables it later; selecting themes is out of scope).
- Rewriting the `IChartObject` model or the axis classes.
- Overhauling gridline/`Calendar`-based tick computation (`ChartCanvas.paintBackground`) — tracked as a follow-up, not in this change.
- Changing chart templates or persisted layouts.

## Decisions

### 1. HiDPI via zoom-aware offscreen images

Create offscreen images at the chart's logical `clientArea` size with `new Image(display, w, h)`. SWT 4.31's image/GC pipeline is already device-zoom-aware: the constructor auto-scales the backing store to native pixels, `getBounds()` reports logical size, and GC drawing auto-scales logical coordinates. All existing drawing code (which uses logical coordinates) keeps working unchanged and already lands at native resolution.

The remaining defect is lifecycle, not size: canvases recreate the image only on resize, and because `getBounds()` returns logical size the resize check never fires on a zoom change. Fix with a single helper, `ChartUtils.createBackingImage(Canvas, Rectangle)`, that (a) creates the logical-size image and (b) records the zoom at creation — from the canvas's monitor via `Monitor.getZoom()`, falling back to `display.getDPI().x / 72` if unavailable — and lets the canvas detect on the next paint that the zoom differs and recreate the image. Used by the main canvas, the date scale canvas, and the vertical scale canvas.

- **Alternative considered**: create the image at `clientArea * zoom/100` pixels and apply `image.setScaleFactor(zoom, zoom)`. Rejected: `Image.setScaleFactor` does not exist in SWT 4.31 (added in 3.104/2024-09), and the 4.31 `Image(Device, w, h)` constructor already auto-scales — multiplying by zoom again would double-scale at zoom > 100%.
- **Alternative considered**: `GC.setTransform(new Transform(...scale))` per paint. Rejected: more invasive, risks double-scaling on every draw call, and doesn't help the blit step.
- **Alternative considered**: keep resize-only recreation (status quo). Rejected — that is the stale-zoom defect this change fixes.
- Antialiasing (shapes + text) is enabled in the `Graphics` constructor, as proposed in the superseded PR.

### 2. Theme layer: `ChartTheme` value object + default registry

Introduce an immutable `ChartTheme` (RGB: line, positive, negative, outline, grid, background) and a `ChartThemes` holder exposing the default light theme (the Material palette proposed in PR #18: blue line `33,150,243`, teal `38,166,154`, red `239,83,80`, outline `64,64,64`).

- Chart classes (`CandleStickChart`, `BarChart`, `HistogramBarChart`, `HistogramAreaChart`, `OHLCLineChart`, `LineChart`) replace their inline `new RGB(...)` field initializers/constructor fallbacks with lookups from `ChartThemes.getDefault()`. Their RGB constructor parameters stay — the `null`-fallback semantics are unchanged.
- `MainChartFactory` field defaults and `MainPropertiesPage` color-selector defaults read from the same theme.
- User preferences keep their existing path: `setParameters` → non-null RGB → `createObject(...)` passes them in, overriding theme defaults (spec `chart-theme`).
- **Alternative considered**: full dependency injection / OSGi service for themes. Rejected as over-engineering for this codebase; a plain value object + static default is enough to centralize the palette and later add a second theme.
- **Rationale for value-object over a live theme object**: RGB is a plain value in SWT; no listeners/observability are needed for this change.

### 3. Downsampling via per-renderer aggregators, cached per visible range

Extend the existing `setDataBounds`/`valid` pattern with an aggregation step and a cache keyed by `(firstDate, lastDate, pixelWidth)`:

- When the visible points exceed `clientArea.width`, aggregate before building geometry.
- **OHLC renderers** (candles, bars, OHLC line): min/max binning per pixel column — preserve each column's high/low (and first-open/last-close for candles) so zoomed-out bars stay honest.
- **Scalar renderers** (line, area, histogram): min/max per column as well (keeps spikes visible); LTTB is an acceptable alternative for line aesthetics — implementation detail, spec only requires one aggregate per column.
- Implemented as shared helpers (e.g., `OHLCDownsampler.downsample(IOHLC[], int width)` and a scalar counterpart) so all chart classes benefit without duplicating logic.
- Cache lives with the chart object alongside `pointArray`; `invalidate()` clears it. Work per frame becomes O(pixels) to draw plus O(n) only when data/bounds actually change.
- **Alternative considered**: always-on LTTB for everything. Rejected — min/max binning is O(n) and correct for OHLC extremes; LTTB is a polish option for lines only.
- **Trade-off**: zoomed-out tooltips report the aggregated candle/bar rather than one specific tick. Accepted (standard practice); per-tick detail returns on zoom-in.

## Risks / Trade-offs

- **[Risk] A scale-canvas or image path misses the zoom-change recreation** → inconsistent crispness across mixed-DPI moves. Mitigation: single `createBackingImage` helper used by all three canvases; verify visually at 200% during implementation.
- **[Risk] Downsampling alters zoomed-out visuals and tooltips** → expected behavior change. Mitigation: spec already defines one-aggregate-per-column; confirm the min/max OHLC result with a large history in review.
- **[Risk] Theme defaults shift user-visible colors** for charts that had no explicit colors → intended by design (spec `chart-theme`), but only affects charts with no saved colors.
- **[Risk] Per-frame allocation regressions** → geometry and aggregation caches reuse objects; keep pooling behavior from the current `pointArray` pattern.
- **[Risk] SWT version drift** (zoom APIs) → guard with fallback to `getDPI()`; `Monitor.getZoom()` exists in the fixed 2024-03 target.

## Migration Plan

- Pure UI change; no persisted data or API breakage. Chart-object constructors and `MainChartFactory` keep their signatures.
- Rollback: revert the change — existing layouts and preferences are untouched.
- Land independently of the extracted PR #18 bug fixes (tooltip + resource disposal), which merge first on master.
