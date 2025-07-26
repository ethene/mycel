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
- **Spore Layer (Infrastructure)**: `spore-*` modules (networking, crypto, sync)
- **Mycel Layer (Application)**: `mycel-*` modules (messaging, forums, blogs)  
- **Android App**: `mycel-android` module

## Project Status

Mycel is an **established secure messaging platform** with comprehensive features and active development roadmap.

### ✅ Completed Implementation
1. **Package Architecture**: Full `com.quantumresearch.mycel.*` structure implemented
2. **Application Structure**: Complete two-layer Spore/Mycel architecture
3. **Multi-Platform Support**: Android, Desktop, and Headless modes
4. **Transport System**: Tor, Bluetooth, Wi-Fi, and Serial/Modem support
5. **Security Implementation**: End-to-end encryption with perfect forward secrecy
6. **Development Infrastructure**: CI/CD pipeline, automated builds, comprehensive documentation

### 🚀 Active Development Areas
1. **Feature Enhancement**: Following comprehensive PRD roadmap (P0-P3 priorities)
2. **Performance Optimization**: Memory usage reduction and sync speed improvements  
3. **Platform Expansion**: iOS development and enhanced desktop applications
4. **Mesh Networking**: LoRa transport and mesh infrastructure development
5. **Economic Layer**: Token wallet and proof-of-relay implementation

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
./gradlew :mycel-android:assembleDebug

# Run tests
./gradlew test
```

### Key Files to Modify for Mycel

#### 1. Primary Build Config
```
mycel-android/build.gradle
- applicationId: "com.quantumresearch.mycel"
```

#### 2. App Name
```
mycel-android/src/main/res/values/strings.xml
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
mycel-android/artwork/
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