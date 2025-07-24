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
- [07-android-layer.md](07-android-layer.md) - UI and platform integration
- [08-database-schema.md](08-database-schema.md) - Database structure and schemas

### 📁 Features & Workflows
- [09-messaging-workflow.md](09-messaging-workflow.md) - How messaging works end-to-end
- [10-transport-system.md](10-transport-system.md) - Transport plugins (Tor, Bluetooth, WiFi)
- [11-crypto-security.md](11-crypto-security.md) - Cryptographic implementation details
- [12-sync-protocol.md](12-sync-protocol.md) - Message synchronization protocol

### 📁 Rebranding Implementation
- **[INCREMENTAL-REBRANDING-PLAN.md](INCREMENTAL-REBRANDING-PLAN.md)** - **PRIMARY GUIDE** - Current rebranding implementation
- [16-configuration-files.md](16-configuration-files.md) - Config files requiring updates
- [GIT-INITIALIZATION-GUIDE.md](GIT-INITIALIZATION-GUIDE.md) - Git repository setup guide

### 📁 Development
- [17-development-setup.md](17-development-setup.md) - Getting started with development
- [18-testing-strategy.md](18-testing-strategy.md) - Testing approaches and frameworks
- [19-deployment-guide.md](19-deployment-guide.md) - Building and deploying the app
- [20-troubleshooting.md](20-troubleshooting.md) - Common issues and solutions

## Quick Reference

### Key Modules (Current Structure)
- **spore-*** - Infrastructure layer (networking, crypto, sync)
  - `spore-api` - Infrastructure APIs
  - `spore-core` - Core infrastructure implementation
  - `spore-android` - Android-specific infrastructure
  - `spore-java` - Java/desktop infrastructure
- **mycel-*** - Application layer (messaging, forums, blogs)
  - `mycel-api` - Application APIs
  - `mycel-core` - Core application logic
  - `mycel-android` - Android UI application
  - `mycel-headless` - REST API service

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

## Archive Folder

The `archive/` folder contains documentation that was relevant during the Briar-to-Mycel rebranding process but is now outdated. This includes:
- Original rebranding strategy documents
- Briar-specific technical implementation details with old package names
- Completed planning documents

The archive folder is excluded from git commits to keep the repository focused on current development.

---

*This documentation supports the ongoing development of Mycel by Quantum Research Pty Ltd.*