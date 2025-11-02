# EclipseTrader

EclipseTrader is a sophisticated stock trading application developed in Java and built upon the robust Eclipse Rich Client Platform (RCP). Originally created by Marco Maccaferri in 2011, this project provides a comprehensive platform for financial market analysis, strategy development, and simulated trading.

The core of the current project is the **JESSX plugin**, a powerful stock market simulator. JESSX enables users to create complex trading scenarios, define market behavior, and deploy automated trading bots to test their strategies in a live, simulated environment. The primary focus of this repository is to enhance and stabilize the JESSX simulation engine, making it a reliable tool for algorithmic trading research and development.

This repository represents an ongoing effort to modernize the original EclipseTrader application by migrating it to a Maven/Tycho build system, addressing critical bugs, and improving overall stability. While the codebase contains legacy plugins for various brokers (e.g., `directa`, `yahoo`), these are currently deactivated and retained for archival purposes.

## Getting Started: Setting Up Your Development Environment

To develop and run EclipseTrader from your IDE, you will need to set up an Eclipse RCP development environment.

### Prerequisites
- **Java 7**: The project is built on Java 7. Ensure you have a compatible JDK installed.
- **Eclipse IDE for RCP and RAP Developers**: Download the appropriate version (e.g., Eclipse 3.8/Juno) to ensure compatibility with the project's dependencies.

### Setup Instructions
1.  **Import Projects**:
    *   In Eclipse, go to `File > Import...`.
    *   Select `General > Existing Projects into Workspace`.
    *   Browse to the root directory of this repository and import all discovered projects.

2.  **Set the Target Platform**:
    *   The target platform defines the set of plugins your workspace will be built and launched against.
    *   Open the `org.eclipsetrader.releng/eclipsetrader.target` file.
    *   Wait for Eclipse to resolve all dependencies. Once it's finished, click **Set as Target Platform** in the top-right corner of the editor.
    *   This may take a few minutes as Eclipse downloads all the necessary plugins from the Juno p2 repository.

3.  **Launch the Application**:
    *   Open the `org.eclipsetrader.releng/eclipsetrader.product` file.
    *   Click the **Synchronize** link to ensure the product definition is up-to-date with the target platform.
    *   Click the **Launch an Eclipse application** link in the **Testing** section to start the EclipseTrader application.

## How to Compile

The project is built using Maven and Tycho. To compile all plugins and create the final product, run the following command from the root of the repository:

```bash
mvn clean install
```

### Important Notes
*   **Skipping Tests**: The project currently lacks a comprehensive automated test suite. It is recommended to skip the test execution during the build process:
    ```bash
    mvn clean install -DskipTests
    ```
*   **Build Environment**: In some environments with file-count limitations (like certain sandboxes), the Tycho build process can fail. A workaround is to temporarily modify the root `pom.xml` to redirect the build output outside the workspace:
    ```xml
    <properties>
        <project.build.directory>/tmp/eclipsetrader-build/${project.artifactId}</project.build.directory>
    </properties>
    ```
    This change should be reverted before committing your code.

## Contributing

Contributions to EclipseTrader are welcome! The primary goal is to stabilize the JESSX simulation functionality and address blocking bugs that prevent core features from working as intended.

### Guidelines
*   **Focus on JESSX**: The main development effort is concentrated on the `org.eclipsetrader.jessx` plugin and its related components.
*   **Manual Verification**: Due to the current lack of an automated test suite, all changes must be thoroughly tested manually. Launch the application and ensure your changes are working correctly and have not introduced any regressions.
*   **Code Compilation**: All submitted code must compile successfully against the provided target platform. Pull requests with compilation errors will not be accepted.
*   **Understand the Legacy**: Be aware that this is a legacy project. Some parts of the code may seem complex or unconventional. When encountering such code, it is better to add comments and report it for future analysis rather than attempting a large-scale refactoring.

### Project Context
The JESSX simulator was originally a separate project (https://github.com/velteyn/jessx) that was integrated into EclipseTrader. Much of its source code was decompiled, which can make it challenging to understand. The focus is on pragmatic fixes to make it functional rather than rewriting it from scratch.
