# Complete Mycel Rebranding Checklist

## Overview
This checklist covers ALL changes needed to rebrand Briar to Mycel by Quantum Research Pty Ltd. **Total Impact: 2,000+ files require modifications.**

---

## PHASE 1: PACKAGE & APPLICATION IDs

### 1.1 Android Application ID Changes
**Priority: HIGH - Required for app distribution**

#### Build Configuration
- [ ] **File**: `briar-android/build.gradle`
  - [ ] Line 31: Change `applicationId "org.briarproject.briar.android"` → `"com.quantumresearch.mycel"`
  - [ ] Line 46: Change `applicationIdSuffix ".debug"` → `".debug"` (becomes `com.quantumresearch.mycel.debug`)
  - [ ] Line 65: Change screenshot suffix accordingly
  - [ ] Line 30: Reset `versionCode` to `1`
  - [ ] Line 31: Reset `versionName` to `"1.0.0"`

#### AndroidManifest.xml Files
- [ ] **File**: `briar-android/src/main/AndroidManifest.xml`
  - [ ] Line 4: Change `package="org.briarproject.briar"` → `"com.quantumresearch.mycel"`
  - [ ] Line 145: Change `<data android:scheme="briar" />` → `<data android:scheme="mycel" />`
- [ ] **File**: `bramble-android/src/main/AndroidManifest.xml`
  - [ ] Change package from `org.briarproject.bramble` → `com.quantumresearch.mycel.infrastructure`

#### Debug Package Override
- [ ] **File**: `briar-android/src/debug/res/values/strings.xml`
  - [ ] Line 4: Change `org.briarproject.briar.android.debug` → `com.quantumresearch.mycel.debug`

### 1.2 Java/Kotlin Package Namespace Changes
**Priority: HIGH - Core functionality dependency**

#### Package Structure Migration
**FROM** → **TO**:
```
org.briarproject.bramble.*     → com.quantumresearch.mycel.infrastructure.*
org.briarproject.briar.*      → com.quantumresearch.mycel.app.*
org.briarproject.briar.android.* → com.quantumresearch.mycel.android.*
```

#### Systematic Package Renaming (1,819 files)
**Method**: Use IDE refactoring tools or systematic find/replace

1. **Phase 1A**: Bramble Layer
   - [ ] Rename all files in `bramble-api/src/main/java/org/briarproject/bramble/`
   - [ ] Rename all files in `bramble-core/src/main/java/org/briarproject/bramble/`
   - [ ] Rename all files in `bramble-android/src/main/java/org/briarproject/bramble/`
   - [ ] Rename all files in `bramble-java/src/main/java/org/briarproject/bramble/`

2. **Phase 1B**: Briar Application Layer
   - [ ] Rename all files in `briar-api/src/main/java/org/briarproject/briar/`
   - [ ] Rename all files in `briar-core/src/main/java/org/briarproject/briar/`

3. **Phase 1C**: Android Application
   - [ ] Rename all files in `briar-android/src/main/java/org/briarproject/briar/`
   - [ ] Update all test files in corresponding `src/test/` directories

4. **Phase 1D**: Headless Application
   - [ ] Rename all files in `briar-headless/src/main/java/org/briarproject/briar/`

#### Import Statement Updates
- [ ] **Automatic**: IDE refactoring will handle most import updates
- [ ] **Manual Check**: Verify no broken imports remain
- [ ] **Test**: Ensure project compiles after package rename

---

## PHASE 2: BRANDING ASSETS

### 2.1 App Name and String Resources
**Priority: HIGH - User-facing changes**

#### Main String Resources
- [ ] **File**: `briar-android/src/main/res/values/strings.xml`
  - [ ] Line 4: `<string name="app_name">Briar</string>` → `<string name="app_name">Mycel</string>`
  - [ ] Line 5: Update package reference to `com.quantumresearch.mycel`
  - [ ] Line 8: `setup_title`: "Welcome to Briar" → "Welcome to Mycel"
  - [ ] Search and replace ALL instances of "Briar" → "Mycel" in this file

#### Localized String Files (167 files)
**Locations**: `briar-android/src/main/res/values-*/strings.xml`

**Languages to update** (45+ locales):
- [ ] values-ar/ (Arabic)
- [ ] values-de/ (German)
- [ ] values-es/ (Spanish)
- [ ] values-fr/ (French)
- [ ] values-it/ (Italian)
- [ ] values-ja/ (Japanese)
- [ ] values-ru/ (Russian)
- [ ] values-zh-rCN/ (Chinese Simplified)
- [ ] values-zh-rTW/ (Chinese Traditional)
- [ ] ...and 35+ more locales

**Action Required**:
- [ ] **Automated**: Use find/replace to change "Briar" → "Mycel" in all files
- [ ] **Manual**: Review translations for context-specific changes
- [ ] **Verification**: Test app in different locales

### 2.2 Visual Assets Creation & Replacement

#### NEW ASSETS TO CREATE

**App Icons** (7 files to create):
- [ ] **Primary Icon**: 512x512 PNG master icon for Mycel
- [ ] `ic_launcher-playstore.png` (512x512)
- [ ] `ic_launcher_round-web.png` (512x512)
- [ ] `mipmap-mdpi/ic_launcher.png` (48x48)
- [ ] `mipmap-hdpi/ic_launcher.png` (72x72)
- [ ] `mipmap-xhdpi/ic_launcher.png` (96x96)
- [ ] `mipmap-xxhdpi/ic_launcher.png` (144x144)
- [ ] `mipmap-xxxhdpi/ic_launcher.png` (192x192)

**Round Icons** (5 files to create):
- [ ] `mipmap-mdpi/ic_launcher_round.png` (48x48)
- [ ] `mipmap-hdpi/ic_launcher_round.png` (72x72)
- [ ] `mipmap-xhdpi/ic_launcher_round.png` (96x96)
- [ ] `mipmap-xxhdpi/ic_launcher_round.png` (144x144)
- [ ] `mipmap-xxxhdpi/ic_launcher_round.png` (192x192)

**Adaptive Icons** (2 XML files to update):
- [ ] `drawable/ic_launcher_foreground.xml` (update vector graphic)
- [ ] Update adaptive icon XML files with new foreground

**Logo Variations** (8 SVG files to recreate):
**Location**: `briar-android/artwork/`
- [ ] `logo_circle.svg` → **mycel_logo_circle.svg**
- [ ] `logo_horizontal_white.svg` → **mycel_logo_horizontal_white.svg**
- [ ] `logo_no_text.svg` → **mycel_logo_icon.svg**
- [ ] `logo_vertical_black.svg` → **mycel_logo_vertical_black.svg**
- [ ] `navigation_drawer_header.svg` → **mycel_nav_header.svg**
- [ ] `navigation_drawer_header_night.svg` → **mycel_nav_header_night.svg**
- [ ] `notification_ongoing.svg` → **mycel_notification.svg**
- [ ] `notification_signout.svg` → **mycel_notification_signout.svg**

**Splash Screen Assets**:
- [ ] Create splash screen logo (vector drawable)
- [ ] Update splash screen background if needed
- [ ] Consider animated splash screen for Android 12+

#### REPLACE EXISTING ASSETS

**Direct File Replacement**:
- [ ] Replace all files in `briar-android/src/main/res/mipmap-*/`
- [ ] Replace all files in `briar-android/src/main/res/drawable/` with "briar" in filename
- [ ] Replace artwork SVG files with Mycel versions

### 2.3 Color Scheme and Themes

#### Theme Name Updates
- [ ] **File**: `briar-android/src/main/res/values/themes.xml`
  - [ ] Line 4: `BriarTheme` → `MycelTheme`
  - [ ] Lines 34, 40, 51, 62, 78, 89: Update all `BriarTheme` variants

#### Consider Color Scheme Updates
**Current Briar Colors** (optional to change):
- Primary: `#2E7D32` (Dark Green)
- Accent: `#4CAF50` (Light Green)

**Quantum Research Brand Colors** (if different):
- [ ] Define new primary color scheme
- [ ] Update `colors.xml` files
- [ ] Update dark theme variants

#### Layout File Updates
- [ ] **File**: `briar-android/src/main/res/layout/briar_button.xml` → rename to `mycel_button.xml`
- [ ] **File**: `briar-android/src/main/res/layout/briar_recycler_view.xml` → rename to `mycel_recycler_view.xml`
- [ ] Update any references to these layouts in code

---

## PHASE 3: CONFIGURATION & DEEP INTEGRATION

### 3.1 URL Schemes and Deep Links
- [ ] **AndroidManifest.xml**: Change `briar://` → `mycel://`
- [ ] **Test**: Verify contact sharing links work with new scheme
- [ ] **Documentation**: Update any user guides mentioning deep links

### 3.2 Notification Channels and Services
- [ ] **Update**: All service class names with "Briar" → "Mycel"
- [ ] **Files affected**: 48+ Java classes including:
  - [ ] `BriarApplication.java` → `MycelApplication.java`
  - [ ] `BriarApplicationImpl.java` → `MycelApplicationImpl.java`
  - [ ] `BriarService.java` → `MycelService.java`
  - [ ] `BriarActivity.java` → `MycelActivity.java`
  - [ ] `BriarController.java` → `MycelController.java`

### 3.3 External Dependencies
- [ ] **Custom JAR files** (may need rebuilding):
  - [ ] `bramble-java/libs/bluecove-2.1.1-SNAPSHOT-briar.jar`
  - [ ] `bramble-java/libs/jssc-0.9-briar.jar`
  - [ ] Check if these contain hardcoded package references

### 3.4 ProGuard Rules
- [ ] **File**: `briar-android/proguard-rules.txt`
  - [ ] Update package keep rules: `org.briarproject.*` → `com.quantumresearch.mycel.*`
- [ ] **File**: `briar-android/proguard-test.txt`
  - [ ] Update test-specific rules

---

## PHASE 4: DOCUMENTATION & METADATA

### 4.1 App Store Metadata (56+ files)
**Location**: `briar-android/fastlane/metadata/android/`

**For each language directory**:
- [ ] `*/title.txt`: "Briar" → "Mycel"
- [ ] `*/full_description.txt`: Replace Briar descriptions with Mycel
- [ ] `*/short_description.txt`: Update short descriptions

**Key directories**:
- [ ] `en-US/` (English - primary)
- [ ] `de-DE/` (German)
- [ ] `es-ES/` (Spanish)
- [ ] `fr-FR/` (French)
- [ ] `it-IT/` (Italian)
- [ ] `ja-JP/` (Japanese)
- [ ] `ru-RU/` (Russian)
- [ ] `zh-CN/` (Chinese Simplified)
- [ ] ...and 25+ more languages

### 4.2 Root Documentation
- [ ] **File**: `README.md`
  - [ ] Line 1: `# Briar` → `# Mycel`
  - [ ] Lines 2-20: Update project description and links
  - [ ] Replace all `briarproject.org` URLs with new domain
  - [ ] Update download links and installation instructions

### 4.3 License and Copyright
- [ ] **File**: `LICENSE.txt`
  - [ ] Update copyright to: `Copyright (C) 2024 Quantum Research Pty Ltd`
  - [ ] Add attribution: `Based on Briar, Copyright (C) Briar contributors`
  - [ ] Keep GPL-3.0 license terms

### 4.4 External URL References (32+ files)
**URLs to update** from `briarproject.org` to new domain:
- [ ] Privacy policy links
- [ ] User manual links
- [ ] Support/contact links
- [ ] Source code repository links
- [ ] Bug reporting links

---

## PHASE 5: VERIFICATION & TESTING

### 5.1 Build Verification
- [ ] **Clean build**: `./gradlew clean build`
- [ ] **Android APK**: `./gradlew :briar-android:assembleDebug`
- [ ] **Headless JAR**: `./gradlew :briar-headless:jar`
- [ ] **All tests pass**: `./gradlew test`

### 5.2 Functional Testing
- [ ] **App launches**: No crashes on startup
- [ ] **Account creation**: Setup flow works
- [ ] **Messaging**: Send/receive messages
- [ ] **Contacts**: Add contacts via QR code
- [ ] **Forums**: Create and post to forums
- [ ] **Transport**: Tor/Bluetooth/LAN connectivity
- [ ] **Deep links**: `mycel://` links work
- [ ] **Notifications**: Push notifications display correctly

### 5.3 Visual Verification
- [ ] **App icon**: Displays correctly in launcher
- [ ] **Splash screen**: Shows Mycel branding
- [ ] **Navigation drawer**: Mycel logo appears
- [ ] **About dialog**: Shows Mycel name and copyright
- [ ] **Settings**: All text shows "Mycel"
- [ ] **All locales**: Test app in different languages

### 5.4 Package Verification
- [ ] **APK analysis**: `aapt dump badging mycel-debug.apk`
- [ ] **Package name**: Shows `com.quantumresearch.mycel`
- [ ] **No briar references**: Search APK contents for "briar"
- [ ] **Signature**: APK signs correctly

---

## PHASE 6: DEPLOYMENT PREPARATION

### 6.1 New Infrastructure Setup
- [ ] **Domain registration**: Register domain for Mycel
- [ ] **Website**: Create mycel.quantumresearch.com
- [ ] **Source repository**: Create GitHub repo for Mycel
- [ ] **App store accounts**: Set up Google Play/F-Droid accounts

### 6.2 Release Preparation
- [ ] **Signing key**: Generate new release keystore for Mycel
- [ ] **Version numbering**: Start from v1.0.0
- [ ] **Release notes**: Prepare initial release notes
- [ ] **Privacy policy**: Create Mycel privacy policy
- [ ] **Support documentation**: Create user guides

### 6.3 Marketing Assets
- [ ] **Screenshots**: Generate app store screenshots
- [ ] **Feature graphics**: Create Play Store graphics
- [ ] **Promotional images**: Create marketing materials
- [ ] **Website content**: Prepare website copy

---

## ESTIMATED EFFORT

### Development Time
- **Package renaming**: 2-3 days (automated + verification)
- **Asset creation**: 3-5 days (design + implementation)
- **Testing & verification**: 2-3 days
- **Documentation**: 1-2 days
- **Deployment setup**: 2-3 days

**Total**: ~10-15 days for complete rebranding

### Resources Needed
- **Developer**: Java/Android development skills
- **Designer**: Logo, icons, and visual asset creation
- **QA Tester**: Comprehensive functionality testing
- **DevOps**: Build system and deployment setup

---

## CRITICAL SUCCESS FACTORS

1. **Systematic Approach**: Use IDE refactoring tools for package renaming
2. **Thorough Testing**: Test all functionality after each phase
3. **Asset Quality**: Ensure all logos/icons are professionally designed
4. **Legal Compliance**: Maintain GPL-3.0 license and attribution
5. **Documentation**: Keep detailed records of all changes made

This checklist ensures no aspect of the rebranding is overlooked and provides a clear path from Briar to Mycel.