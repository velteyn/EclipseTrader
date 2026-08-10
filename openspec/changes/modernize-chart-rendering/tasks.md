## 1. Theme layer

- [x] 1.1 Add `ChartTheme` value object (line, positive, negative, outline, grid, background RGB) and `ChartThemes.getDefault()` exposing the default Material palette (line `33,150,243`, positive `38,166,154`, negative `239,83,80`, outline `64,64,64`)
- [x] 1.2 Replace inline `new RGB(...)` field/constructor fallbacks in `CandleStickChart`, `BarChart`, `HistogramBarChart`, `HistogramAreaChart`, `OHLCLineChart`, and `LineChart` with lookups from `ChartThemes.getDefault()`, keeping constructor color parameters and their null-fallback semantics
- [x] 1.3 Source `MainChartFactory` color field defaults and `MainPropertiesPage` color-selector defaults from the same theme
- [ ] 1.4 Verify: charts created without explicit colors render with the theme palette, and user-saved colors still override them (spec `chart-theme`)

## 2. HiDPI rendering

- [x] 2.1 Add a `ChartUtils.createBackingImage(Canvas, Rectangle)` helper that creates a logical-size offscreen image via `new Image(display, width, height)` (SWT 4.31 auto-scales it to native pixels), records the creation zoom from the canvas's monitor (`Monitor.getZoom()`, falling back to `display.getDPI().x / 72`), and exposes whether the image needs recreating when the zoom differs
- [x] 2.2 Migrate the main offscreen image in `ChartCanvas.onPaint` to the helper (dispose and recreate on resize or zoom change, as today)
- [x] 2.3 Migrate the date scale canvas and vertical scale canvas image creation to the same helper (recreate on zoom change too)
- [x] 2.4 Enable shape and text antialiasing in the `Graphics` constructor (spec `chart-rendering` — Antialiased drawing)
- [ ] 2.5 Verify charts render crisply at 100%, 150%, and 200% display zoom (simulate with `GDK_SCALE`) with no blur and no layout regression, and that a zoom change after image creation recreates the offscreen image (spec `chart-rendering` — Device-zoom-aware rendering)

## 3. Downsampling and geometry caching

- [x] 3.1 Add `OHLCDownsampler` with min/max binning per pixel column (preserving high/low, first-open/last-close for candles) returning an aggregated `IOHLC[]`
- [x] 3.2 Add the scalar equivalent for line/area/histogram renderers
- [x] 3.3 Wire downsampling into `setDataBounds`: when visible points exceed the chart width, aggregate before geometry build; cache keyed by (firstDate, lastDate, width) and cleared on `invalidate()` (spec `chart-rendering` — Geometry caching)
- [x] 3.4 Apply to `CandleStickChart`, `BarChart`, `OHLCLineChart`, `HistogramAreaChart`, `HistogramBarChart`, and `LineChart` (spec `chart-rendering` — Downsampling of large series)
- [ ] 3.5 Verify with a large history: zoomed-out draws at most one element per pixel column, pan/zoom remain responsive, and zoom-in restores full per-tick detail

## 4. Verification

- [ ] 4.1 Add pure-function unit tests for `OHLCDownsampler` (extreme preservation, one-per-column bound, boundary at width == points) and wire `org.eclipsetrader.ui.tests` into the Maven reactor so they run in GitHub Actions CI
- [ ] 4.2 Build the product with `mvn package` (the existing `.github/workflows/maven.yml` job on JDK 21) and confirm `org.eclipsetrader.ui` and `org.eclipsetrader.ui.charts.indicators` compile
- [ ] 4.3 Extend the Codespaces devcontainer (`.devcontainer/devcontainer.json`) with a virtual display (e.g., desktop-lite/VNC or Xvfb) and GTK so the SWT product can run headless
- [ ] 4.4 Visually verify summary bar, crosshair, chart export-to-image, and indicator rendering at 100%/150%/200% zoom (simulate with `GDK_SCALE`) using small and large histories
