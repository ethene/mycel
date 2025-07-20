# Mycel Quick Start Guide

## What is Mycel?

**Mycel** is a secure, decentralized messaging application developed by **Quantum Research Pty Ltd**. It provides peer-to-peer communication without relying on central servers, supporting multiple transport methods including Tor, Bluetooth, and Wi-Fi.

## Key Information

### Brand Details
- **Application Name**: Mycel
- **Developer**: Quantum Research Pty Ltd
- **Original Project**: Rebranded from Briar messaging app
- **Package Structure**: `com.quantumresearch.mycel.*`
- **Application ID**: `com.quantumresearch.mycel`

### Architecture
- **Infrastructure Layer**: `com.quantumresearch.mycel.infrastructure.*` (was bramble)
- **Application Layer**: `com.quantumresearch.mycel.app.*` (was briar)
- **Android App**: `com.quantumresearch.mycel.app.android.*`

## Current Status

This codebase is currently in **rebranding phase** from Briar to Mycel. The following elements need updating:

### 🔴 Critical Changes Required
1. **Package Names**: All `org.briarproject.*` → `com.quantumresearch.mycel.*`
2. **Application ID**: Update in `briar-android/build.gradle`
3. **App Name**: Update "Briar" → "Mycel" in all string resources
4. **AndroidManifest.xml**: Update package and class references

### 🟡 High Priority Changes
1. **Visual Assets**: Replace all logos in `briar-android/artwork/`
2. **App Icons**: Update launcher icons in all density folders
3. **String Resources**: Update in 30+ language files
4. **Deep Links**: Change from `briar://` to `mycel://`

### 🟢 Medium Priority Changes
1. **URLs**: Update to Quantum Research domains
2. **Build Configuration**: Update signing and distribution
3. **Documentation**: Update all references

## Quick Development Setup

### Prerequisites
- Android Studio
- Java 8 or higher
- Android SDK (API 21-34)

### Building Mycel
```bash
# Build the project
./gradlew build

# Build Android APK
./gradlew :briar-android:assembleDebug

# Run tests
./gradlew test
```

### Key Files to Modify for Mycel

#### 1. Primary Build Config
```
briar-android/build.gradle
- applicationId: "com.quantumresearch.mycel"
```

#### 2. App Name
```
briar-android/src/main/res/values/strings.xml
- app_name: "Mycel"
```

#### 3. Package Structure
```
All Java/Kotlin files:
- org.briarproject.bramble.* → com.quantumresearch.mycel.infrastructure.*
- org.briarproject.briar.* → com.quantumresearch.mycel.app.*
```

#### 4. Visual Assets
```
briar-android/artwork/
- Replace all SVG logo files with Mycel branding
```

## Documentation Structure

The complete documentation is in the `docs/` folder:

- **`01-project-overview.md`** - Complete project understanding
- **`13-rebranding-strategy.md`** - Step-by-step Mycel rebranding guide
- **`16-configuration-files.md`** - All files requiring Mycel updates

## Next Steps

1. **Review Documentation**: Read the rebranding strategy document
2. **Set Up Environment**: Prepare development tools and signing certificates
3. **Create Visual Assets**: Design Mycel logos and branding
4. **Begin Package Rename**: Start with automated package name replacement
5. **Update Build Config**: Modify application ID and build settings
6. **Test Thoroughly**: Validate all functionality works with new branding

## Important Notes

- **Preserve Functionality**: All core features must remain unchanged
- **Security Priority**: Maintain all cryptographic and security implementations
- **User Data**: Plan migration strategy for existing Briar users
- **Testing**: Comprehensive testing required at each phase

For detailed implementation instructions, see `docs/13-rebranding-strategy.md`.

---

*Mycel by Quantum Research Pty Ltd - Secure Messaging Without Compromise*