# Configuration Files Reference

## Overview

This document catalogs all configuration files that require updates during rebranding, organized by priority and impact level.

## Critical Configuration Files (🔴 App Breaking)

### 1. Android Application Manifest

**File**: `briar-android/src/main/AndroidManifest.xml`

**Changes Required**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.quantumresearch.mycel">  <!-- CHANGE: Mycel package name -->

    <application
        android:name="com.quantumresearch.mycel.app.android.MycelApplication"  <!-- CHANGE: Mycel app class -->
        android:allowBackup="false"
        android:label="@string/app_name"  <!-- Points to Mycel app name -->
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/MycelTheme">  <!-- Mycel theme -->

        <!-- Main launcher activity -->
        <activity
            android:name="com.quantumresearch.mycel.app.android.splash.SplashScreenActivity"  <!-- CHANGE -->
            android:exported="true"
            android:theme="@style/SplashScreenTheme">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Deep link handling -->
        <activity
            android:name="com.quantumresearch.mycel.app.android.contact.AddContactActivity"  <!-- CHANGE -->
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="mycel" />  <!-- CHANGE: Mycel scheme -->
            </intent-filter>
        </activity>

        <!-- Service declarations with new package names -->
        <service
            android:name="com.quantumresearch.mycel.app.android.MycelService"  <!-- CHANGE -->
            android:exported="false" />
    </application>
</manifest>
```

### 2. Primary Build Configuration

**File**: `briar-android/build.gradle`

**Critical Changes**:
```gradle
android {
    compileSdk 34
    buildToolsVersion '34.0.0'

    defaultConfig {
        applicationId "com.quantumresearch.mycel"  // MYCEL CHANGE
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 1                              // START FRESH
        versionName "1.0.0"                        // NEW VERSION
        
        // Update build config fields
        buildConfigField "String", "GitHash",
            "\"${getStdout(['git', 'rev-parse', '--short=7', 'HEAD'], 'No commit hash')}\""
        
        testInstrumentationRunner 'com.quantumresearch.mycel.app.android.MycelTestRunner'  // CHANGE
    }

    // Update build variants if needed
    buildTypes {
        debug {
            applicationIdSuffix ".debug"
            resValue "string", "app_name", "Mycel Debug"  // CHANGE
        }
        release {
            resValue "string", "app_name", "Mycel"        // CHANGE
        }
    }

    // Update product flavors
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

dependencies {
    // All dependencies remain the same
    // Package names in source code will be updated separately
}
```

### 3. Module Structure Definition

**File**: `settings.gradle`

**Potential Changes**:
```gradle
// Module inclusions (names may change)
include ':infrastructure-api'      // Was bramble-api
include ':infrastructure-core'     // Was bramble-core
include ':infrastructure-android'  // Was bramble-android
include ':infrastructure-java'     // Was bramble-java
include ':app-api'                 // Was briar-api
include ':app-core'                // Was briar-core
include ':app-android'             // Was briar-android
include ':app-headless'            // Was briar-headless

// Mailbox modules (conditional)
if (ext.has("newbrand.mailbox_integration_tests") && 
    ext.get("newbrand.mailbox_integration_tests") == "true"
    || System.env.MAILBOX_INTEGRATION_TESTS) {
    include ':mailbox-integration-tests'
    include(":mailbox-core")
    include(":mailbox-lib")
    project(":mailbox-core").projectDir = file("newbrand-mailbox/mailbox-core")
    project(":mailbox-lib").projectDir = file("newbrand-mailbox/mailbox-lib")
}
```

## High Priority Configuration Files (🟡 User Visible)

### 4. Primary String Resources

**File**: `briar-android/src/main/res/values/strings.xml`

**Key Changes**:
```xml
<resources>
    <!-- App identity -->
    <string name="app_name">Mycel</string>
    <string name="app_name_formatted">Mycel</string>
    
    <!-- Notifications -->
    <string name="notification_title">Mycel</string>
    <string name="foreground_service_title">Mycel is running</string>
    
    <!-- About and help -->
    <string name="about_briar">About Mycel</string>
    <string name="about_briar_beta">About Mycel Beta</string>
    
    <!-- URLs and links -->
    <string name="website_url">https://quantumresearch.com.au</string>
    <string name="manual_url">https://quantumresearch.com.au/mycel/manual</string>
    <string name="privacy_policy_url">https://quantumresearch.com.au/mycel/privacy</string>
    
    <!-- Features and descriptions -->
    <string name="briar_headless_gets_started">Mycel headless gets started</string>
    <string name="feature_briar_desktop">Mycel Desktop</string>
    
    <!-- Error messages and system text -->
    <string name="error_briar_core_missing">Mycel core component missing</string>
    <string name="database_key_strengthener_intro">Mycel is strengthening your database key...</string>
    
    <!-- Transport and protocol -->
    <string name="transport_tor">Tor (via Mycel)</string>
    <string name="transport_bluetooth">Bluetooth (via Mycel)</string>
</resources>
```

### 5. Color and Theme Resources

**File**: `briar-android/src/main/res/values/colors.xml`

**Brand Color Updates**:
```xml
<resources>
    <!-- Primary brand colors -->
    <color name="briar_primary">#NEW_PRIMARY_COLOR</color>
    <color name="briar_primary_dark">#NEW_PRIMARY_DARK</color>
    <color name="briar_accent">#NEW_ACCENT_COLOR</color>
    
    <!-- Rename color references if desired -->
    <color name="newbrand_primary">#NEW_PRIMARY_COLOR</color>
    <color name="newbrand_primary_dark">#NEW_PRIMARY_DARK</color>
    <color name="newbrand_accent">#NEW_ACCENT_COLOR</color>
    
    <!-- UI element colors -->
    <color name="briar_button">#NEW_BUTTON_COLOR</color>
    <color name="briar_text_primary">#NEW_TEXT_PRIMARY</color>
    <color name="briar_background">#NEW_BACKGROUND</color>
</resources>
```

**File**: `briar-android/src/main/res/values/styles.xml`

**Theme Updates**:
```xml
<resources>
    <!-- Base application theme -->
    <style name="BriarTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/newbrand_primary</item>
        <item name="colorPrimaryDark">@color/newbrand_primary_dark</item>
        <item name="colorAccent">@color/newbrand_accent</item>
        <!-- Other theme attributes -->
    </style>

    <!-- Rename themes if desired -->
    <style name="NewBrandTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <!-- New theme definition -->
    </style>
</resources>
```

### 6. Network Security Configuration

**File**: `briar-android/src/main/res/xml/network_security_config.xml`

**Potential Updates**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <!-- Update domains if hosting changes -->
        <domain includeSubdomains="true">newbrand.com</domain>
        <domain includeSubdomains="true">api.newbrand.com</domain>
    </domain-config>
    
    <!-- Certificate pinning for new domains -->
    <domain-config>
        <domain includeSubdomains="true">newbrand.com</domain>
        <pin-set>
            <pin digest="SHA-256">NEW_CERTIFICATE_PIN</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

## Medium Priority Configuration Files (🟢 Internal)

### 7. Root Build Configuration

**File**: `build.gradle`

**Updates**:
```gradle
// Top-level build file
allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url "https://jitpack.io" }
    }
    
    afterEvaluate {
        tasks.withType(Test) {
            // Update property names if needed
            outputs.upToDateWhen { System.getenv("OPTIONAL_TESTS") == null }
            systemProperty 'java.security.egd', System.getProperty('java.security.egd')
        }
    }
}

buildscript {
    ext {
        // Version numbers can be updated
        kotlin_version = '1.9.10'
        dagger_version = "2.51.1"
        // ... other versions
    }
    
    dependencies {
        classpath 'com.android.tools.build:gradle:7.2.2'
        // Other build dependencies
    }
}
```

### 8. Gradle Properties

**File**: `gradle.properties`

**Updates**:
```properties
# Build performance
org.gradle.jvmargs=-Xmx2g
android.useAndroidX=true
android.enableJetifier=true

# Feature flags - update property names
newbrand.mailbox_integration_tests=false  # Was briar.mailbox_integration_tests

# Signing properties (new values needed)
NEWBRAND_KEYSTORE_FILE=newbrand-release.keystore
NEWBRAND_KEYSTORE_PASSWORD=new_password
NEWBRAND_KEY_ALIAS=newbrand_key
NEWBRAND_KEY_PASSWORD=new_key_password
```

### 9. ProGuard Configuration

**File**: `briar-android/proguard-rules.txt`

**Package Name Updates**:
```proguard
# Keep classes with main methods
-keepclasseswithmembers class * {
    public static void main(java.lang.String[]);
}

# Keep NewBrand application classes
-keep class com.newbrand.app.** { *; }        # Was org.briarproject.briar
-keep class com.newbrand.infrastructure.** { *; }  # Was org.briarproject.bramble

# Update any specific class references
-keep class com.newbrand.app.android.BrandApplication { *; }

# Tor integration (update package references if tor wrapper changes)
-keep class org.briarproject.onionwrapper.** { *; }  # May stay same

# Database drivers
-keep class org.h2.** { *; }
-keep class org.hsqldb.** { *; }

# Bluetooth libraries
-keep class javax.obex.** { *; }

# Jackson JSON processing
-keep class com.fasterxml.jackson.** { *; }
```

## Low Priority Configuration Files (🔵 Development)

### 10. Local Development Configuration

**File**: `local.properties` (not tracked in git)

**Example Content**:
```properties
# Android SDK location
sdk.dir=/Users/developer/Android/Sdk

# NDK location (if needed)
ndk.dir=/Users/developer/Android/Sdk/ndk/21.4.7075529

# Custom properties for new brand
newbrand.debug.mode=true
newbrand.test.server.url=https://test.newbrand.com
```

### 11. Git Configuration

**File**: `.gitignore`

**Updates**:
```gitignore
# Build artifacts
/build
/*/build/
*.apk
*.ap_

# Local development
local.properties
newbrand.properties  # Add new brand-specific config

# IDE files
.idea/
*.iml

# Signing keys
*.keystore
newbrand-*.keystore  # New signing keys

# Generated files
/caches/
/daemon/
/native/
/wrapper/
```

### 12. Fastlane Configuration

**File**: `briar-android/fastlane/Appfile`

**Updates**:
```ruby
app_identifier("com.newbrand.messenger")  # New app ID
apple_id("developer@newbrand.com")       # New developer account
itc_team_id("NEW_TEAM_ID")              # New App Store Connect team
team_id("NEW_TEAM_ID")                  # New Developer Program team

# For multiple targets if needed
for_platform :android do
  package_name("com.newbrand.messenger")
end
```

**File**: `briar-android/fastlane/Fastfile`

**Updates**:
```ruby
default_platform(:android)

platform :android do
  desc "Build and upload to Play Store"
  lane :release do
    gradle(task: "assembleRelease")
    upload_to_play_store(
      package_name: "com.newbrand.messenger",  # Update
      track: "production"
    )
  end
  
  desc "Take screenshots"
  lane :screenshots do
    gradle(task: "assembleScreenshotDebug")
    screengrab(
      app_package_name: "com.newbrand.messenger.screenshot"  # Update
    )
  end
end
```

## Database and Persistence Configuration

### 13. Database Configuration

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/db/`

**Files with potential configuration**:
- Database schema version constants
- Migration script references
- Default database names and paths

**Example Changes**:
```java
// Database constants that may need updating
public static final String DATABASE_NAME = "newbrand_db";  // Was briar_db
public static final String SETTINGS_NAMESPACE = "newbrand.settings";
public static final int DATABASE_VERSION = 1;  // Reset for new brand

// File paths for database storage
private static final String DB_DIR = "newbrand";  // Was briar
private static final String BACKUP_DIR = "newbrand_backup";
```

### 14. Shared Preferences

**Location**: Throughout Android codebase

**Preference Keys to Update**:
```java
// Shared preference file names
public static final String PREF_FILE_SETTINGS = "newbrand_settings";
public static final String PREF_FILE_TRANSPORT = "newbrand_transport";

// Preference key constants
public static final String PREF_SCREEN_LOCK = "newbrand.screen_lock";
public static final String PREF_NOTIFICATIONS = "newbrand.notifications";
```

## Transport and Network Configuration

### 15. Transport Configuration Constants

**Bluetooth Constants**:
```java
// Bluetooth service UUID (should be unique to new brand)
public static final String BLUETOOTH_UUID = "com.newbrand.BLUETOOTH";

// Service name for discovery
public static final String SERVICE_NAME = "NewBrand_Bluetooth";
```

**Tor Configuration**:
```java
// Tor working directory
public static final String TOR_DIRECTORY = "newbrand_tor";

// Hidden service directory prefix
public static final String HS_DIR_PREFIX = "newbrand_hs_";

// Control port authentication cookie
public static final String COOKIE_FILE = "newbrand_control_auth_cookie";
```

**LAN Discovery**:
```java
// Multicast group for local discovery
public static final String MULTICAST_GROUP = "newbrand.local";

// Default port ranges
public static final int MIN_PORT = 7000;  // May change to avoid conflicts
public static final int MAX_PORT = 7999;
```

## Testing Configuration

### 16. Test Configuration Files

**Android Test Configuration**:
```xml
<!-- briar-android/src/androidTest/res/values/strings.xml -->
<resources>
    <string name="test_app_name">NewBrand Test</string>
    <string name="test_package_name">com.newbrand.messenger.test</string>
</resources>
```

**Test Runner Configuration**:
```java
// Custom test runner class name update
public class BrandTestRunner extends AndroidJUnitRunner {
    // Test runner implementation
}
```

## Documentation Configuration

### 17. README and Documentation

**Files to Update**:
- `README.md` - Main project documentation
- `CONTRIBUTING.md` - Contribution guidelines
- `briar-headless/README.md` - Headless version docs
- `docs/*.md` - All documentation files

**Key Updates**:
- Replace all "Briar" references with new brand name
- Update URLs and links
- Update build instructions with new package names
- Update API documentation

## Configuration Summary by Priority

### 🔴 Critical (Must Change for App to Function)
1. `briar-android/src/main/AndroidManifest.xml`
2. `briar-android/build.gradle` (applicationId)
3. All Java package declarations
4. `briar-android/src/main/res/values/strings.xml` (app_name)

### 🟡 High (User-Visible Changes)
1. All string resource files (30+ languages)
2. Color and theme definitions
3. App store metadata files
4. Visual assets (logos, icons)

### 🟢 Medium (Internal References)
1. Build configuration files
2. ProGuard configuration
3. Network security configuration
4. Transport constants

### 🔵 Low (Development/Optional)
1. Local development files
2. Documentation files
3. Test configuration
4. Git configuration

This comprehensive list ensures no configuration file is overlooked during the rebranding process.