# Complete Folder Structure Analysis

## Project Root Structure

```
mycel/
├── 📁 bramble-api/          # Core protocol API definitions
├── 📁 bramble-core/         # Core protocol implementation  
├── 📁 bramble-android/      # Android-specific protocol layer
├── 📁 bramble-java/         # Desktop-specific protocol layer
├── 📁 briar-api/           # Application-level API definitions
├── 📁 briar-core/          # Application logic implementation
├── 📁 briar-android/       # ⭐ Main Android application
├── 📁 briar-headless/      # Headless/server version
├── 📁 briar-mailbox/       # Mailbox server functionality
├── 📁 mailbox-integration-tests/ # Mailbox integration tests
├── 📁 patches/             # Source code patches
├── 📁 libs/                # Custom JAR libraries
├── 📁 gradle/              # Gradle wrapper files
├── 📁 build/               # Build artifacts (generated)
├── 📁 caches/              # Gradle cache (generated)
├── 📁 daemon/              # Gradle daemon (generated)
├── 📁 docs/                # 📚 Our documentation
├── 📄 build.gradle         # Root build configuration
├── 📄 settings.gradle      # Module structure definition
├── 📄 gradle.properties    # Build properties
├── 📄 local.properties     # Local development settings
├── 📄 gradlew             # Gradle wrapper script
├── 📄 README.md           # Project documentation
├── 📄 CLAUDE.md           # Claude AI guidance
└── 📄 LICENSE.txt         # Project license
```

## Critical Modules for Rebranding

### 🔴 briar-android/ (HIGHEST PRIORITY)

The main Android application containing all user-facing elements:

```
briar-android/
├── 📁 artwork/ ⭐ CRITICAL BRANDING ASSETS
│   ├── logo_circle.svg                    # Main logo
│   ├── logo_horizontal_white.svg          # Horizontal logo variant
│   ├── logo_no_text.svg                   # Logo without text
│   ├── logo_vertical_black.svg            # Vertical logo variant
│   ├── navigation_drawer_header.svg       # Navigation header
│   ├── navigation_drawer_header_night.svg # Dark theme header
│   ├── notification_ongoing.svg           # Notification icon
│   ├── trust-indicator.svg                # Security indicator
│   └── [other UI graphics]
│
├── 📁 fastlane/ ⭐ APP STORE ASSETS
│   ├── 📁 metadata/android/
│   │   ├── 📁 en-US/
│   │   │   ├── title.txt                  # App store title
│   │   │   ├── short_description.txt      # Short description
│   │   │   ├── full_description.txt       # Full description
│   │   │   └── changelogs/                # Version changelogs
│   │   └── [30+ other languages]
│   └── Screengrabfile                     # Screenshot automation
│
├── 📁 src/main/ ⭐ MAIN APPLICATION CODE
│   ├── 📄 AndroidManifest.xml ⭐ CRITICAL
│   │   # Contains:
│   │   # - Package name: org.briarproject.briar.android
│   │   # - App name references
│   │   # - Activity declarations
│   │   # - Intent filters (briar:// deep links)
│   │   # - Permissions
│   │
│   ├── 📁 java/org/briarproject/briar/android/ ⭐ PACKAGE STRUCTURE
│   │   ├── 📁 activity/              # Activities (screens)
│   │   ├── 📁 attachment/            # File attachments
│   │   ├── 📁 blog/                  # Blog functionality
│   │   ├── 📁 contact/               # Contact management
│   │   ├── 📁 conversation/          # Chat interface
│   │   ├── 📁 forum/                 # Forum functionality
│   │   ├── 📁 fragment/              # UI fragments
│   │   ├── 📁 login/                 # Authentication
│   │   ├── 📁 navdrawer/             # Navigation drawer
│   │   ├── 📁 privategroup/          # Private groups
│   │   ├── 📁 settings/              # App settings
│   │   ├── 📁 sharing/               # Content sharing
│   │   ├── 📁 splash/                # Splash screen
│   │   ├── 📁 util/                  # Utilities
│   │   └── BriarApplication.java     # Main app class
│   │
│   ├── 📁 res/ ⭐ ANDROID RESOURCES
│   │   ├── 📁 drawable*/             # Vector graphics, icons
│   │   ├── 📁 layout*/               # UI layouts
│   │   ├── 📁 mipmap*/              # App launcher icons ⭐
│   │   ├── 📁 values*/              # Strings, colors, styles ⭐
│   │   │   ├── strings.xml          # English strings
│   │   │   ├── colors.xml           # Color definitions
│   │   │   ├── styles.xml           # UI styles
│   │   │   └── themes.xml           # App themes
│   │   ├── 📁 values-ar/            # Arabic translations
│   │   ├── 📁 values-de/            # German translations
│   │   ├── 📁 values-es/            # Spanish translations
│   │   ├── 📁 values-fr/            # French translations
│   │   ├── 📁 values-it/            # Italian translations
│   │   ├── 📁 values-ja/            # Japanese translations
│   │   ├── 📁 values-ru/            # Russian translations
│   │   ├── 📁 values-zh-rCN/        # Chinese Simplified
│   │   └── [27+ other language dirs]
│   │
│   ├── 📄 ic_launcher-playstore.png ⭐    # Play Store icon
│   └── 📄 ic_launcher_round-web.png ⭐    # Round launcher icon
│
├── 📁 src/androidTest/              # Android instrumentation tests
├── 📁 src/test/                     # Unit tests
├── 📄 build.gradle ⭐ BUILD CONFIG
│   # Contains:
│   # - applicationId "org.briarproject.briar.android"
│   # - versionName "1.5.14"
│   # - versionCode 10514
│   # - Dependencies
│   # - Build variants (debug/release, official/screenshot)
│
└── 📄 proguard-rules.txt           # Code obfuscation rules
```

### 🟡 bramble-android/ (MEDIUM PRIORITY)

Android-specific protocol implementation:

```
bramble-android/
├── 📁 src/main/
│   ├── 📁 java/org/briarproject/bramble/ ⭐ PACKAGE STRUCTURE
│   │   ├── 📁 android/              # Android adaptations
│   │   └── [protocol implementations]
│   │
│   └── 📁 res/values/strings.xml    # Android string resources
│
└── 📄 build.gradle                  # Build configuration
```

### 🟡 briar-headless/ (MEDIUM PRIORITY)

Headless/server version:

```
briar-headless/
├── 📁 src/main/
│   ├── 📁 java/org/briarproject/briar/headless/ ⭐ PACKAGE STRUCTURE
│   │   ├── 📁 rest/                 # REST API endpoints
│   │   └── Main.java                # Main entry point
│   │
│   └── 📁 kotlin/org/briarproject/briar/headless/ # Kotlin components
│
├── 📄 README.md                     # Headless documentation
└── 📄 build.gradle                  # Build configuration
```

## Core Protocol Modules (Lower Priority)

### 🟢 bramble-api/ & bramble-core/

Protocol layer interface definitions and implementations:

```
bramble-api/
└── src/main/java/org/briarproject/bramble/api/ ⭐ PACKAGE STRUCTURE
    ├── 📁 contact/                  # Contact management APIs
    ├── 📁 crypto/                   # Cryptographic APIs
    ├── 📁 db/                       # Database APIs
    ├── 📁 event/                    # Event system APIs
    ├── 📁 plugin/                   # Transport plugin APIs
    ├── 📁 sync/                     # Synchronization APIs
    └── [other core APIs]

bramble-core/
└── src/main/java/org/briarproject/bramble/ ⭐ PACKAGE STRUCTURE
    ├── 📁 crypto/                   # Crypto implementations
    ├── 📁 db/                       # Database implementations
    ├── 📁 plugin/                   # Transport implementations
    ├── 📁 sync/                     # Sync implementations
    └── [other implementations]
```

### 🟢 briar-api/ & briar-core/

Application layer definitions and implementations:

```
briar-api/
└── src/main/java/org/briarproject/briar/api/ ⭐ PACKAGE STRUCTURE
    ├── 📁 blog/                     # Blog APIs
    ├── 📁 forum/                    # Forum APIs
    ├── 📁 messaging/                # Messaging APIs
    ├── 📁 privategroup/             # Private group APIs
    └── [other app APIs]

briar-core/
└── src/main/java/org/briarproject/briar/ ⭐ PACKAGE STRUCTURE
    ├── 📁 blog/                     # Blog implementations
    ├── 📁 forum/                    # Forum implementations
    ├── 📁 messaging/                # Messaging implementations
    ├── 📁 privategroup/             # Private group implementations
    └── [other implementations]
```

## Configuration and Build Files

### Root Level Configuration

```
📄 settings.gradle ⭐ MODULE STRUCTURE
# Defines which modules are included in the build
# Contains module path mappings

📄 build.gradle ⭐ ROOT BUILD CONFIG
# Common build configuration for all modules
# Dependency versions and repositories

📄 gradle.properties ⭐ BUILD PROPERTIES
# JVM memory settings
# Android build options
# Feature flags (mailbox_integration_tests)

📄 local.properties 🔵 LOCAL DEVELOPMENT
# SDK paths and local developer settings
# Not tracked in git, safe to ignore for rebranding
```

### Generated Directories (Clean Before Rebranding)

```
📁 build/                           # Generated build artifacts
📁 caches/                          # Gradle cache
📁 daemon/                          # Gradle daemon files
📁 native/                          # Native library cache
📁 wrapper/                         # Gradle wrapper cache
📁 kotlin-profile/                  # Kotlin compilation profiles
📄 android.lock                     # Android build lock file
```

## Rebranding Impact Summary

### 🔴 CRITICAL (App Breaking Changes)
- **Package Names**: All `org.briarproject.*` → new package structure
- **Application ID**: `org.briarproject.briar.android` → new app ID
- **AndroidManifest.xml**: Package declarations and app name
- **Main App Name**: `app_name` string in all languages

### 🟡 HIGH (User Visible Changes)
- **Visual Assets**: All SVG logos and PNG icons
- **String Resources**: 30+ language translations
- **App Store Metadata**: Titles and descriptions
- **Deep Links**: `briar://` scheme handlers

### 🟢 MEDIUM (Internal References)
- **Java/Kotlin Classes**: Class names with "Briar" references
- **Resource Names**: Layout and drawable file names
- **Build Scripts**: Module names and dependencies

### 🔵 LOW (Development/Build)
- **Generated Files**: Can be cleaned and regenerated
- **Documentation**: README and development docs
- **Test Files**: Internal test references