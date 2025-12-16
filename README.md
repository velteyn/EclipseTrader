# EclipseTrader

EclipseTrader is a sophisticated stock trading application developed in Java and built upon the robust Eclipse Rich Client Platform (RCP). Originally created by Marco Maccaferri in 2011, this project provides a comprehensive platform for financial market analysis, strategy development, and simulated trading.

The core of the current project is the **JESSX plugin**, a powerful stock market simulator. JESSX enables users to create complex trading scenarios, define market behavior, and deploy automated trading bots to test their strategies in a live, simulated environment. The primary focus of this repository is to enhance and stabilize the JESSX simulation engine, making it a reliable tool for algorithmic trading research and development.

This repository represents an ongoing effort to modernize the original EclipseTrader application by migrating it to a Maven/Tycho build system, addressing critical bugs, and improving overall stability. While the codebase contains legacy plugins for various brokers (e.g., `directa`, `yahoo`), these are currently deactivated and retained for archival purposes.

[![Java CI with Maven](https://github.com/velteyn/EclipseTrader/actions/workflows/maven.yml/badge.svg)](https://github.com/velteyn/EclipseTrader/actions/workflows/maven.yml)

## Getting Started: Setting Up Your Development Environment

To develop and run EclipseTrader from your IDE, you will need to set up an Eclipse RCP development environment.

### Prerequisites
- **Java 8**: Use Adoptium/OpenJDK 8 (matching the workspace runtime). (The goal is to move to Java 21/25 soon (HELP ME !!))
- **Eclipse IDE for RCP and RAP Developers**: A recent SimRel (e.g., 2025‑12).
- **Tycho/Maven**: Included in the repository build; no separate install needed in Eclipse.

### Setup Instructions
1.  **Import Projects**:
    *   In Eclipse, go to `File > Import...`.
    *   Select `Maven > Existing Maven Projects`.
    *   Browse to the root directory of this repository and import all discovered projects.

2.  **Set the Target Platform**:
    *   The target platform defines the set of plugins your workspace will be built and launched against.
    *   Open the `org.eclipsetrader.releng/eclipsetrader.target` file.
    *   Wait for Eclipse to resolve all dependencies. Once it's finished, click **Set as Target Platform** in the top-right corner of the editor.
    *   This may take a few minutes as Eclipse downloads all the necessary plugins.
    *   Set Java Compiler compliance level to Java 1.8 and select that for JREs (temporary but HELP ME to move to Java 21/25 tkz)

3.  **Launch the Application from the IDE**:
    * Open `org.eclipsetrader.releng/eclipsetrader.product`.
    * Click **Synchronize** (top‑right).
    * In **Testing**, click **Launch an Eclipse application**.
    * Alternatively, use `org.eclipsetrader.releng/EclipseTrader-Workspace.launch` for a curated workspace‑only launch (excludes Equinox p2 extras).

4.  **Avoid Duplicate Singletons in Launch**:
    * In the Run Configuration → Plug‑ins tab set “Launch with” to `Plug‑ins selected below`.
    * Keep the workspace instance of `org.eclipsetrader.core (1.0.0.qualifier)` and uncheck any timestamped instance coming from local p2 repos.
    * Uncheck optional Equinox **p2** bundles (console, director, operations, ui) which are not required to run EclipseTrader and can cause resolution errors in dev launches.

5.  **JessX Scenario Selection (Preferences)**:
    * Open **Preferences → Plugins → JessX! Scenarios**.
    * Pick a bundled scenario: `default.xml`, `bull-market.xml`, `bear-market.xml`, `volatile-market.xml`, or click **Browse…** and select any `.xml` on disk.
    * To add custom scenarios, drop files into:
      ```
      <workspace>/.metadata/.plugins/org.eclipsetrader.jessx/scenarios/
      ```
    * The selected scenario is persisted and loaded at startup by JessX.

6.  **Start JessX Server and Data Flow**:
    * Ensure the **JessX broker** is set as the active broker.
    * Click the “Play” button to start JessX; the server initializes and the client connects on `localhost:6290`.
    * Watchlists and charts will update from the simulated feed.

7.  **Common Runtime Warnings**:
    * **SLF4J**: If you see “Failed to load class org.slf4j.impl.StaticLoggerBinder”, logging defaults to NOP. Add one provider (`slf4j-simple`, `slf4j-jdk14`, `slf4j-reload4j`, or `logback-classic`) if desired, or `slf4j-nop` to suppress.
    * **Jasper TLD scanner**: Informational only; safe to ignore.

## How to Compile and Run

The project is built using Maven and Tycho. To compile all plugins and create the final product, run the following command from the root of the repository:

```bash
mvn clean install
```

After a successful build, you can run the application from the command line:

```bash
org.eclipsetrader.releng\target\products\org.eclipsetrader.platform.workbench\win32\win32\x86_64\trader.exe
```
*(Adjust the path according to your operating system)*

### Important Notes
- **Tests**: Modern tests run under Tycho in `org.eclipsetrader.core.modern.tests`. If you prefer faster local builds:
  ```bash
  mvn clean install -DskipTests
  ```
- **Product build**: If the p2 director fails due to missing features, ensure `org.jdom-feature` and other local feature IU versions are present (built by the reactor) and avoid mixing targets.

## Contributing

Contributions to EclipseTrader are welcome! The primary goal is to stabilize the JESSX simulation functionality and address blocking bugs that prevent core features from working as intended.

### Guidelines
* **Focus on JESSX**: Main effort centers on `org.eclipsetrader.jessx` and related components.
* **Manual Verification**: Launch from the IDE and verify changes interactively.
* **Code Compilation**: Ensure successful compilation against the target platform.
* **Legacy Awareness**: Prefer pragmatic fixes over large refactors without coverage.

### Project Context
The JESSX simulator was originally a separate project (https://github.com/velteyn/jessx) that was integrated into EclipseTrader. Much of its source code was decompiled, which can make it challenging to understand. The focus is on pragmatic fixes to make it functional rather than rewriting it from scratch.
