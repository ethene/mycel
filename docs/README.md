# Mycel Documentation Index

This folder contains comprehensive documentation for the **Mycel** messaging application by **Quantum Research Pty Ltd**.

**Project Status**: Currently rebranding from Briar to Mycel while preserving all core functionality.

## Quick Start
- [00-mycel-quick-start.md](00-mycel-quick-start.md) - **START HERE** - Quick overview and setup for Mycel

## Documentation Structure

### 📁 Core Architecture
- [01-project-overview.md](01-project-overview.md) - High-level project overview and goals
- [02-module-structure.md](02-module-structure.md) - Detailed module breakdown and dependencies
- [03-folder-structure.md](03-folder-structure.md) - Complete folder-by-folder analysis
- [04-build-system.md](04-build-system.md) - Gradle build system documentation

### 📁 Code Analysis
- [05-bramble-layer.md](05-bramble-layer.md) - Infrastructure layer (networking, crypto, sync)
- [06-briar-layer.md](06-briar-layer.md) - Application layer (messaging, forums, blogs)
- [07-android-layer.md](07-android-layer.md) - UI and platform integration
- [08-database-schema.md](08-database-schema.md) - Database structure and schemas

### 📁 Features & Workflows
- [09-messaging-workflow.md](09-messaging-workflow.md) - How messaging works end-to-end
- [10-transport-system.md](10-transport-system.md) - Transport plugins (Tor, Bluetooth, WiFi)
- [11-crypto-security.md](11-crypto-security.md) - Cryptographic implementation details
- [12-sync-protocol.md](12-sync-protocol.md) - Message synchronization protocol

### 📁 Rebranding Guide
- [13-rebranding-strategy.md](13-rebranding-strategy.md) - Complete rebranding roadmap
- [14-naming-conventions.md](14-naming-conventions.md) - Naming patterns and conventions
- [15-ui-elements.md](15-ui-elements.md) - UI components and themes to modify
- [16-configuration-files.md](16-configuration-files.md) - Config files requiring updates

### 📁 Development
- [17-development-setup.md](17-development-setup.md) - Getting started with development
- [18-testing-strategy.md](18-testing-strategy.md) - Testing approaches and frameworks
- [19-deployment-guide.md](19-deployment-guide.md) - Building and deploying the app
- [20-troubleshooting.md](20-troubleshooting.md) - Common issues and solutions

## Quick Reference

### Key Modules
- **bramble-*** - Infrastructure (networking, crypto, sync)
- **briar-*** - Application logic (messaging, forums, blogs)
- **-android** - Android-specific implementations
- **-api** - Interface definitions
- **-core** - Core implementations

### Important Files
- `settings.gradle` - Module configuration
- `build.gradle` - Build configuration
- `gradle.properties` - Build properties
- `local.properties` - Local development settings

### Key Directories
- `src/main/java` - Java source code
- `src/main/res` - Android resources
- `src/test` - Unit tests
- `src/androidTest` - Android integration tests

---

*This documentation is designed to provide complete understanding for rebranding and customizing the Briar messaging application.*