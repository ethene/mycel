# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Mycel** is a decentralized messaging app developed by **Quantum Research Pty Ltd** for secure peer-to-peer communication without central servers. It supports multiple transports (Tor, Bluetooth, Wi-Fi) and prioritizes security and privacy.

**Important**: This is a rebranding of the original Briar messaging application. The core functionality and architecture remain unchanged, but all branding elements are being updated to Mycel by Quantum Research.

## Build Commands

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :briar-core:test
./gradlew :bramble-core:test

# Run Android instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run mailbox integration tests (when enabled)
MAILBOX_INTEGRATION_TESTS=true ./gradlew mailbox-integration-tests:test
```

### Building Android APK
```bash
# Debug build
./gradlew :briar-android:assembleDebug

# Release build (requires signing configuration)
./gradlew :briar-android:assembleRelease
```

### Running the Headless REST API
```bash
./gradlew :briar-headless:run
```

### Code Quality
```bash
# Animal Sniffer API compatibility check
./gradlew signatureCheck

# Dependency verification
./gradlew check
```

## Important Build Notes

⚠️ **Build Timeout Handling**: If any build command (like `./gradlew build`, `./gradlew test`, etc.) runs for more than 2 minutes, do NOT assume it has failed. Instead, ask the user to run the command in a separate terminal to verify if it completes successfully or encounters actual errors. Gradle builds can take considerable time due to dependency resolution, compilation, and testing phases.

## Architecture Overview

### Module Structure

**Bramble Layer (Infrastructure):**
- `bramble-api`: Core transport and sync APIs
- `bramble-core`: Networking, crypto, and sync implementation  
- `bramble-android`: Android-specific transport implementations
- `bramble-java`: Java/desktop transport implementations

**Briar Layer (Application Logic):**
- `briar-api`: High-level messaging APIs
- `briar-core`: Application features (messaging, forums, blogs)
- `briar-android`: Android UI and platform integration
- `briar-headless`: REST API for headless operation

### Key Design Patterns

1. **Dependency Injection**: Extensive use of Dagger 2 with module-based architecture
2. **Plugin Architecture**: Pluggable transport system (Bluetooth, TCP, Tor)
3. **Event-Driven**: Central EventBus for decoupled component communication
4. **Layered Architecture**: Clear separation between transport, application, and UI layers
5. **Factory Pattern**: Object creation through factory interfaces

### Core Packages

**Bramble Core:**
- `api.sync`: Message synchronization protocol
- `api.crypto`: Cryptographic operations  
- `api.plugin`: Transport plugin architecture
- `api.db`: Database abstraction
- `api.contact`: Contact and identity management
- `api.event`: Event bus system

**Briar Core:**
- `api.messaging`: Private messaging
- `api.forum`: Public forums
- `api.blog`: Blog and RSS functionality
- `api.privategroup`: Private group chats
- `api.sharing`: Content sharing
- `api.introduction`: Contact introduction protocol

## Development Guidelines

### Testing
- Unit tests are located in `src/test` directories
- Android instrumentation tests in `src/androidTest`
- Use JMock for mocking in unit tests
- Robolectric for Android unit tests
- Integration tests require special configuration

### Security Considerations
- All networking goes through encrypted transports
- Database is encrypted with user-derived keys
- Never log sensitive information (keys, message content)
- Follow existing crypto patterns for new features

### Database
- Uses `DatabaseComponent` for high-level operations
- All database access must be transactional
- Supports H2 and HyperSQL backends
- Migration scripts in database implementations

### Android Development
- Follows MVVM pattern with ViewModels and LiveData
- Fragment-based UI architecture
- Material Design components
- Extensive accessibility support

### Adding New Features
1. Define APIs in appropriate `-api` module
2. Implement in corresponding `-core` module
3. Add Dagger modules for dependency injection
4. Create UI components in `-android` module
5. Add comprehensive tests

## Common Development Tasks

### Adding a New Transport Plugin
1. Implement `Plugin` interface in `bramble-*` module
2. Create corresponding `PluginFactory`
3. Register in appropriate Dagger module
4. Add configuration in `TransportPropertyManager`

### Adding New Message Types
1. Define in `briar-api` messaging package
2. Implement validation in `briar-core`
3. Add database schema changes if needed
4. Create UI components for display/editing
5. Add comprehensive test coverage

### Working with Events
- Use `EventBus` for loose coupling between components
- Define events in `api.event` package
- Register listeners in appropriate lifecycle methods
- Always consider thread safety when handling events

## Documentation

Comprehensive documentation is available in the `docs/` folder:

### Core Architecture Documentation
- `docs/01-project-overview.md` - High-level project overview and goals
- `docs/02-module-structure.md` - Detailed module breakdown and dependencies
- `docs/03-folder-structure.md` - Complete folder-by-folder analysis
- `docs/09-messaging-workflow.md` - How messaging works end-to-end
- `docs/10-transport-system.md` - Transport plugins (Tor, Bluetooth, WiFi)

### Rebranding Documentation
- `docs/INCREMENTAL-REBRANDING-PLAN.md` - **PRIMARY PLAN** - Incremental, test-driven rebranding approach
- `docs/VERIFIED-REBRANDING-PLAN.md` - Comprehensive analysis with verified file locations (reference only)
- `docs/13-rebranding-strategy.md` - Complete rebranding roadmap and strategy
- `docs/16-configuration-files.md` - All config files requiring updates for rebranding

**Important**: The project was originally Briar but is being rebranded to **Mycel** by **Quantum Research Pty Ltd**. All documentation in `docs/` contains detailed analysis for understanding the codebase structure and implementing the Mycel rebrand while preserving all functionality.

## Mycel Rebranding Details

### Brand Identity
- **New Name**: Mycel
- **Developer**: Quantum Research Pty Ltd
- **Package Structure**: `com.quantumresearch.mycel.*` (from `org.briarproject.*`)
- **Application ID**: `com.quantumresearch.mycel` (from `org.briarproject.briar.android`)
- **Deep Link Scheme**: `mycel://` (from `briar://`)

### Key Rebranding Requirements
1. **Package Names**: All `org.briarproject.bramble.*` → `com.quantumresearch.mycel.infrastructure.*`
2. **Package Names**: All `org.briarproject.briar.*` → `com.quantumresearch.mycel.app.*`
3. **App Name**: "Briar" → "Mycel" in all string resources (30+ languages)
4. **Visual Assets**: Replace all Briar logos with Mycel branding
5. **URLs**: Update to Quantum Research domains (e.g., https://quantumresearch.com.au)
6. **Build Configuration**: Update applicationId, signing certificates, store accounts

**PRIMARY REBRANDING PLAN**: `docs/INCREMENTAL-REBRANDING-PLAN.md` contains the step-by-step implementation approach. Additional reference documentation is available in `docs/13-rebranding-strategy.md` and `docs/16-configuration-files.md`.

## Current Status

**IMPORTANT**: This project is ready to begin incremental Mycel rebranding implementation. The analysis phase is complete with comprehensive documentation in the `docs/` folder.

### Rebranding Approach
**Use the incremental, test-driven approach** detailed in `docs/INCREMENTAL-REBRANDING-PLAN.md`:

**Phase 1: String Resources** → **Phase 2: App ID** → **Phase 3-4: Package Renaming** → **Phase 5: Configuration** → **Phase 6: Visual Assets** → **Phase 7: Documentation**

### Critical Requirements:
1. **Test after each phase**: All tests must pass before proceeding
2. **Build verification**: `./gradlew build` must succeed after each phase  
3. **Git commits**: Commit after each successful phase completion
4. **Documentation updates**: Update `CLAUDE.md` and relevant docs after each phase
5. **Incremental approach**: Do NOT skip phases or combine them

### Testing Protocol:
```bash
# After each phase
./gradlew clean build test
./gradlew :briar-android:assembleDebug
# Manual verification of core functionality
```

**Current Phase**: Ready to begin Phase 1 (String Resources Only)

Refer to `docs/INCREMENTAL-REBRANDING-PLAN.md` for detailed step-by-step instructions.

## Git Repository Management

### Repository Setup
**IMPORTANT**: This project should be converted from the original Briar git repository to a clean Mycel repository owned by Quantum Research Pty Ltd.

**Complete Git Setup Guide**: `docs/GIT-INITIALIZATION-GUIDE.md` provides step-by-step instructions for:
1. **Backing up current state** (safety first)
2. **Removing old Briar git history** (clean slate)
3. **Creating new GitHub repository**: `https://github.com/quantumresearch/mycel`
4. **Establishing git commit policy** for rebranding phases

### Git Commit Policy for Rebranding

#### **Required Format for All Rebranding Commits:**
```
[PHASE-X] Brief description of changes

Detailed description of what was changed and why.
Reference to specific files and line numbers when applicable.

Testing:
- ✅ Build: ./gradlew build
- ✅ Tests: ./gradlew test  
- ✅ APK: Installs and runs correctly

Phase Progress: X/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

#### **Commit After Every Phase:**
- **Phase 1**: String resources only (`[PHASE-1] Update string resources from Briar to Mycel`)
- **Phase 2**: Application ID (`[PHASE-2] Update Android application ID to com.quantumresearch.mycel`)
- **Phase 3**: Bramble packages (`[PHASE-3] Rename bramble packages to com.quantumresearch.mycel.infrastructure`)
- **Phase 4**: Briar packages (`[PHASE-4] Rename briar packages to com.quantumresearch.mycel.app`)
- **Phase 5**: Configuration (`[PHASE-5] Update deep links and configuration files`)
- **Phase 6**: Visual assets (`[PHASE-6] Replace Briar visual assets with Mycel branding`)
- **Phase 7**: Documentation (`[PHASE-7] Final documentation and metadata updates`)

#### **Testing Script:**
Each phase must pass the testing verification:
```bash
./test-phase.sh  # Created by git initialization guide
```

#### **Progress Tracking:**
Update `REBRANDING-PROGRESS.md` after each phase completion.

### Git Commands Reference
```bash
# Stage and commit after successful phase
git add .
git commit -m "[PHASE-X] Description..."

# Push to GitHub
git push origin main

# Check status and recent commits
git status
git log --oneline -5
```

**Critical Rule**: NEVER commit without successful testing. Each commit must represent a working, tested state of the application.