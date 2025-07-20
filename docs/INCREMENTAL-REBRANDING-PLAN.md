# Incremental Mycel Rebranding Plan
## Text/Names First, Assets Last - Test-Driven Approach

Based on the requirement for smaller, testable steps, this plan prioritizes text and naming changes while leaving visual assets (colors/logos/graphics) for the final phase.

---

## 🎯 **OVERALL STRATEGY**

### **Phase Approach:**
1. **Text-Only Changes** (Phases 1-4): Update names, strings, packages
2. **Configuration Updates** (Phase 5): Deep links, build config, metadata  
3. **Asset Replacement** (Phase 6): Visual elements, logos, colors
4. **Testing & Documentation** after each phase

### **Success Criteria:**
- ✅ All tests pass after each phase
- ✅ App builds and runs correctly
- ✅ Documentation updated to reflect changes
- ✅ No functionality breaks

---

## 📋 **PHASE 1: STRING RESOURCES ONLY**
*Duration: 1-2 days*

### **Scope:** App name and user-facing text only
**Goal:** Change "Briar" → "Mycel" in string resources without touching code

#### **1.1 Primary String File**
**File:** `briar-android/src/main/res/values/strings.xml`
```xml
<!-- BEFORE -->
<string name="app_name" translatable="false">Briar</string>
<string name="setup_title">Welcome to Briar</string>

<!-- AFTER -->
<string name="app_name" translatable="false">Mycel</string>
<string name="setup_title">Welcome to Mycel</string>
```

**Changes:**
- [ ] Line 4: `app_name` "Briar" → "Mycel"
- [ ] Line 8: `setup_title` "Welcome to Briar" → "Welcome to Mycel"
- [ ] All other "Briar" references in user-facing strings

#### **1.2 Localized Strings (45+ languages)**
**Automated approach using find/replace:**
```bash
# Find all string files
find briar-android/src/main/res/values-*/strings.xml

# Replace "Briar" with "Mycel" in all files
sed -i 's/Briar/Mycel/g' briar-android/src/main/res/values-*/strings.xml
```

#### **1.3 Testing Phase 1**
```bash
# Clean build
./gradlew clean

# Build and verify no compilation errors
./gradlew :briar-android:assembleDebug

# Run unit tests
./gradlew test

# Install and manually test app launches
adb install briar-android/build/outputs/apk/debug/briar-android-debug.apk
```

#### **1.4 Documentation Update**
- [ ] Update `CLAUDE.md` to reflect Phase 1 completion
- [ ] Update any docs referencing string changes

**✅ Phase 1 Success Criteria:**
- App displays "Mycel" name throughout UI
- All tests pass
- App builds and installs successfully
- No functionality broken

---

## 📋 **PHASE 2: BUILD CONFIGURATION & APP ID**
*Duration: 1 day*

### **Scope:** Android application ID and build configuration
**Goal:** Change package identifier without touching Java package names yet

#### **2.1 Application ID Changes**
**File:** `briar-android/build.gradle`
```gradle
// BEFORE
applicationId "org.briarproject.briar.android"

// AFTER  
applicationId "com.quantumresearch.mycel"
```

**Changes:**
- [ ] Line 31: Update applicationId
- [ ] Line 29: Reset versionCode to 1
- [ ] Line 30: Reset versionName to "1.0.0"

#### **2.2 Debug Package Reference**
**File:** `briar-android/src/debug/res/values/strings.xml`
```xml
<!-- BEFORE -->
<string name="app_package" translatable="false">org.briarproject.briar.android.debug</string>

<!-- AFTER -->
<string name="app_package" translatable="false">com.quantumresearch.mycel.debug</string>
```

#### **2.3 Testing Phase 2**
```bash
# Clean build with new app ID
./gradlew clean
./gradlew :briar-android:assembleDebug

# Verify new package name in APK
aapt dump badging briar-android/build/outputs/apk/debug/*.apk | grep package

# Should show: package: name='com.quantumresearch.mycel'

# Test installation (will be new app, not update)
adb install briar-android/build/outputs/apk/debug/*.apk
```

#### **2.4 Documentation Update**
- [ ] Update build commands in `CLAUDE.md` if needed
- [ ] Document new application ID

**✅ Phase 2 Success Criteria:**
- APK shows new package name: `com.quantumresearch.mycel`
- App installs as separate app (not update to Briar)
- All functionality works identically
- Tests pass

---

## 📋 **PHASE 3: JAVA PACKAGE RENAMING - INFRASTRUCTURE**
*Duration: 2-3 days*

### **Scope:** Bramble layer package renaming only
**Goal:** Rename `org.briarproject.bramble.*` → `com.quantumresearch.mycel.infrastructure.*`

#### **3.1 Bramble Module Renaming**
**Order of operations:**
1. `bramble-api` (foundation)
2. `bramble-core` (depends on api)  
3. `bramble-android` (depends on core)
4. `bramble-java` (depends on core)

#### **3.2 Package Renaming Strategy**
**Use IDE refactoring tools (recommended):**

**Android Studio/IntelliJ:**
1. Right-click on package `org.briarproject.bramble`
2. Refactor → Rename → Select "Rename package"
3. Enter: `com.quantumresearch.mycel.infrastructure`
4. Click "Refactor" and review all changes
5. Confirm and apply

**Manual approach (if needed):**
```bash
# Create new directory structure
mkdir -p bramble-api/src/main/java/com/quantumresearch/mycel/infrastructure
mkdir -p bramble-core/src/main/java/com/quantumresearch/mycel/infrastructure

# Move files and update package declarations
# (This is complex - IDE refactoring strongly recommended)
```

#### **3.3 AndroidManifest Update**
**File:** `bramble-android/src/main/AndroidManifest.xml`
```xml
<!-- BEFORE -->
package="org.briarproject.bramble"

<!-- AFTER -->
package="com.quantumresearch.mycel.infrastructure"
```

#### **3.4 Testing Phase 3**
```bash
# Incremental testing after each module
./gradlew :bramble-api:build
./gradlew :bramble-core:build
./gradlew :bramble-android:build
./gradlew :bramble-java:build

# Full project build
./gradlew build

# Run bramble-specific tests
./gradlew :bramble-core:test
```

**✅ Phase 3 Success Criteria:**
- All bramble modules compile successfully
- Package structure updated: `com.quantumresearch.mycel.infrastructure.*`
- All imports updated automatically
- Tests pass for all bramble modules

---

## 📋 **PHASE 4: JAVA PACKAGE RENAMING - APPLICATION**
*Duration: 2-3 days*

### **Scope:** Briar layer package renaming
**Goal:** Rename `org.briarproject.briar.*` → `com.quantumresearch.mycel.app.*`

#### **4.1 Briar Module Renaming**
**Order of operations:**
1. `briar-api` (foundation)
2. `briar-core` (depends on api + bramble)
3. `briar-android` (depends on core)
4. `briar-headless` (depends on core)

#### **4.2 Package Renaming**
**Same IDE refactoring approach as Phase 3:**
- `org.briarproject.briar.*` → `com.quantumresearch.mycel.app.*`

#### **4.3 AndroidManifest Update**
**File:** `briar-android/src/main/AndroidManifest.xml`
```xml
<!-- BEFORE -->
package="org.briarproject.briar"

<!-- AFTER -->
package="com.quantumresearch.mycel"
```

#### **4.4 Test Runner Update**
**File:** `briar-android/build.gradle`
```gradle
// BEFORE
testInstrumentationRunner 'org.briarproject.briar.android.BriarTestRunner'

// AFTER
testInstrumentationRunner 'com.quantumresearch.mycel.android.MycelTestRunner'
```

#### **4.5 Testing Phase 4**
```bash
# Test each module incrementally
./gradlew :briar-api:build
./gradlew :briar-core:build  
./gradlew :briar-android:build
./gradlew :briar-headless:build

# Full build and test
./gradlew build
./gradlew test

# Android APK test
./gradlew :briar-android:assembleDebug
adb install -r briar-android/build/outputs/apk/debug/*.apk
```

**✅ Phase 4 Success Criteria:**
- All modules compile with new package names
- Full test suite passes
- Android app installs and runs correctly
- All briar.* packages renamed to mycel.app.*

---

## 📋 **PHASE 5: CONFIGURATION & DEEP LINKS**
*Duration: 1-2 days*

### **Scope:** URLs, deep links, and configuration files
**Goal:** Update external references and deep linking

#### **5.1 Deep Link Scheme Update**
**Files containing `briar://` (19 files confirmed):**

**Priority files:**
- `bramble-api/src/main/java/org/briarproject/bramble/api/contact/HandshakeLinkConstants.java`
- `bramble-core/src/main/java/org/briarproject/bramble/contact/PendingContactFactoryImpl.java`
- `briar-android/src/main/java/org/briarproject/briar/android/navdrawer/IntentRouter.java`

**Change:** `briar://` → `mycel://`

#### **5.2 Layout and Theme Names**
**Files:**
- `briar-android/src/main/res/layout/briar_button.xml` → `mycel_button.xml`
- `briar-android/src/main/res/layout/briar_recycler_view.xml` → `mycel_recycler_view.xml`
- Update all references to these layouts in code

**Themes:** `BriarTheme` → `MycelTheme` in:
- `briar-android/src/main/res/values/themes.xml`

#### **5.3 Custom JAR Dependencies**
**Check files in `bramble-java/libs/`:**
- `bluecove-2.1.1-SNAPSHOT-briar.jar`
- `jssc-0.9-briar.jar`

**Action:** Verify if these contain hardcoded package references

#### **5.4 Testing Phase 5**
```bash
# Build and test deep link functionality
./gradlew build
./gradlew :briar-android:assembleDebug

# Test deep links work
adb shell am start -W -a android.intent.action.VIEW -d "mycel://[test-link]" com.quantumresearch.mycel
```

**✅ Phase 5 Success Criteria:**
- Deep links work with `mycel://` scheme  
- All configuration references updated
- Themes and layouts use Mycel naming
- No broken references

---

## 📋 **PHASE 6: VISUAL ASSETS & FINAL POLISH**
*Duration: 3-5 days*

### **Scope:** Logos, icons, colors, and visual branding
**Goal:** Replace all visual Briar assets with Mycel branding

#### **6.1 App Icons** 
**Create and replace:**
- All `mipmap-*/ic_launcher*.png` files (12 files)
- Adaptive icon XML files
- Play Store assets

#### **6.2 Logo Assets**
**Replace in `briar-android/artwork/`:**
- `logo_circle.svg` → `mycel_logo_circle.svg`
- `logo_horizontal_white.svg` → `mycel_logo_horizontal_white.svg`
- All 8+ logo variants

#### **6.3 Color Scheme (Optional)**
**Consider updating brand colors in:**
- `briar-android/src/main/res/values/colors.xml`
- Dark theme variants

#### **6.4 App Store Metadata**
**Update `briar-android/fastlane/metadata/android/`:**
- Update all language directories (25+)
- Replace descriptions, titles, screenshots

#### **6.5 Testing Phase 6**
```bash
# Final comprehensive testing
./gradlew clean build test
./gradlew :briar-android:assembleDebug

# Visual verification
adb install briar-android/build/outputs/apk/debug/*.apk
# Verify icons, splash screen, navigation header
```

**✅ Phase 6 Success Criteria:**
- All visual assets show Mycel branding
- App icon displays correctly in launcher
- No Briar visual references remain
- All functionality preserved

---

## 📋 **PHASE 7: DOCUMENTATION & METADATA**
*Duration: 1 day*

### **Scope:** Final documentation and external references
**Goal:** Complete rebranding documentation

#### **7.1 Root Documentation**
- [ ] `README.md`: Update project description
- [ ] `LICENSE.txt`: Update copyright to Quantum Research Pty Ltd
- [ ] Add attribution to original Briar project

#### **7.2 Build System Documentation**
- [ ] Update `CLAUDE.md` with final Mycel details
- [ ] Update module documentation
- [ ] Verify all build commands work

#### **7.3 External URLs**
- [ ] Update any hardcoded `briarproject.org` URLs
- [ ] Update support/contact information
- [ ] Update repository links

---

## 🔄 **TESTING PROTOCOL (After Each Phase)**

### **Standard Test Sequence:**
```bash
# 1. Clean build
./gradlew clean

# 2. Full build
./gradlew build

# 3. Run all tests  
./gradlew test

# 4. Android specific
./gradlew :briar-android:assembleDebug
./gradlew :briar-android:testDebugUnitTest

# 5. Manual verification
adb install -r [apk-file]
# Test core functionality: account setup, messaging, contacts

# 6. Verify no regressions
# Compare functionality with previous phase
```

### **Rollback Strategy:**
- **Git commits after each phase**
- **Revert capability if tests fail**
- **Document any issues encountered**

---

## 📝 **DOCUMENTATION UPDATES (After Each Phase)**

### **Required Updates:**
1. **CLAUDE.md**: Current status, completed phases
2. **Build instructions**: Verify all commands work
3. **Module documentation**: Update package references
4. **Rebranding progress**: Track completion status

### **Documentation Template:**
```markdown
## Rebranding Status: Phase X Complete

### Completed:
- [x] Phase 1: String resources updated
- [x] Phase 2: Application ID changed
- [ ] Phase 3: Bramble packages (IN PROGRESS)

### Current Package Structure:
- Infrastructure: `com.quantumresearch.mycel.infrastructure.*`
- Application: `com.quantumresearch.mycel.app.*` 
- Android: `com.quantumresearch.mycel.android.*`

### Testing Status:
- ✅ All unit tests pass
- ✅ Android app builds and runs
- ✅ Core functionality verified
```

---

## ⚡ **EXECUTION TIMELINE**

### **Estimated Duration: 10-12 days**
- **Phase 1-2**: 2-3 days (Strings and App ID)
- **Phase 3-4**: 4-6 days (Package renaming) 
- **Phase 5**: 1-2 days (Configuration)
- **Phase 6**: 3-5 days (Visual assets)
- **Phase 7**: 1 day (Documentation)

### **Critical Success Factors:**
1. **Test-driven approach**: No phase proceeds without passing tests
2. **Incremental commits**: Git commit after each successful phase
3. **Documentation maintenance**: Keep docs current with changes
4. **IDE tools**: Use refactoring tools for package renaming
5. **Rollback readiness**: Ability to revert if issues arise

This incremental approach ensures stability and testability throughout the rebranding process while maintaining all functionality.