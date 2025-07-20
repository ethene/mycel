# Module Structure Analysis

## Module Hierarchy Overview

The Briar project is organized into a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                    │
├─────────────────────────────────────────────────────────┤
│  briar-android     │  briar-headless  │                 │
│  (Android UI)      │  (REST API/CLI)  │                 │
├─────────────────────────────────────────────────────────┤
│               briar-core + briar-api                    │
│         (Messaging, Forums, Blogs, Groups)             │
├─────────────────────────────────────────────────────────┤
│                Infrastructure Layer                     │
├─────────────────────────────────────────────────────────┤
│  bramble-android   │  bramble-java    │                 │
│  (Android impl)    │  (Desktop impl)  │                 │
├─────────────────────────────────────────────────────────┤
│              bramble-core + bramble-api                 │
│       (Networking, Crypto, Database, Sync)             │
└─────────────────────────────────────────────────────────┘
```

## Detailed Module Analysis

### Foundation Layer (Bramble)

#### bramble-api
- **Type**: Java Library (Java 8)
- **Purpose**: Core API definitions and interfaces
- **Size**: Minimal - interface definitions only
- **Key Components**:
  - Plugin interfaces for transport abstraction
  - Sync protocol definitions
  - Database abstraction interfaces
  - Cryptographic operation interfaces
  - Event system definitions
- **Dependencies**: 
  - Null safety annotations
  - JSR305 for static analysis
  - Dagger for dependency injection
- **Special Notes**: 
  - Exports test artifacts for downstream modules
  - Animal Sniffer ensures Java 8 compatibility

#### bramble-core
- **Type**: Java Library (Java 8)
- **Purpose**: Core infrastructure implementation
- **Key Components**:
  - Database implementation (H2 for compatibility)
  - Cryptographic implementations (BouncyCastle, EdDSA, Curve25519)
  - Networking stack (OkHttp, UPnP, SOCKS)
  - Synchronization protocol
  - Transport abstraction layer
  - Onion routing integration
- **Major Dependencies**:
  - `bramble-api` (implements interfaces)
  - `org.bouncycastle:bcprov-jdk15to18:1.71`
  - `net.i2p.crypto:eddsa:0.2.0`
  - `org.whispersystems:curve25519-java:0.5.0`
  - `com.h2database:h2:1.4.192` (legacy for Java 6 support)
  - `com.squareup.okhttp3:okhttp:4.12.0`
  - `org.briarproject:onionwrapper-core:0.1.3`
- **Security Focus**: Implements all cryptographic primitives and secure protocols

#### bramble-android
- **Type**: Android Library
- **Purpose**: Android-specific Bramble implementation
- **Android Config**:
  - Compile SDK: 34, Build Tools: 34.0.0
  - Min SDK: 21, Target SDK: 33
  - Version: 1.5.14
- **Key Components**:
  - Android-specific transport implementations
  - Tor binary integration for Android
  - Battery optimization handling
  - Android networking adaptations
- **Dependencies**:
  - `bramble-api` and `bramble-core`
  - `org.briarproject:onionwrapper-android:0.1.3`
  - `org.briarproject:tor-android:0.4.8.14`
  - `org.briarproject:lyrebird-android:0.5.0-3`
  - `org.briarproject:dont-kill-me-lib:0.2.8`

#### bramble-java
- **Type**: Java Library (Java 8)
- **Purpose**: Desktop/server Bramble implementation
- **Key Components**:
  - Desktop-specific transport implementations
  - Bluetooth support (BlueCove)
  - Serial communication (JSSC)
  - Native library access (JNA)
- **Dependencies**:
  - `bramble-core` (builds upon core)
  - `net.java.dev.jna:jna:5.13.0`
  - `net.java.dev.jna:jna-platform:5.13.0`
  - `org.briarproject:onionwrapper-java:0.1.3`
- **Custom Libraries**:
  - BlueCove Bluetooth stack (modified)
  - JSSC serial communication library

### Application Layer (Briar)

#### briar-api
- **Type**: Java Library (Java 8)
- **Purpose**: High-level application API definitions
- **Key Components**:
  - Messaging interfaces
  - Forum and blog interfaces
  - Group management interfaces
  - Sharing and introduction interfaces
- **Dependencies**: Only `bramble-api` (minimal)
- **Design**: Pure interface definitions for application features

#### briar-core
- **Type**: Java Library (Java 8)
- **Purpose**: Application feature implementations
- **Key Components**:
  - Private messaging implementation
  - Forum and blog functionality
  - Group management
  - RSS feed processing
  - Content sharing protocols
  - Contact introduction system
- **Major Dependencies**:
  - `briar-api` and `bramble-core`
  - `com.rometools:rome:1.18.0` (RSS processing)
  - `org.jsoup:jsoup:1.15.3` (HTML parsing)
  - `com.squareup.okhttp3:okhttp:4.12.0`
  - `org.jdom:jdom2:2.0.6.1` (XML processing)

#### briar-android
- **Type**: Android Application
- **Purpose**: Main Android user interface
- **Android Config**:
  - Application ID: `org.briarproject.briar.android`
  - Min SDK: 21, Target SDK: 34
  - Version: 1.5.14 (versionCode 10514)
- **Build Variants**:
  - **Flavors**: `official`, `screenshot`
  - **Types**: `debug` (with `.debug` suffix), `release`
- **UI Dependencies**:
  - Modern AndroidX libraries (Fragment, Preference, Material)
  - `com.github.bumptech.glide:glide:4.16.0` (image loading)
  - `com.github.chrisbanes:PhotoView:2.3.0` (image viewing)
  - `com.google.zxing:core:3.3.3` (QR codes)
  - `de.hdodenhof:circleimageview:3.1.0` (profile images)
  - `com.vanniktech:emoji:0.9.0` (emoji support)
- **Testing**:
  - Comprehensive test suite with Espresso, Robolectric, Mockito
  - Screenshot testing with Fastlane
  - Test orchestrator for reliable testing

#### briar-headless
- **Type**: Java Application with Kotlin
- **Purpose**: Command-line and REST API server
- **Key Components**:
  - REST API endpoints
  - Command-line interface
  - Multi-platform distribution
- **Dependencies**:
  - All core modules for full functionality
  - `org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4`
  - `io.javalin:javalin:5.6.1` (web framework)
  - `com.github.ajalt.clikt:clikt:4.2.0` (CLI)
- **Distribution**: 
  - Multi-platform JAR with platform-specific binaries
  - Supports Linux (aarch64, armhf, x86_64), Windows x86_64, macOS (aarch64, x86_64)

### Testing Module

#### mailbox-integration-tests
- **Type**: Java Library (Test-only)
- **Purpose**: Integration tests for mailbox functionality
- **Conditional**: Only included when `MAILBOX_INTEGRATION_TESTS=true`
- **Dependencies**:
  - Test dependencies on core modules
  - Integration with `mailbox-core` and `mailbox-lib`
  - HTTP client and logging for testing

## Module Dependencies Graph

```
bramble-api
    ↓
bramble-core ──┬─→ bramble-android
    ↓          └─→ bramble-java
briar-api
    ↓
briar-core ────┬─→ briar-android
    ↓          └─→ briar-headless
mailbox-integration-tests
```

## Key Design Patterns

### 1. API/Implementation Separation
- Each layer has separate `-api` and `-core` modules
- Enables loose coupling and easier testing
- Allows multiple implementations of same interfaces

### 2. Platform Abstraction
- Core logic is platform-independent
- Platform-specific modules (`-android`, `-java`) handle OS integration
- Enables code reuse across desktop and mobile

### 3. Layered Architecture
- Clear separation between infrastructure (Bramble) and application (Briar)
- Each layer builds upon lower layers
- Dependencies only flow downward

### 4. Modular Testing
- Each module exports test artifacts for reuse
- Comprehensive test coverage at each layer
- Platform-specific testing strategies

## Rebranding Implications

### High Impact Modules (Require Extensive Changes)
- **briar-android**: All UI strings, themes, icons, package names
- **briar-headless**: API documentation, CLI commands
- Application IDs and package names throughout

### Medium Impact Modules (Moderate Changes)
- **briar-core**: Configuration strings, default values
- **bramble-android**: Android-specific configurations

### Low Impact Modules (Minimal Changes)
- **bramble-api/core**: Core protocols remain unchanged
- **briar-api**: Interface definitions usually unchanged
- **bramble-java**: Desktop implementations rarely need changes

### Files Requiring Updates
- `build.gradle` files (application IDs, version names)
- `AndroidManifest.xml` (package names, app names)
- Resource files (`strings.xml`, themes, styles)
- Configuration files and properties
- Documentation and README files