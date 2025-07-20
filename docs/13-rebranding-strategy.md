# Mycel Rebranding Strategy

## Overview

This document provides a comprehensive strategy for rebranding the Briar messaging application to **Mycel** by **Quantum Research Pty Ltd**, including step-by-step instructions, risk assessments, and validation procedures.

## Rebranding Scope

### What We're Changing
- **App Name**: From "Briar" to "Mycel"
- **Developer**: From "The Briar Project" to "Quantum Research Pty Ltd"
- **Package Names**: From `org.briarproject.*` to `com.quantumresearch.mycel.*`
- **Visual Identity**: Logos, icons, colors, themes
- **User-Facing Text**: All strings, descriptions, documentation
- **URLs and Links**: Deep links, website references (Quantum Research domains)
- **Build Configuration**: App IDs, signing, distribution

### What We're Preserving
- **Core Protocol**: Bramble protocol remains unchanged
- **Database Schema**: Existing user data compatibility
- **Security Model**: All cryptographic implementations
- **Feature Set**: All functionality remains identical
- **API Interfaces**: Internal APIs unchanged

## Phase 1: Planning and Preparation

### 1.1 Brand Identity Definition

**Mycel Brand Requirements**:
- [x] Application name: **Mycel**
- [x] Developer: **Quantum Research Pty Ltd**
- [x] Package domain: `com.quantumresearch.mycel.*`
- [ ] Color scheme and visual identity for Mycel
- [ ] Mycel logo designs (multiple formats needed)
- [ ] Marketing copy and descriptions for Mycel

**Deliverables**:
- Mycel brand guidelines document
- Mycel logo assets in SVG format
- Quantum Research color palette definitions
- Typography specifications for Mycel
- Marketing copy for app stores (Quantum Research messaging)

### 1.2 Technical Preparation

**Prerequisites**:
- [ ] Clean development environment
- [ ] Full project backup
- [ ] New Android signing certificate for Quantum Research
- [ ] Quantum Research app store developer accounts
- [ ] Domain registration for Quantum Research/Mycel

**Tools Required**:
- Vector graphics editor (for logo conversion)
- Text replacement tools (for bulk string changes)
- Android Studio for resource management
- Git for version control and branching

### 1.3 Risk Assessment

**High Risk Items**:
- Package name changes (breaks existing installations)
- Database migration (user data loss risk)
- Transport protocol compatibility
- App store approval process

**Mitigation Strategies**:
- Comprehensive testing environment
- User data migration tools
- Gradual rollout strategy
- Rollback procedures

## Phase 2: Core Infrastructure Changes

### 2.1 Package Name Changes

**Priority**: 🔴 CRITICAL

**Files to Change**:
```
📁 All modules/src/main/java/
├── org/briarproject/bramble/ → com/quantumresearch/mycel/infrastructure/
└── org/briarproject/briar/ → com/quantumresearch/mycel/app/
```

**Process**:
1. **Automated Replacement**:
   ```bash
   # Mycel-specific replacement commands
   find . -name "*.java" -exec sed -i 's/org\.briarproject\.bramble/com.quantumresearch.mycel.infrastructure/g' {} \;
   find . -name "*.java" -exec sed -i 's/org\.briarproject\.briar/com.quantumresearch.mycel.app/g' {} \;
   ```

2. **Manual Verification**:
   - Check all package declarations
   - Verify import statements
   - Update AndroidManifest.xml references
   - Update build.gradle applicationId to `com.quantumresearch.mycel`

3. **File Structure Reorganization**:
   ```bash
   # Move Java source files to Mycel package structure
   mkdir -p src/main/java/com/quantumresearch/mycel/
   mv src/main/java/org/briarproject/* src/main/java/com/quantumresearch/mycel/
   ```

### 2.2 Build Configuration Updates

**Files to Update**:

#### Root build.gradle
```gradle
// Update any org.briarproject references in dependencies
// Update version numbers for new brand
```

#### briar-android/build.gradle
```gradle
android {
    defaultConfig {
        applicationId "com.quantumresearch.mycel"  // MYCEL
        versionName "1.0.0"                        // NEW VERSION
        versionCode 1                              // RESET
    }
}
```

#### settings.gradle
```gradle
// Module names may need updating if they reference briar
// Update any project directory references
```

### 2.3 Android Manifest Updates

**File**: `briar-android/src/main/AndroidManifest.xml`

**Changes Required**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.quantumresearch.mycel">  <!-- MYCEL PACKAGE -->

    <application
        android:name="com.quantumresearch.mycel.app.android.MycelApplication"  <!-- MYCEL CLASS -->
        android:label="@string/app_name">  <!-- Points to Mycel app name -->
        
        <!-- Update all activity names -->
        <activity android:name="com.quantumresearch.mycel.app.android.splash.SplashScreenActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Update deep link schemes -->
        <intent-filter>
            <data android:scheme="mycel" />  <!-- MYCEL SCHEME -->
        </intent-filter>
    </application>
</manifest>
```

## Phase 3: Visual Identity Implementation

### 3.1 Logo and Icon Updates

**Location**: `briar-android/artwork/`

**Files to Replace**:
- `logo_circle.svg` ⭐
- `logo_horizontal_white.svg` ⭐
- `logo_no_text.svg` ⭐
- `logo_vertical_black.svg` ⭐
- `navigation_drawer_header.svg`
- `navigation_drawer_header_night.svg`

**Process**:
1. Create new SVG assets matching existing dimensions
2. Maintain same file names for minimal code changes
3. Ensure proper color schemes for light/dark themes
4. Test icon clarity at all Android icon sizes

### 3.2 Android App Icons

**Location**: `briar-android/src/main/res/mipmap-*/`

**Required Icons**:
- `ic_launcher.png` (all density folders)
- `ic_launcher_round.png` (all density folders)
- `ic_launcher_foreground.png` (adaptive icon)
- `ic_launcher_background.png` (adaptive icon)

**Densities Required**:
- mdpi (48x48)
- hdpi (72x72)
- xhdpi (96x96)
- xxhdpi (144x144)
- xxxhdpi (192x192)

**Generation Process**:
1. Use Android Studio Image Asset wizard
2. Import master logo file
3. Generate all density variants
4. Test on various devices and launchers

### 3.3 UI Color Schemes

**Location**: `briar-android/src/main/res/values/colors.xml`

**Key Colors to Update**:
```xml
<resources>
    <!-- Primary brand colors -->
    <color name="briar_primary">#NEW_PRIMARY</color>
    <color name="briar_primary_dark">#NEW_PRIMARY_DARK</color>
    <color name="briar_accent">#NEW_ACCENT</color>
    
    <!-- Update any briar-specific color names -->
    <color name="briar_button">#NEW_BUTTON_COLOR</color>
    <color name="briar_text_primary">#NEW_TEXT_PRIMARY</color>
</resources>
```

## Phase 4: Text and Content Updates

### 4.1 String Resources

**Primary File**: `briar-android/src/main/res/values/strings.xml`

**Critical Strings**:
```xml
<resources>
    <string name="app_name">Mycel</string>  <!-- CRITICAL -->
    <string name="notification_title">Mycel</string>
    
    <!-- Update all briar references -->
    <string name="about_briar">About Mycel</string>
    <string name="briar_headless_gets_started">Mycel headless gets started...</string>
    
    <!-- Deep link schemes -->
    <string name="website_url">https://quantumresearch.com.au</string>
    <string name="manual_url">https://quantumresearch.com.au/mycel/manual</string>
</resources>
```

### 4.2 Multi-Language Updates

**Affected Directories**: `briar-android/src/main/res/values-*/`

**Languages to Update** (30+ languages):
- English (values/)
- Arabic (values-ar/)
- German (values-de/)
- Spanish (values-es/)
- French (values-fr/)
- Italian (values-it/)
- Japanese (values-ja/)
- Russian (values-ru/)
- Chinese Simplified (values-zh-rCN/)
- [27+ additional languages]

**Translation Strategy**:
1. Update English strings first
2. Use professional translation service for primary markets
3. Community translation for additional languages
4. Maintain translation keys for easy updates

### 4.3 App Store Metadata

**Location**: `briar-android/fastlane/metadata/android/`

**Files to Update per Language**:
- `title.txt` - App store title
- `short_description.txt` - Brief description  
- `full_description.txt` - Detailed description
- `changelogs/` - Version change logs

**Example Updates**:
```
title.txt: "Mycel: Secure Messaging"
short_description.txt: "Secure messaging without central servers"
full_description.txt: "Mycel is a messaging app by Quantum Research designed for activists..."
```

## Phase 5: Configuration and Constants

### 5.1 Network Configuration

**Deep Link Schemes**:
```java
// Update throughout codebase
public static final String BRIAR_SCHEME = "newbrand";  // NEW
public static final String CONTACT_SCHEME = "newbrand://contact/";  // NEW
```

**Network Constants**:
```java
// Bluetooth service UUIDs
public static final String BLUETOOTH_UUID = "com.newbrand.BLUETOOTH";  // NEW

// Multicast discovery
public static final String MULTICAST_ADDRESS = "newbrand.local";  // NEW
```

### 5.2 Database Migration

**Considerations**:
- Existing users have data with old package paths
- Database encryption keys tied to package name
- Contact exchange protocols may reference old identifiers

**Migration Strategy**:
1. **Data Export Tool**: Create utility to export/import user data
2. **Compatibility Layer**: Maintain old package recognition during transition
3. **User Communication**: Clear migration instructions for users

### 5.3 Transport Configuration

**Tor Configuration**:
```java
// Hidden service directory names
public static final String TOR_DIRECTORY = "newbrand_tor";  // NEW

// Circuit isolation
public static final String CIRCUIT_PURPOSE = "newbrand_messaging";  // NEW
```

## Phase 6: Testing and Validation

### 6.1 Functional Testing

**Test Categories**:
- [ ] App installation and first launch
- [ ] Account creation and setup
- [ ] Contact addition (QR codes, remote links)
- [ ] Message sending/receiving over all transports
- [ ] File sharing and attachments
- [ ] Forum and blog functionality
- [ ] Settings and preferences
- [ ] App background/foreground transitions

### 6.2 Integration Testing

**Transport Testing**:
- [ ] Tor connectivity and hidden services
- [ ] Bluetooth pairing and messaging
- [ ] LAN discovery and connections
- [ ] Cross-transport failover

**Platform Testing**:
- [ ] Android versions (API 21-34)
- [ ] Different device manufacturers
- [ ] Various screen sizes and densities
- [ ] Headless server functionality

### 6.3 Security Validation

**Cryptographic Testing**:
- [ ] Message encryption/decryption
- [ ] Key exchange protocols
- [ ] Transport security layers
- [ ] Database encryption

**Privacy Testing**:
- [ ] Tor anonymity preservation
- [ ] Metadata protection
- [ ] Contact privacy
- [ ] Transport fingerprinting resistance

## Phase 7: Deployment Strategy

### 7.1 Gradual Rollout

**Stage 1: Internal Testing**
- Development team testing
- Internal QA validation
- Security audit
- Performance benchmarking

**Stage 2: Beta Testing**
- Closed beta with trusted users
- Feature completeness validation
- User experience feedback
- Migration procedure testing

**Stage 3: Limited Release**
- Release to small user subset
- Monitor for critical issues
- Gather usage analytics
- Prepare hotfixes if needed

**Stage 4: General Availability**
- Full public release
- Marketing campaign launch
- User migration support
- Community engagement

### 7.2 User Migration

**Migration Tools**:
- Data export/import utilities
- Contact sharing assistance
- Setup migration guides
- Customer support resources

**Communication Strategy**:
- Pre-announcement to existing users
- Migration timeline communication
- Step-by-step migration guides
- Technical support channels

### 7.3 Legacy Support

**Transition Period**:
- Maintain old app for limited time
- Contact exchange compatibility
- Message format compatibility
- Transport protocol compatibility

**Deprecation Timeline**:
- Month 1-3: Both apps supported
- Month 4-6: Legacy app deprecated warnings
- Month 7+: Legacy app discontinued

## Phase 8: Post-Launch Activities

### 8.1 Monitoring and Analytics

**Key Metrics**:
- App installation and activation rates
- User migration completion rates
- Transport usage patterns
- Crash and error rates
- User feedback and support tickets

**Monitoring Tools**:
- Crash reporting (if privacy-compliant)
- Usage analytics (anonymized)
- Transport performance metrics
- Security incident monitoring

### 8.2 Ongoing Maintenance

**Regular Updates**:
- Security patches and updates
- Transport protocol improvements
- UI/UX enhancements
- New feature development
- Platform compatibility updates

**Community Engagement**:
- Developer documentation updates
- Community contribution guidelines
- Translation management
- User feedback incorporation

## Risk Mitigation

### High-Risk Scenarios

**Package Name Conflicts**:
- Verify new package name availability
- Check for trademark conflicts
- Ensure app store approval

**User Data Loss**:
- Comprehensive backup procedures
- Migration testing on diverse datasets
- Rollback procedures for failed migrations
- User communication about data handling

**Protocol Incompatibility**:
- Maintain backward compatibility
- Gradual protocol version transitions
- Transport-specific migration strategies
- Cross-brand contact exchange

**Security Vulnerabilities**:
- Security audit before release
- Penetration testing
- Code review process
- Vulnerability disclosure procedures

### Rollback Procedures

**Emergency Rollback Triggers**:
- Critical security vulnerabilities
- Widespread user data loss
- Transport protocol failures
- App store policy violations

**Rollback Process**:
1. Immediate app store removal if needed
2. Communication to users about issues
3. Restoration of previous version
4. Data recovery procedures if needed
5. Post-incident analysis and improvements

## Success Criteria

### Technical Success Metrics
- [ ] All tests pass on rebranded version
- [ ] No critical security vulnerabilities
- [ ] Transport protocols function correctly
- [ ] User data migration success rate >95%
- [ ] App store approval in all target markets

### Business Success Metrics
- [ ] User adoption rate for new brand
- [ ] User retention during transition period
- [ ] Community feedback satisfaction
- [ ] Market presence establishment
- [ ] Revenue impact assessment

## Timeline Estimate

**Phase 1-2 (Infrastructure)**: 2-3 weeks
**Phase 3-4 (Visual/Content)**: 1-2 weeks  
**Phase 5-6 (Configuration/Testing)**: 2-3 weeks
**Phase 7 (Deployment)**: 1-2 weeks
**Phase 8 (Post-Launch)**: Ongoing

**Total Estimated Timeline**: 6-10 weeks for complete rebranding

This timeline assumes dedicated development resources and no major architectural changes beyond rebranding.