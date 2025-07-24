# Build System Documentation

## Overview

The Briar/Mycel project uses a sophisticated Gradle-based build system with multiple modules, dependency verification, and platform-specific builds. This document provides comprehensive details about the build configuration and processes.

## Gradle Version and Configuration

### Gradle Wrapper
- **Version**: 7.6.1 (as specified in `gradle/wrapper/gradle-wrapper.properties`)
- **Distribution**: `gradle-7.6.1-bin.zip`
- **Wrapper Script**: `./gradlew` (Unix) / `gradlew.bat` (Windows)

### Root Build Configuration

**File**: `build.gradle`

```gradle
// Top-level build file with common configuration
allprojects {
    repositories {
        mavenCentral()    // Primary Maven repository
        google()          // Google/Android repositories
        maven { url "https://jitpack.io" }  // Third-party libraries
    }
    
    afterEvaluate {
        tasks.withType(Test) {
            // Allow test re-runs with optional tests
            outputs.upToDateWhen { System.getenv("OPTIONAL_TESTS") == null }
            
            // Entropy gathering device for security
            systemProperty 'java.security.egd', System.getProperty('java.security.egd')
        }
    }
}
```

### Build Properties

**File**: `gradle.properties`

```properties
# JVM Memory Settings
org.gradle.jvmargs=-Xmx2g

# Android Configuration
android.useAndroidX=true
android.enableJetifier=true

# Feature Flags
briar.mailbox_integration_tests=false

# Dependency Verification
noWitness=androidApis,_internal_aapt2_binary
```

## Dependency Management

### Version Management

**Centralized Versions** (in root `build.gradle`):
```gradle
ext {
    kotlin_version = '1.9.10'
    dagger_version = "2.51.1"
    okhttp_version = "4.12.0"
    jackson_version = "2.13.4"
    tor_version = "0.4.8.14"
    lyrebird_version = "0.5.0-3"
    jsoup_version = '1.15.3'
    bouncy_castle_version = '1.71'
    junit_version = "4.13.2"
    jmock_version = '2.12.0'
    mockwebserver_version = '4.10.0'
    onionwrapper_version = '0.1.3'
}
```

### Dependency Verification (Witness Plugin)

**Security Feature**: All dependencies verified with cryptographic hashes
- **Plugin**: `witness.gradle` (custom Gradle plugin)
- **Configuration**: `libs/gradle-witness.jar`
- **Exclusions**: Some internal Android dependencies excluded from verification

## Module Structure and Dependencies

### Module Hierarchy

```
spore-api (foundation)
    ↓
spore-core → spore-android, spore-java
    ↓
mycel-api
    ↓
mycel-core → mycel-android, mycel-headless
    ↓
mailbox-integration-tests (conditional)
```

### Module Configuration

**File**: `settings.gradle`

```gradle
// Core modules
include ':spore-api'
include ':spore-core'
include ':spore-android'
include ':spore-java'
include ':mycel-api'
include ':mycel-core'
include ':mycel-android'
include ':mycel-headless'

// Conditional mailbox modules
if (ext.has("briar.mailbox_integration_tests") && 
    ext.get("briar.mailbox_integration_tests") == "true"
    || System.env.MAILBOX_INTEGRATION_TESTS) {
    include ':mailbox-integration-tests'
    include(":mailbox-core")
    include(":mailbox-lib")
    project(":mailbox-core").projectDir = file("mycel-mailbox/mailbox-core")
    project(":mailbox-lib").projectDir = file("mycel-mailbox/mailbox-lib")
}
```

## Android Build Configuration

### Main Android Application

**File**: `mycel-android/build.gradle`

```gradle
apply plugin: 'com.android.application'
apply plugin: 'witness'
apply from: 'witness.gradle'

android {
    compileSdk 34
    buildToolsVersion '34.0.0'

    defaultConfig {
        applicationId "org.briarproject.briar.android"
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 10514
        versionName "1.5.14"
        
        buildConfigField "String", "TorVersion", "\"$tor_version\""
        buildConfigField "String", "GitHash",
            "\"${getStdout(['git', 'rev-parse', '--short=7', 'HEAD'], 'No commit hash')}\""
        
        def now = (long) (System.currentTimeMillis() / 1000)
        buildConfigField "Long", "BuildTimestamp",
            "\"${getStdout(['git', 'log', '-n', '1', '--format=%ct'], now)}000L\""
            
        testInstrumentationRunner 'org.briarproject.briar.android.BriarTestRunner'
        testInstrumentationRunnerArguments disableAnalytics: 'true'
    }

    buildTypes {
        debug {
            applicationIdSuffix ".debug"
            shrinkResources false
            minifyEnabled true
            crunchPngs false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 
                         'proguard-rules.txt'
        }
        release {
            shrinkResources true
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 
                         'proguard-rules.txt'
        }
    }

    flavorDimensions "purpose"
    productFlavors {
        official {
            dimension "purpose"
        }
        screenshot {
            dimension "purpose"
            applicationIdSuffix ".screenshot"
        }
    }
}
```

### Build Variants

1. **Debug Builds**:
   - `officialDebug` - Standard debug build
   - `screenshotDebug` - For automated screenshot generation

2. **Release Builds**:
   - `officialRelease` - Production release
   - `screenshotRelease` - Release build for screenshots

### ProGuard Configuration

**File**: `mycel-android/proguard-rules.txt`

```proguard
# Keep classes with main methods
-keepclasseswithmembers class * {
    public static void main(java.lang.String[]);
}

# Keep Briar application classes
-keep class org.briarproject.briar.** { *; }
-keep class org.briarproject.bramble.** { *; }

# Tor integration
-keep class org.briarproject.onionwrapper.** { *; }

# Database drivers
-keep class org.h2.** { *; }
-keep class org.hsqldb.** { *; }

# Jackson JSON processing
-keep class com.fasterxml.jackson.** { *; }

# Bluetooth libraries
-keep class javax.obex.** { *; }
```

## Java/Desktop Build Configuration

### Core Library Modules

**Common Configuration Pattern**:
```gradle
apply plugin: 'java-library'
apply plugin: 'witness'
apply from: 'witness.gradle'

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
    options.compilerArgs << '-Xlint:unchecked'
    options.compilerArgs << '-Xlint:deprecation'
}
```

### Bramble Java Module

**File**: `spore-java/build.gradle`

```gradle
dependencies {
    implementation project(':spore-core')
    
    // Native library access
    implementation 'net.java.dev.jna:jna:5.13.0'
    implementation 'net.java.dev.jna:jna-platform:5.13.0'
    
    // Custom JAR files
    implementation files('libs/bluecove-2.1.1-SNAPSHOT-briar.jar')
    implementation files('libs/bluecove-gpl-2.1.1-SNAPSHOT.jar')
    implementation files('libs/jssc-0.9-briar.jar')
    
    // Onion routing
    implementation "org.briarproject:onionwrapper-java:$onionwrapper_version"
}

test {
    // Native library path for tests
    systemProperty 'java.library.path', 
        System.getProperty('java.library.path') + 
        File.pathSeparator + file('libs').absolutePath
}
```

## Headless Application Build

### Kotlin Integration

**File**: `mycel-headless/build.gradle`

```gradle
apply plugin: 'application'
apply plugin: 'org.jetbrains.kotlin.jvm'

mainClassName = 'org.briarproject.briar.headless.Main'

dependencies {
    // All core modules
    implementation project(':mycel-core')
    implementation project(':spore-java')
    
    // Kotlin runtime
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4'
    
    // Web framework
    implementation 'io.javalin:javalin:5.6.1'
    
    // CLI framework
    implementation 'com.github.ajalt.clikt:clikt:4.2.0'
    
    // Platform-specific binaries
    implementation "org.briarproject:tor-linux-x86_64:$tor_version"
    implementation "org.briarproject:tor-linux-armhf:$tor_version"
    // ... other platform binaries
}
```

### Multi-Platform JAR Generation

```gradle
jar {
    manifest {
        attributes 'Main-Class': mainClassName
    }
    
    // Include all dependencies
    from configurations.runtimeClasspath.collect { 
        it.isDirectory() ? it : zipTree(it) 
    }
    
    // Platform-specific binary filtering
    exclude 'linux-aarch64/**'
    exclude 'linux-armhf/**'
    // ... exclude unused platforms
    
    // Deterministic JAR creation
    preserveFileTimestamps = false
    reproducibleFileOrder = true
}
```

## Testing Configuration

### Android Testing

**Test Types**:
1. **Unit Tests**: `src/test/` - Robolectric-based
2. **Instrumentation Tests**: `src/androidTest/` - Device/emulator tests
3. **Screenshot Tests**: `src/androidTestScreenshot/` - UI screenshot generation

**Test Configuration**:
```gradle
android {
    testOptions {
        unitTests {
            includeAndroidResources = true
            returnDefaultValues = true
        }
        
        animationsDisabled = true
        
        execution 'ANDROIDX_TEST_ORCHESTRATOR'
    }
}

dependencies {
    // Unit testing
    testImplementation "junit:junit:$junit_version"
    testImplementation "org.jmock:jmock:$jmock_version"
    testImplementation "org.jmock:jmock-junit4:$jmock_version"
    testImplementation 'org.robolectric:robolectric:4.8.2'
    
    // Android testing
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.0'
    androidTestImplementation 'androidx.test:runner:1.5.0'
    androidTestImplementation 'androidx.test:rules:1.3.0'
    
    // Test orchestrator
    androidTestUtil 'androidx.test:orchestrator:1.3.0'
}
```

### Java Module Testing

```gradle
dependencies {
    testImplementation "junit:junit:$junit_version"
    testImplementation "org.jmock:jmock:$jmock_version"
    testImplementation "org.jmock:jmock-junit4:$jmock_version"
    testImplementation "org.jmock:jmock-legacy:$jmock_version"
    testImplementation 'net.jodah:concurrentunit:0.4.6'
}

test {
    useJUnit()
    
    // Test system properties
    systemProperty 'java.awt.headless', 'true'
    
    // Memory settings for tests
    minHeapSize = "512m"
    maxHeapSize = "2g"
}
```

## Custom Gradle Tasks

### Git Information Extraction

```gradle
def getStdout = { command, defaultValue ->
    def stdout = new ByteArrayOutputStream()
    try {
        exec {
            commandLine = command
            standardOutput = stdout
        }
        return stdout.toString().trim()
    } catch (Exception ignored) {
        return defaultValue
    }
}
```

### Translation Management

**File**: `mycel-android/build.gradle`

```gradle
// Custom task for translation verification
task verifyTranslations {
    doLast {
        // Verify all string resources are properly translated
        def baseDir = file('src/main/res/values')
        def translationDirs = fileTree('src/main/res').matching {
            include 'values-*/'
        }
        
        // Implementation for translation verification
    }
}
```

## Build Optimization

### Parallel Builds

```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
```

### Memory Optimization

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError
```

### Build Cache

- **Local Cache**: Enabled for faster rebuilds
- **Remote Cache**: Not configured (could be added for CI/CD)

## Continuous Integration Considerations

### Environment Variables

- `MAILBOX_INTEGRATION_TESTS`: Enable mailbox testing
- `OPTIONAL_TESTS`: Control test execution
- `ANDROID_HOME`: Android SDK location

### Build Commands

```bash
# Clean build
./gradlew clean build

# Android APK build
./gradlew :mycel-android:assembleDebug
./gradlew :mycel-android:assembleRelease

# Run all tests
./gradlew test
./gradlew connectedAndroidTest

# Headless JAR build
./gradlew :mycel-headless:jar

# Dependency verification
./gradlew check
```

## Rebranding Build Implications

### Build Configuration Changes

1. **Application ID**: Update in `mycel-android/build.gradle`
2. **Version Reset**: Start fresh version numbering for Mycel
3. **Signing Configuration**: New signing keys for Quantum Research
4. **Package Names**: Update throughout all modules

### Mycel Build Updates Required

```gradle
// mycel-android/build.gradle
android {
    defaultConfig {
        applicationId "com.quantumresearch.mycel"  // NEW
        versionCode 1                              // RESET
        versionName "1.0.0"                        // NEW
    }
}
```

### Build Script Modifications

- Update ProGuard rules with new package names
- Modify Git hash/timestamp generation if needed
- Update any hardcoded "briar" references in build scripts
- Configure new signing keys and certificates

The build system is well-structured and will support the Mycel rebranding with minimal configuration changes, primarily focused on package names and application identifiers.