# Development Setup and Environment

## Overview

This document provides comprehensive setup instructions for developing the Briar/Mycel application, including all required tools, SDKs, and environmental configurations for both Android and desktop development.

## System Requirements

### Operating System Support

**Primary Development Platforms**:
- **Linux**: Ubuntu 20.04+ (recommended), Debian 11+, Fedora 35+
- **macOS**: macOS 12 (Monterey) or later
- **Windows**: Windows 10/11 with WSL2 (Linux subsystem recommended)

**Hardware Requirements**:
- **RAM**: 8GB minimum, 16GB recommended
- **Storage**: 50GB free space for SDK, tools, and source code
- **CPU**: x64 processor, ARM64 supported on Apple Silicon

## Core Development Tools

### 1. Java Development Kit (JDK)

**Required Version**: OpenJDK 17 or Oracle JDK 17

**Installation**:

**Linux (Ubuntu/Debian)**:
```bash
sudo apt update
sudo apt install openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
```

**macOS**:
```bash
# Using Homebrew
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home' >> ~/.zshrc
```

**Windows**:
```bash
# Using Chocolatey
choco install openjdk17
# Or download from https://adoptium.net/
```

**Verification**:
```bash
java -version
javac -version
echo $JAVA_HOME
```

### 2. Android Development Environment

#### Android Studio

**Required Version**: Android Studio Giraffe (2022.3.1) or later

**Download**: https://developer.android.com/studio

**Installation Components**:
- Android SDK Platform 34 (Android 14)
- Android SDK Build-Tools 34.0.0
- Android SDK Platform-Tools (latest)
- Android SDK Tools (latest)
- Android Emulator (for testing)

#### Android SDK Configuration

**SDK Location** (set in Android Studio or environment):
```bash
export ANDROID_HOME=$HOME/Android/Sdk  # Linux/macOS
export ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk  # Windows

# Add to PATH
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
```

**Required SDK Components**:
```bash
# Install via sdkmanager
sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"
sdkmanager "cmake;3.22.1" "ndk;25.1.8937393"
sdkmanager "system-images;android-34;google_apis;x86_64"  # For emulator
```

#### Android Emulator Setup

**Create Virtual Device**:
```bash
# Create AVD (Android Virtual Device)
avdmanager create avd -n "Mycel_Test" -k "system-images;android-34;google_apis;x86_64"

# Start emulator
emulator -avd Mycel_Test
```

**Recommended AVD Configuration**:
- **API Level**: 34 (Android 14)
- **Target**: Google APIs
- **ABI**: x86_64 (for development), arm64-v8a (for Apple Silicon)
- **RAM**: 4GB
- **Storage**: 8GB

### 3. Git and Version Control

**Git Installation**:
```bash
# Linux
sudo apt install git

# macOS
brew install git

# Windows
winget install Git.Git
```

**Git Configuration**:
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
git config --global init.defaultBranch main
```

### 4. Build Tools

#### Gradle

**Version**: Gradle 7.6.1 (via Gradle Wrapper - no separate installation needed)

**Gradle Properties** (`~/.gradle/gradle.properties`):
```properties
# Performance optimization
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g

# Android optimizations
android.useAndroidX=true
android.enableJetifier=true
```

#### Kotlin (for mycel-headless)

**Installation** (if not using IDE):
```bash
# Linux/macOS
curl -s https://get.sdkman.io | bash
sdk install kotlin

# Homebrew (macOS)
brew install kotlin
```

## IDE Setup

### Android Studio Configuration

#### Essential Plugins

**Pre-installed Plugins**:
- Android APK Support
- Android NDK Support
- Git Integration
- Gradle Support

**Recommended Additional Plugins**:
- **SonarLint**: Code quality analysis
- **CheckStyle-IDEA**: Code style verification
- **Markdown Navigator**: Documentation editing
- **Database Navigator**: Database inspection

#### Code Style Configuration

**Import Code Style**:
1. File → Settings → Editor → Code Style
2. Import from `.idea/codeStyles/` (if available)
3. Or configure manually:
   - **Java**: 4 spaces, no tabs
   - **XML**: 4 spaces, wrap at 120 characters
   - **Kotlin**: Official Kotlin style guide

#### Build Configuration

**Memory Settings** (Help → Edit Custom VM Options):
```
-Xmx4096m
-XX:ReservedCodeCacheSize=512m
-XX:MaxMetaspaceSize=1024m
```

### Alternative IDEs

#### IntelliJ IDEA

**Setup for Java/Kotlin Development**:
- Ultimate Edition recommended for full Android support
- Community Edition sufficient for spore-core/mycel-core work

#### Visual Studio Code

**Extensions for Briar Development**:
- Extension Pack for Java
- Android iOS Emulator
- Gradle for Java
- Git Graph

## Source Code Setup

### Repository Cloning

```bash
# Clone the repository
git clone https://github.com/briarproject/briar.git mycel
cd mycel

# Switch to appropriate branch (if needed)
git checkout master

# Initialize submodules (if any)
git submodule update --init --recursive
```

### Build Verification

#### Initial Build Test

```bash
# Verify Gradle wrapper works
./gradlew --version

# Clean build to verify setup
./gradlew clean build

# Android debug build
./gradlew :mycel-android:assembleDebug

# Run tests
./gradlew test
```

**Expected Build Time**:
- **First Build**: 10-20 minutes (downloads dependencies)
- **Incremental Builds**: 2-5 minutes
- **Clean Builds**: 5-10 minutes

#### Android Device Testing

**USB Debugging Setup**:
1. Enable Developer Options on Android device
2. Enable USB Debugging
3. Connect device via USB
4. Accept debugging authorization prompt

**Device Verification**:
```bash
# List connected devices
adb devices

# Install debug APK
./gradlew :mycel-android:installDebug

# View logs
adb logcat | grep Briar
```

## Development Dependencies

### Runtime Dependencies

#### Tor Integration

**Linux Dependencies**:
```bash
# Ubuntu/Debian
sudo apt install tor obfs4proxy

# Fedora
sudo dnf install tor obfs4proxy
```

**macOS Dependencies**:
```bash
brew install tor
```

#### Bluetooth Development (Linux)

```bash
# BlueZ stack for Bluetooth
sudo apt install bluez bluez-tools libbluetooth-dev

# Development libraries
sudo apt install libbluetooth3-dev
```

#### Database Tools

**H2 Database Console** (for debugging):
```bash
# Download H2 console JAR
wget https://repo1.maven.org/maven2/com/h2database/h2/2.1.214/h2-2.1.214.jar

# Start console
java -cp h2-2.1.214.jar org.h2.tools.Console
```

### Development Libraries

#### Native Libraries (Linux)

```bash
# JNA (Java Native Access) dependencies
sudo apt install build-essential

# For Bluetooth support
sudo apt install libbluetooth-dev

# For audio support (future features)
sudo apt install libasound2-dev
```

## Environment Configuration

### Environment Variables

**Required Variables** (add to `~/.bashrc` or `~/.zshrc`):
```bash
# Java
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Android
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools

# Gradle
export GRADLE_OPTS="-Xmx2g -Dorg.gradle.daemon=true"

# Development flags
export OPTIONAL_TESTS=true  # Enable optional test suites
```

### Project Configuration

#### Local Properties

**File**: `local.properties`
```properties
# Android SDK location
sdk.dir=/home/username/Android/Sdk

# NDK location (if using native code)
ndk.dir=/home/username/Android/Sdk/ndk/25.1.8937393

# Signing configuration (for release builds)
keystore.path=/path/to/release.keystore
keystore.password=your_keystore_password
key.alias=your_key_alias
key.password=your_key_password
```

#### Gradle Properties

**File**: `gradle.properties` (project-specific)
```properties
# Build optimization
org.gradle.jvmargs=-Xmx2g
org.gradle.parallel=true

# Android configuration
android.useAndroidX=true
android.enableJetifier=true

# Feature flags
briar.mailbox_integration_tests=false

# Debug options
briar.debug.networking=false
briar.debug.database=false
```

## Database Setup

### Development Database

**H2 Database** (default for development):
- **Location**: `~/.local/share/briar/` (Linux), `~/Library/Application Support/briar/` (macOS)
- **Encryption**: AES-encrypted by default
- **Console Access**: Available via H2 console tool

**Database Reset** (for clean testing):
```bash
# Stop application
# Delete database files
rm -rf ~/.local/share/briar/db*

# Restart application (creates new database)
```

### Test Database Configuration

**In-Memory Testing**:
```java
// Test configuration uses in-memory H2
@TestConfiguration
public class TestDatabaseConfig {
    @Bean
    public DatabaseConfig getDatabaseConfig() {
        return new H2DatabaseConfig(
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            "test_user",
            "test_password"
        );
    }
}
```

## Testing Environment

### Unit Testing

**Test Execution**:
```bash
# Run all unit tests
./gradlew test

# Run specific module tests
./gradlew :spore-core:test
./gradlew :mycel-core:test

# Run with detailed output
./gradlew test --info
```

### Android Instrumentation Testing

**Device Testing**:
```bash
# Run on connected device/emulator
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.briarproject.briar.android.ExampleTest
```

### Performance Testing

**Memory Profiling**:
- Android Studio Profiler
- LeakCanary integration (for memory leaks)
- Custom memory monitoring in debug builds

## Development Workflow

### Code Style and Quality

#### Pre-commit Hooks

**Setup** (optional but recommended):
```bash
# Install pre-commit framework
pip install pre-commit

# Install hooks (if .pre-commit-config.yaml exists)
pre-commit install

# Manual formatting check
./gradlew checkStyle
```

#### Code Analysis

**Static Analysis Tools**:
```bash
# SpotBugs (security and bug detection)
./gradlew spotbugsMain

# PMD (code quality)
./gradlew pmdMain

# Android Lint
./gradlew :mycel-android:lint
```

### Debugging Setup

#### Android Debugging

**Debug Build Configuration**:
- **Debuggable**: true
- **Minification**: false (debug builds)
- **ProGuard**: disabled for debugging
- **Logging**: verbose level enabled

**Network Debugging**:
```java
// Enable network logging (debug builds only)
if (BuildConfig.DEBUG) {
    System.setProperty("briar.debug.networking", "true");
}
```

#### Desktop Debugging

**JVM Debug Arguments**:
```bash
# Remote debugging
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar mycel-headless.jar

# Memory debugging
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/mycel-heap.hprof -jar mycel-headless.jar
```

## Troubleshooting Common Setup Issues

### Build Issues

#### Gradle Sync Failures

**Solution**:
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Refresh dependencies
./gradlew --refresh-dependencies build
```

#### Android SDK Issues

**Solution**:
```bash
# Update all SDK components
sdkmanager --update

# Accept all licenses
sdkmanager --licenses
```

#### Memory Issues

**Solution**:
```bash
# Increase Gradle memory
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"

# Increase Android Studio memory
# Edit studio.vmoptions file
```

### Runtime Issues

#### Database Connection Errors

**Symptoms**: `DbException: Database connection failed`

**Solution**:
```bash
# Check database file permissions
ls -la ~/.local/share/briar/

# Reset database (development only)
rm -rf ~/.local/share/briar/db*
```

#### Network/Transport Issues

**Symptoms**: Tor connection failures, Bluetooth not working

**Solution**:
```bash
# Linux: Check Tor service
sudo systemctl status tor

# Check Bluetooth service
sudo systemctl status bluetooth

# Verify permissions
groups $USER | grep bluetooth
```

## IDE-Specific Setup

### Android Studio

**Recommended Settings**:
- **Build**: Use Gradle offline mode for faster builds
- **Editor**: Enable auto-import for optimized imports
- **Memory**: Increase IDE heap size to 4GB
- **Indexing**: Exclude build directories from indexing

### IntelliJ IDEA

**Project Import**:
1. Open → Select root directory
2. Import as Gradle project
3. Use auto-import
4. Set Project SDK to JDK 17

### VS Code

**Workspace Configuration** (`.vscode/settings.json`):
```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.home": "/usr/lib/jvm/java-17-openjdk-amd64",
    "gradle.nestedProjects": true,
    "files.exclude": {
        "**/build": true,
        "**/.gradle": true
    }
}
```

## Performance Optimization

### Build Performance

**Gradle Optimization**:
```properties
# ~/.gradle/gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
```

**Android Studio Optimization**:
- Enable Instant Run (for faster deployment)
- Use separate JVM process for Gradle
- Configure appropriate heap sizes

### Development Database

**Performance Settings**:
```properties
# Faster database operations for development
briar.db.connection_pool_size=10
briar.db.cache_size=256MB
```

This development setup provides a comprehensive foundation for Briar/Mycel development across all target platforms, ensuring consistent build environments and efficient development workflows.