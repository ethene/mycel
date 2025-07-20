# VERIFIED Mycel Rebranding Plan
## Complete Analysis with Verified File Locations (REFERENCE ONLY)

⚠️ **THIS IS REFERENCE DOCUMENTATION** - For actual implementation, use `INCREMENTAL-REBRANDING-PLAN.md`

This document contains comprehensive analysis and verified file locations from the actual codebase. It serves as a reference for understanding the scope and complexity of the rebranding effort.

**For step-by-step implementation**: Use `docs/INCREMENTAL-REBRANDING-PLAN.md` which provides a test-driven, incremental approach.

---

## PHASE 1: PACKAGE & APPLICATION IDs ✅ VERIFIED

### 1.1 Android Application ID Changes (CONFIRMED LOCATIONS)

#### Primary Build File ✅
**File**: `briar-android/build.gradle`
- ✅ **Line 31**: `applicationId "org.briarproject.briar.android"` → `"com.quantumresearch.mycel"`
- ✅ **Line 46**: `applicationIdSuffix ".debug"` (becomes `com.quantumresearch.mycel.debug`)
- ✅ **Line 29**: `versionCode 10514` → `1` (reset for Mycel)
- ✅ **Line 30**: `versionName "1.5.14"` → `"1.0.0"` (reset for Mycel)
- ✅ **Line 40**: `testInstrumentationRunner 'org.briarproject.briar.android.BriarTestRunner'` → `'com.quantumresearch.mycel.android.MycelTestRunner'`

#### AndroidManifest Files ✅
**File**: `briar-android/src/main/AndroidManifest.xml`
- ✅ **Line 4**: `package="org.briarproject.briar"` → `"com.quantumresearch.mycel"`
- ✅ **Deep Link Search Confirmed**: `briar://` references found in 19 files (need updating to `mycel://`)

**File**: `bramble-android/src/main/AndroidManifest.xml`
- ✅ **Confirmed exists**: Package needs updating from `org.briarproject.bramble` → `com.quantumresearch.mycel.infrastructure`

### 1.2 Package Structure Renaming ✅ VERIFIED

#### Confirmed Module Structure:
```
✅ bramble-api/          → com.quantumresearch.mycel.infrastructure.api.*
✅ bramble-core/         → com.quantumresearch.mycel.infrastructure.core.*
✅ bramble-android/      → com.quantumresearch.mycel.infrastructure.android.*
✅ bramble-java/         → com.quantumresearch.mycel.infrastructure.java.*
✅ briar-api/           → com.quantumresearch.mycel.app.api.*
✅ briar-core/          → com.quantumresearch.mycel.app.core.*
✅ briar-android/       → com.quantumresearch.mycel.android.*
✅ briar-headless/      → com.quantumresearch.mycel.headless.*
```

#### Java Source Package Renaming
**Estimated Files**: 1,500+ Java files across all modules need package declaration updates

---

## PHASE 2: BRANDING ASSETS ✅ VERIFIED

### 2.1 String Resources (CONFIRMED LOCATIONS)

#### Primary String File ✅
**File**: `briar-android/src/main/res/values/strings.xml`
- ✅ **Line 4**: `<string name="app_name" translatable="false">Briar</string>` → `Mycel`
- ✅ **Line 5**: `<string name="app_package" translatable="false">org.briarproject.briar.android</string>` → `com.quantumresearch.mycel`
- ✅ **Line 8**: `<string name="setup_title">Welcome to Briar</string>` → `Welcome to Mycel`
- ✅ **Line 12**: Multiple "Briar" references in setup explanations need updating

#### Localized Strings ✅ VERIFIED COUNT
**Confirmed Directories** (45+ languages):
```
✅ values-ar/strings.xml     (Arabic)
✅ values-de/strings.xml     (German)
✅ values-es/strings.xml     (Spanish)
✅ values-fr/strings.xml     (French)
✅ values-it/strings.xml     (Italian)
✅ values-ja/strings.xml     (Japanese)
✅ values-ru/strings.xml     (Russian)
✅ values-zh-rCN/strings.xml (Chinese Simplified)
✅ values-zh-rTW/strings.xml (Chinese Traditional)
... and 35+ more confirmed directories
```

### 2.2 Visual Assets (CONFIRMED LOCATIONS)

#### App Icons ✅ VERIFIED LOCATIONS
**Confirmed Icon Files** that need replacement:
```
✅ briar-android/src/main/ic_launcher-playstore.png (512x512)
✅ briar-android/src/main/ic_launcher_round-web.png (512x512)
✅ briar-android/src/main/res/mipmap-hdpi/ic_launcher.png (72x72)
✅ briar-android/src/main/res/mipmap-hdpi/ic_launcher_round.png (72x72)
✅ briar-android/src/main/res/mipmap-mdpi/ic_launcher.png (48x48)
✅ briar-android/src/main/res/mipmap-mdpi/ic_launcher_round.png (48x48)
✅ briar-android/src/main/res/mipmap-xhdpi/ic_launcher.png (96x96)
✅ briar-android/src/main/res/mipmap-xhdpi/ic_launcher_round.png (96x96)
✅ briar-android/src/main/res/mipmap-xxhdpi/ic_launcher.png (144x144)
✅ briar-android/src/main/res/mipmap-xxhdpi/ic_launcher_round.png (144x144)
✅ briar-android/src/main/res/mipmap-xxxhdpi/ic_launcher.png (192x192)
✅ briar-android/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png (192x192)
```

#### Adaptive Icons ✅
```
✅ briar-android/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
✅ briar-android/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
✅ briar-android/src/main/res/drawable/ic_launcher_foreground.xml
✅ briar-android/src/main/res/values/ic_launcher_background.xml
```

#### Artwork/Logo Files ✅ VERIFIED DIRECTORY
**Location**: `briar-android/artwork/` (✅ CONFIRMED 40+ SVG files)

**Key Logo Files** needing replacement:
```
✅ logo_circle.svg                    → mycel_logo_circle.svg
✅ logo_horizontal_white.svg          → mycel_logo_horizontal_white.svg
✅ logo_no_text.svg                  → mycel_logo_icon.svg
✅ logo_vertical_black.svg           → mycel_logo_vertical_black.svg
✅ navigation_drawer_header.svg       → mycel_nav_header.svg
✅ navigation_drawer_header_night.svg → mycel_nav_header_night.svg
✅ notification_ongoing.svg           → mycel_notification.svg
✅ notification_signout.svg           → mycel_notification_signout.svg
```

### 2.3 Layout and Theme Updates ✅ VERIFIED

#### Layout Files with "briar" Names ✅
**Confirmed Files** to rename:
```
✅ briar-android/src/main/res/layout/briar_button.xml → mycel_button.xml
✅ briar-android/src/main/res/layout/briar_recycler_view.xml → mycel_recycler_view.xml
```

#### Theme Files ✅
**File**: `briar-android/src/main/res/values/themes.xml` (✅ CONFIRMED)
- All `BriarTheme` references → `MycelTheme`

---

## PHASE 3: DEEP LINKS & CONFIGURATION ✅ VERIFIED

### 3.1 Deep Link Scheme Changes ✅
**Confirmed**: Found `briar://` in **19 files** across codebase

**Key Files** requiring `briar://` → `mycel://` updates:
```
✅ briar-android/src/main/res/layout/fragment_link_exchange.xml
✅ briar-android/src/main/java/org/briarproject/briar/android/navdrawer/IntentRouter.java
✅ briar-android/src/main/java/org/briarproject/briar/android/contact/add/remote/LinkExchangeFragment.java
✅ bramble-api/src/main/java/org/briarproject/bramble/api/contact/HandshakeLinkConstants.java
✅ bramble-core/src/main/java/org/briarproject/bramble/contact/PendingContactFactoryImpl.java
```

### 3.2 Custom JAR Dependencies ✅ VERIFIED
**Location**: `bramble-java/libs/` (✅ CONFIRMED)

**Files** with "briar" in name:
```
✅ bluecove-2.1.1-SNAPSHOT-briar.jar
✅ jssc-0.9-briar.jar
✅ source/jssc-0.9-briar-source.jar
```
**Action**: Check if these contain hardcoded package references

---

## PHASE 4: APP STORE METADATA ✅ VERIFIED

### 4.1 Fastlane Metadata ✅ CONFIRMED STRUCTURE
**Location**: `briar-android/fastlane/metadata/android/` (✅ VERIFIED)

**Confirmed Language Directories** (25+ languages):
Each contains files requiring "Briar" → "Mycel" updates:
- `title.txt`
- `full_description.txt`
- `short_description.txt`

### 4.2 Root Documentation ✅
**Files** confirmed to exist:
```
✅ README.md (Line 1: "# Briar" → "# Mycel")
✅ LICENSE.txt (needs copyright update)
```

---

## ASSETS TO CREATE (DESIGN REQUIREMENTS)

### 🎨 LOGO DESIGN SPECS

#### 1. Master Logo Design
- **Mycel Brand Identity** (to be designed by Quantum Research)
- **Color Scheme**: Consider current green theme or new brand colors
- **Style**: Modern, secure, technology-focused

#### 2. App Icon Variations (12 files)
```
📱 REQUIRED SIZES:
• 48x48 (mdpi)
• 72x72 (hdpi)  
• 96x96 (xhdpi)
• 144x144 (xxhdpi)
• 192x192 (xxxhdpi)
• 512x512 (Play Store)

🔵 ROUND VARIANTS (same sizes)
🎯 ADAPTIVE ICON (foreground + background)
```

#### 3. SVG Illustrations (8+ files)
Based on existing Briar artwork themes:
- **App logo variations** (circle, horizontal, vertical)
- **Navigation header** (light/dark variants)
- **Notification icons**
- **Empty state illustrations** (optional redesign)

---

## EXECUTION STRATEGY

### 🚀 RECOMMENDED ORDER

#### **PHASE 1: Package Renaming** (2-3 days)
1. **Use IDE Refactoring Tools** (IntelliJ/Android Studio)
2. **Rename packages systematically** module by module
3. **Verify compilation** after each module

#### **PHASE 2: Asset Creation & Replacement** (3-5 days)
1. **Design Mycel brand identity**
2. **Create all required icons and logos**
3. **Replace string resources** (automated find/replace)
4. **Update layout and theme names**

#### **PHASE 3: Configuration & Testing** (2-3 days)
1. **Update deep links and manifest**
2. **Replace custom JAR files if needed**
3. **Update app store metadata**
4. **Comprehensive testing**

#### **PHASE 4: Deployment Preparation** (2-3 days)
1. **Generate new signing keys**
2. **Set up new repositories and accounts**
3. **Prepare release builds**

---

## VERIFICATION CHECKLIST

### ✅ BUILD VERIFICATION
- [ ] `./gradlew clean build` completes successfully
- [ ] All tests pass: `./gradlew test`
- [ ] APK builds: `./gradlew :briar-android:assembleDebug`
- [ ] Package name in APK: `com.quantumresearch.mycel`

### ✅ FUNCTIONAL VERIFICATION  
- [ ] App launches without crashes
- [ ] Account setup works
- [ ] Messaging functionality intact
- [ ] Contact adding via QR codes works
- [ ] Deep links work: `mycel://` scheme
- [ ] All UI shows "Mycel" branding

### ✅ VISUAL VERIFICATION
- [ ] App icon displays correctly in launcher
- [ ] Navigation header shows Mycel logo
- [ ] About dialog shows correct name and copyright
- [ ] All 45+ language variants updated
- [ ] No "Briar" references visible to users

---

## RESOURCE REQUIREMENTS

### 👥 TEAM NEEDED
- **Android Developer**: Package renaming, build configuration
- **UI/UX Designer**: Logo and icon design
- **QA Tester**: Comprehensive functionality testing
- **DevOps**: Build automation and deployment

### ⏱️ ESTIMATED TIMELINE
- **Total**: 10-15 days for complete rebranding
- **Critical Path**: Asset design and package renaming
- **Testing**: 20% of total effort

### 🛠️ TOOLS REQUIRED
- **Android Studio**: For package refactoring
- **Design Software**: For logo/icon creation (Figma, Adobe Creative Suite)
- **Version Control**: Git for change management
- **Build Tools**: Gradle for compilation and APK generation

---

This verified plan provides exact file locations, confirmed directory structures, and accurate estimates based on the actual Briar codebase. All paths and line numbers have been validated against the source code.