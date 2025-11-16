## Overview
- Resolve missing types and unstable source folders by making PDE manage plug-in builds and dependencies, and by resolving Nebula from the update site via the target platform.

## PDE Build Normalization
- com.sun.syndication.fetcher: use `Bundle-ClassPath: .` and `build.properties` with `source.. = src/`, `output.. = bin/`, include `.` not `src/` (`com.sun.syndication.fetcher/META-INF/MANIFEST.MF`, `build.properties`).
- com.sun.syndication: set JDT output to `bin` to match PDE (`com.sun.syndication/.classpath`), keep `build.properties` `source.. = src/`, `output.. = bin/`.
- Disable Maven nature for the two bundles so m2e stops rewiring classpath (`com.sun.syndication.fetcher/.project`, `com.sun.syndication/.project`).
- Use `PDE Tools -> Update Classpath` on each to re-sync Eclipse with `build.properties`.

## JDOM Availability
- Ensure the workspace `org.jdom` bundle is present and exported (`org.jdom/META-INF/MANIFEST.MF:1-16`, `build.properties`).
- Add/keep `Require-Bundle: org.jdom` for `com.sun.syndication` so JDT sees `org.jdom.Document` (`com.sun.syndication/META-INF/MANIFEST.MF`).

## Nebula Migration
- Update the target to include Nebula IUs from the Nebula update site (`org.eclipsetrader.releng/eclipsetrader.target`): add `org.eclipse.nebula.cwt`, `org.eclipse.nebula.widgets.cdatetime`, `org.eclipse.nebula.jface.cdatetime`.
- Relax strict version on `org.eclipse.nebula.widgets.cdatetime` where pinned (e.g., `org.eclipsetrader.jessx/META-INF/MANIFEST.MF:12`).
- Activate the target in `Preferences -> Plug-in Development -> Target Platform`, reload, then `Project -> Clean…`.
- Once resolved, remove local Nebula projects (`org.eclipse.nebula.cwt`, `org.eclipse.nebula.widgets.cdatetime`, `org.eclipse.nebula.jface.cdatetime`, and `org.eclipse.nebula.widgets.pshelf` since PShelf is unused).

## Build Verification
- CLI: run `mvn -U clean verify` from `c:\Projects\Virtual Investor\EclipseTrader`. Expect Tycho to materialize products under `org.eclipsetrader.releng/target/products/...`.
- IDE: ensure no “Missing required plug-in” or “Package does not exist” errors; re-run product export if needed (`org.eclipsetrader.releng/eclipsetrader.product`).

## Notes
- If Nebula bundles require `JavaSE-11`, ensure the product/runtime uses a Java 11+ VM; update launch or product JRE as needed.
- If any remaining compilation errors appear (e.g., Apache HttpClient packages), verify `Require-Bundle`/`Import-Package` entries and add workspace bundles as needed (`org.apache.commons.httpclient`, `org.apache.commons.logging`).