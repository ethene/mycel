# Deployment Guide and Distribution

## Overview

This document provides comprehensive guidance for deploying and distributing the Briar/Mycel application across Android, desktop, and headless server environments, including build processes, signing, and distribution strategies.

## Android Application Deployment

### Build Configuration

#### Production Build Setup

**File**: `mycel-android/build.gradle`

```gradle
android {
    compileSdk 34
    buildToolsVersion '34.0.0'

    defaultConfig {
        applicationId "com.quantumresearch.mycel"  // Updated for Mycel
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 1      // Reset for Mycel
        versionName "1.0.0" // Reset for Mycel
        
        // Production configuration
        buildConfigField "boolean", "DEBUG_MODE", "false"
        buildConfigField "String", "BUILD_TYPE", "\"release\""
    }

    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 
                         'proguard-rules.txt'
            
            // Security hardening
            debuggable false
            jniDebuggable false
            renderscriptDebuggable false
            zipAlignEnabled true
        }
        
        debug {
            applicationIdSuffix ".debug"
            debuggable true
            minifyEnabled false
            shrinkResources false
        }
    }
}
```

#### Signing Configuration

**Keystore Generation**:
```bash
# Generate release keystore
keytool -genkey -v -keystore mycel-release.keystore \
    -alias mycel-key -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass [STORE_PASSWORD] -keypass [KEY_PASSWORD] \
    -dname "CN=Quantum Research Pty Ltd, OU=Mobile Apps, O=Quantum Research, L=City, S=State, C=AU"

# Verify keystore
keytool -list -v -keystore mycel-release.keystore
```

**Signing Configuration** (`mycel-android/build.gradle`):
```gradle
android {
    signingConfigs {
        release {
            if (project.hasProperty('KEYSTORE_FILE')) {
                storeFile file(KEYSTORE_FILE)
                storePassword KEYSTORE_PASSWORD
                keyAlias KEY_ALIAS
                keyPassword KEY_PASSWORD
            }
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

**Local Properties** (`local.properties`):
```properties
# Keystore configuration (keep secure)
KEYSTORE_FILE=/path/to/mycel-release.keystore
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=mycel-key
KEY_PASSWORD=your_key_password
```

### Build Process

#### Release Build Commands

```bash
# Clean build
./gradlew clean

# Generate release APK
./gradlew :mycel-android:assembleRelease

# Generate AAB (Android App Bundle) for Play Store
./gradlew :mycel-android:bundleRelease

# Verify APK signature
apksigner verify --verbose mycel-release.apk

# Get APK information
aapt dump badging mycel-release.apk
```

#### Build Verification

**APK Analysis**:
```bash
# Check APK size
ls -lh app/build/outputs/apk/release/mycel-release.apk

# Analyze APK contents
unzip -l mycel-release.apk | head -20

# Check for debug symbols (should be minimal in release)
objdump -t mycel-release.apk | grep debug || echo "No debug symbols found"
```

### Distribution Channels

#### 1. Google Play Store

**Preparation**:
- Generate AAB (Android App Bundle) format
- Complete Play Console Developer Agreement
- Prepare store listing materials

**Store Listing Requirements**:
- **App Icon**: 512x512 PNG
- **Feature Graphic**: 1024x500 PNG
- **Screenshots**: Phone (16:9 ratio), Tablet (optional)
- **Privacy Policy URL**: Required for apps handling personal data
- **Content Rating**: PEGI/ESRB questionnaire

**Submission Process**:
```bash
# Generate signed AAB
./gradlew bundleRelease

# Upload via Play Console or use fastlane
bundle exec fastlane supply --aab app/build/outputs/bundle/release/mycel-release.aab
```

#### 2. F-Droid (Open Source Distribution)

**F-Droid Metadata** (`metadata/com.quantumresearch.mycel.yml`):
```yaml
Categories:
  - Internet
  - Security

License: GPL-3.0-or-later
AuthorName: Quantum Research Pty Ltd
AuthorEmail: contact@quantumresearch.com
WebSite: https://mycel.quantumresearch.com
SourceCode: https://github.com/quantumresearch/mycel
IssueTracker: https://github.com/quantumresearch/mycel/issues

AutoName: Mycel
Description: |-
    Mycel is a secure messaging app that protects your privacy through 
    peer-to-peer encrypted communication. No central servers required.

RepoType: git
Repo: https://github.com/quantumresearch/mycel

Builds:
  - versionName: '1.0.0'
    versionCode: 1
    commit: v1.0.0
    subdir: mycel-android
    gradle:
      - official
```

#### 3. Direct APK Distribution

**APK Hosting**:
- Host signed APK on official website
- Provide SHA-256 checksums for verification
- Include installation instructions

**Verification Instructions**:
```bash
# Verify APK signature
apksigner verify mycel-release.apk

# Check SHA-256 checksum
sha256sum mycel-release.apk
# Should match published checksum: [EXPECTED_HASH]
```

### Security Considerations

#### Code Obfuscation

**ProGuard Rules** (`proguard-rules.txt`):
```proguard
# Keep public APIs
-keep public class com.quantumresearch.mycel.** { public *; }

# Aggressive obfuscation
-overloadaggressively
-allowaccessmodification

# Remove debug information
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Protect against reverse engineering
-keepclassmembers class com.quantumresearch.mycel.** {
    !public !protected <fields>;
    !public !protected <methods>;
}
```

#### App Signing Best Practices

1. **Key Security**:
   - Store keystore in secure location
   - Use strong passwords (minimum 12 characters)
   - Backup keystore securely
   - Never commit keystore to version control

2. **Certificate Transparency**:
   - Consider certificate pinning for network security
   - Monitor for certificate misuse

## Desktop Application Deployment

### Java/Desktop Build

#### JAR Generation

**Headless Application** (`mycel-headless/build.gradle`):
```gradle
jar {
    manifest {
        attributes(
            'Main-Class': 'org.briarproject.briar.headless.Main',
            'Implementation-Title': 'Mycel Headless',
            'Implementation-Version': version,
            'Implementation-Vendor': 'Quantum Research Pty Ltd'
        )
    }
    
    // Include all dependencies
    from configurations.runtimeClasspath.collect { 
        it.isDirectory() ? it : zipTree(it) 
    }
    
    // Exclude duplicate files
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // Platform-specific exclusions
    exclude 'META-INF/*.SF'
    exclude 'META-INF/*.DSA'
    exclude 'META-INF/*.RSA'
}
```

**Build Commands**:
```bash
# Generate fat JAR
./gradlew :mycel-headless:jar

# Verify JAR contents
jar tf build/libs/mycel-headless-1.0.0.jar | head -10

# Test JAR execution
java -jar build/libs/mycel-headless-1.0.0.jar --help
```

#### Native Packaging

**jlink (JDK 11+)**:
```bash
# Create custom JRE
jlink --module-path $JAVA_HOME/jmods \
      --add-modules java.base,java.desktop,java.logging,java.net.http \
      --output mycel-jre \
      --compress=2 \
      --no-header-files \
      --no-man-pages

# Package with custom JRE
jpackage --input build/libs \
         --name Mycel \
         --main-jar mycel-headless-1.0.0.jar \
         --runtime-image mycel-jre \
         --type app-image
```

**Platform-Specific Packaging**:

**Linux (AppImage)**:
```bash
# Create AppImage structure
mkdir -p AppDir/usr/bin
mkdir -p AppDir/usr/share/applications
mkdir -p AppDir/usr/share/icons/hicolor/256x256/apps

# Copy JAR and launcher
cp mycel-headless.jar AppDir/usr/bin/
cp mycel.desktop AppDir/usr/share/applications/
cp mycel-icon.png AppDir/usr/share/icons/hicolor/256x256/apps/

# Generate AppImage
appimagetool AppDir/ Mycel-1.0.0-x86_64.AppImage
```

**macOS (DMG)**:
```bash
# Create app bundle
jpackage --input build/libs \
         --name Mycel \
         --main-jar mycel-headless-1.0.0.jar \
         --type dmg \
         --app-version 1.0.0 \
         --vendor "Quantum Research Pty Ltd"
```

**Windows (MSI)**:
```bash
# Create Windows installer
jpackage --input build/libs \
         --name Mycel \
         --main-jar mycel-headless-1.0.0.jar \
         --type msi \
         --app-version 1.0.0 \
         --vendor "Quantum Research Pty Ltd" \
         --win-menu \
         --win-dir-chooser
```

### Distribution Packaging

#### Linux Repositories

**Debian Package** (`debian/control`):
```
Package: mycel
Version: 1.0.0
Section: net
Priority: optional
Architecture: all
Depends: openjdk-17-jre
Maintainer: Quantum Research Pty Ltd <contact@quantumresearch.com>
Description: Secure peer-to-peer messaging application
 Mycel provides secure, private communication without central servers.
 Messages are encrypted end-to-end and routed through multiple transports
 including Tor, Bluetooth, and Wi-Fi Direct.
```

**RPM Spec** (`mycel.spec`):
```spec
Name:           mycel
Version:        1.0.0
Release:        1%{?dist}
Summary:        Secure peer-to-peer messaging application
License:        GPLv3+
URL:            https://mycel.quantumresearch.com
Source0:        mycel-%{version}.tar.gz

BuildRequires:  java-17-openjdk-devel
Requires:       java-17-openjdk-headless

%description
Mycel provides secure, private communication without central servers.

%prep
%setup -q

%build
./gradlew jar

%install
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_datadir}/mycel
install -m 755 scripts/mycel %{buildroot}%{_bindir}/
install -m 644 build/libs/mycel-headless-%{version}.jar %{buildroot}%{_datadir}/mycel/

%files
%{_bindir}/mycel
%{_datadir}/mycel/
%doc README.md
%license LICENSE
```

#### Homebrew Formula

**Formula** (`mycel.rb`):
```ruby
class Mycel < Formula
  desc "Secure peer-to-peer messaging application"
  homepage "https://mycel.quantumresearch.com"
  url "https://github.com/quantumresearch/mycel/archive/v1.0.0.tar.gz"
  sha256 "sha256_hash_here"
  license "GPL-3.0-or-later"

  depends_on "openjdk@17"

  def install
    system "./gradlew", "jar"
    libexec.install "build/libs/mycel-headless-#{version}.jar"
    bin.write_jar_script libexec/"mycel-headless-#{version}.jar", "mycel"
  end

  test do
    assert_match "Mycel #{version}", shell_output("#{bin}/mycel --version")
  end
end
```

## Server/Headless Deployment

### Docker Deployment

#### Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    tor \
    obfs4proxy \
    && rm -rf /var/lib/apt/lists/*

# Create mycel user
RUN useradd -r -s /bin/false mycel

# Copy application
COPY build/libs/mycel-headless-*.jar /opt/mycel/mycel.jar
COPY scripts/docker-entrypoint.sh /opt/mycel/

# Set permissions
RUN chown -R mycel:mycel /opt/mycel \
    && chmod +x /opt/mycel/docker-entrypoint.sh

# Create data directory
RUN mkdir -p /var/lib/mycel \
    && chown mycel:mycel /var/lib/mycel

# Expose ports
EXPOSE 7000 7001

# Switch to mycel user
USER mycel

# Set working directory
WORKDIR /opt/mycel

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:7001/health || exit 1

# Entry point
ENTRYPOINT ["./docker-entrypoint.sh"]
```

#### Docker Compose

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  mycel:
    build: .
    container_name: mycel-server
    restart: unless-stopped
    
    ports:
      - "7000:7000"  # API port
      - "7001:7001"  # Admin port
    
    volumes:
      - mycel-data:/var/lib/mycel
      - ./config:/opt/mycel/config:ro
    
    environment:
      - MYCEL_DATA_DIR=/var/lib/mycel
      - MYCEL_CONFIG_FILE=/opt/mycel/config/mycel.conf
      - JAVA_OPTS=-Xmx1g -XX:+UseG1GC
    
    networks:
      - mycel-network
    
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "5"

volumes:
  mycel-data:
    driver: local

networks:
  mycel-network:
    driver: bridge
```

#### Kubernetes Deployment

**mycel-deployment.yaml**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mycel-server
  labels:
    app: mycel
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mycel
  template:
    metadata:
      labels:
        app: mycel
    spec:
      containers:
      - name: mycel
        image: quantumresearch/mycel:1.0.0
        ports:
        - containerPort: 7000
        - containerPort: 7001
        env:
        - name: MYCEL_DATA_DIR
          value: "/var/lib/mycel"
        - name: JAVA_OPTS
          value: "-Xmx1g -XX:+UseG1GC"
        volumeMounts:
        - name: mycel-data
          mountPath: /var/lib/mycel
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health
            port: 7001
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /ready
            port: 7001
          initialDelaySeconds: 10
          periodSeconds: 5
      volumes:
      - name: mycel-data
        persistentVolumeClaim:
          claimName: mycel-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mycel-service
spec:
  selector:
    app: mycel
  ports:
  - name: api
    port: 7000
    targetPort: 7000
  - name: admin
    port: 7001
    targetPort: 7001
  type: ClusterIP
```

### Systemd Service

**mycel.service**:
```ini
[Unit]
Description=Mycel Secure Messaging Server
After=network.target tor.service
Wants=tor.service

[Service]
Type=simple
User=mycel
Group=mycel
WorkingDirectory=/opt/mycel
ExecStart=/usr/bin/java -jar /opt/mycel/mycel.jar --config /etc/mycel/mycel.conf
Restart=always
RestartSec=10

# Security settings
NoNewPrivileges=yes
PrivateTmp=yes
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=/var/lib/mycel

# Resource limits
LimitNOFILE=65536
MemoryMax=2G

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=mycel

[Install]
WantedBy=multi-user.target
```

**Installation**:
```bash
# Install service file
sudo cp mycel.service /etc/systemd/system/

# Create user and directories
sudo useradd -r -s /bin/false mycel
sudo mkdir -p /opt/mycel /var/lib/mycel /etc/mycel
sudo chown mycel:mycel /var/lib/mycel

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable mycel
sudo systemctl start mycel

# Check status
sudo systemctl status mycel
```

## CI/CD Pipeline

### GitHub Actions

**release.yml**:
```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
    
    - name: Decode keystore
      run: |
        echo ${{ secrets.KEYSTORE_BASE64 }} | base64 -d > keystore.jks
    
    - name: Build release APK
      run: ./gradlew :mycel-android:assembleRelease
      env:
        KEYSTORE_FILE: keystore.jks
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: mycel-android
        path: mycel-android/build/outputs/apk/release/mycel-release.apk

  build-desktop:
    runs-on: ${{ matrix.os }}
    strategy:
      matrix:
        os: [ubuntu-latest, macos-latest, windows-latest]
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build JAR
      run: ./gradlew :mycel-headless:jar
    
    - name: Package for distribution
      run: |
        # Platform-specific packaging commands
        ./scripts/package-${{ runner.os }}.sh
    
    - name: Upload artifacts
      uses: actions/upload-artifact@v3
      with:
        name: mycel-${{ runner.os }}
        path: dist/

  create-release:
    needs: [build-android, build-desktop]
    runs-on: ubuntu-latest
    steps:
    - name: Download artifacts
      uses: actions/download-artifact@v3
    
    - name: Create Release
      uses: softprops/action-gh-release@v1
      with:
        files: |
          mycel-android/mycel-release.apk
          mycel-Linux/mycel-linux.tar.gz
          mycel-macOS/mycel-macos.dmg
          mycel-Windows/mycel-windows.msi
        generate_release_notes: true
        draft: false
        prerelease: false
```

## Security and Compliance

### Code Signing Verification

**Android APK Verification**:
```bash
# Verify APK signature
apksigner verify --verbose mycel-release.apk

# Check certificate details
keytool -printcert -jarfile mycel-release.apk

# Verify with specific certificate
jarsigner -verify -verbose -certs mycel-release.apk
```

**JAR Verification**:
```bash
# Sign JAR
jarsigner -keystore mycel-release.keystore mycel-headless.jar mycel-key

# Verify JAR signature
jarsigner -verify -verbose mycel-headless.jar
```

### Security Scanning

**Dependency Scanning**:
```bash
# OWASP dependency check
./gradlew dependencyCheckAnalyze

# Snyk vulnerability scanning
snyk test

# GitHub Security Advisories
npm audit  # for any npm dependencies
```

### Privacy and Compliance

#### GDPR Compliance Checklist

- [ ] Privacy policy updated for Mycel
- [ ] Data processing documentation
- [ ] User consent mechanisms
- [ ] Data deletion procedures
- [ ] Data portability features
- [ ] Privacy by design implementation

#### App Store Compliance

- [ ] Content rating questionnaire completed
- [ ] In-app purchase policies (if applicable)
- [ ] Accessibility guidelines compliance
- [ ] Platform-specific requirements met

## Monitoring and Analytics

### Application Monitoring

**Health Check Endpoint** (Headless):
```java
@RestController
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", Instant.now());
        status.put("version", BuildConfig.VERSION_NAME);
        
        return ResponseEntity.ok(status);
    }
}
```

### Crash Reporting

**Android Integration**:
```java
// Minimal crash reporting (privacy-focused)
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Log crash locally (no external services)
        CrashReporter.logCrash(throwable, getDeviceInfo());
        
        // Restart application
        restartApplication();
    }
}
```

This deployment guide provides comprehensive coverage for distributing Mycel across all target platforms while maintaining security and privacy standards appropriate for a secure messaging application.