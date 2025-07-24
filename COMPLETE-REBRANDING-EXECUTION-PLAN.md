# Complete Mycel Rebranding Execution Plan

## Current Status: 30% Complete
**Phases Completed:** 1 ✅ | 2 ✅  
**Current Phase:** Ready for Phase 3 (Infrastructure Package Renaming)

## Phase-by-Phase Execution Plan

### **Phase 3: Infrastructure Package Renaming** 🔄 NEXT
**Goal:** Rename all `org.briarproject.bramble.*` → `com.quantumresearch.mycel.infrastructure.*`

#### **3.1 Java Package Renaming**
```bash
# Create new directory structure
mkdir -p spore-{api,core,android,java}/src/main/java/com/quantumresearch/mycel/infrastructure
mkdir -p spore-{api,core,android,java}/src/test/java/com/quantumresearch/mycel/infrastructure

# Move packages systematically
mv spore-api/src/main/java/org/briarproject/bramble/* \
   spore-api/src/main/java/com/quantumresearch/mycel/infrastructure/

# Update package declarations in ALL moved files
find spore-*/src -name "*.java" -exec sed -i '' \
  's/package org\.briarproject\.bramble/package com.quantumresearch.mycel.infrastructure/g' {} \;

# Update imports across entire codebase
find . -name "*.java" -exec sed -i '' \
  's/import org\.briarproject\.bramble/import com.quantumresearch.mycel.infrastructure/g' {} \;
```

#### **3.2 Class Renaming in Infrastructure**
- `BrambleAndroidModule.java` → `SporeAndroidModule.java`
- `BrambleAndroidEagerSingletons.java` → `SporeAndroidEagerSingletons.java`
- `BrambleCoreModule.java` → `SporeCoreModule.java`
- `BrambleApplication.java` → `SporeApplication.java`

#### **3.3 Build File Updates**
- Update all `build.gradle` files with new package references
- Update Dagger component modules
- Update ProGuard/R8 rules if any

#### **3.4 Test Verification**
```bash
./gradlew clean build test
./gradlew :mycel-android:assembleDebug
```

---

### **Phase 4: Application Package Renaming** 🔄 NEXT
**Goal:** Rename all `org.briarproject.briar.*` → `com.quantumresearch.mycel.app.*`

#### **4.1 Java Package Renaming**
```bash
# Create new directory structure (already partially done)
mkdir -p mycel-{api,core,android,headless}/src/main/java/com/quantumresearch/mycel/app
mkdir -p mycel-{api,core,android,headless}/src/test/java/com/quantumresearch/mycel/app

# Update remaining package declarations
find mycel-*/src -name "*.java" -exec sed -i '' \
  's/package org\.briarproject\.briar/package com.quantumresearch.mycel.app/g' {} \;

# Update imports across entire codebase
find . -name "*.java" -exec sed -i '' \
  's/import org\.briarproject\.briar/import com.quantumresearch.mycel.app/g' {} \;
```

#### **4.2 Critical Class Renaming**
- `BriarService.java` → `MycelService.java`
- `BriarHeadlessApp.kt` → `MycelHeadlessApp.kt`
- Any remaining `Briar*` classes → `Mycel*`

#### **4.3 Android Manifest Updates**
```xml
<!-- Update service references -->
android:name="com.quantumresearch.mycel.app.android.MycelService"
<action android:name="com.quantumresearch.mycel.app.android.MycelService" />
```

#### **4.4 Test Verification**
```bash
./gradlew clean build test
./gradlew :mycel-android:assembleDebug
```

---

### **Phase 5: Configuration Updates** 🔄 MEDIUM PRIORITY
**Goal:** Update build configs, properties, and deep links

#### **5.1 Gradle Properties**
```properties
# gradle.properties
mycel.mailbox_integration_tests=false  # was: briar.mailbox_integration_tests
```

#### **5.2 Deep Link Schemes** (Already updated)
- ✅ `briar://` → `mycel://` (completed in AndroidManifest.xml)

#### **5.3 Build Script Comments**
- Update comments in `build.gradle` files
- Update script documentation

#### **5.4 Verification**
```bash
./gradlew clean build
```

---

### **Phase 6: Resource Renaming** 🔄 MEDIUM PRIORITY
**Goal:** Update colors, styles, layouts, and custom views

#### **6.1 Color Resources** (40+ items)
```xml
<!-- mycel-android/src/main/res/values/colors.xml -->
<color name="mycel_primary">         <!-- was: briar_primary -->
<color name="mycel_accent">          <!-- was: briar_accent -->
<color name="mycel_night_surface">   <!-- was: briar_night_surface -->
<!-- Update 40+ briar_* colors -->
```

#### **6.2 Style Resources**
```xml
<!-- mycel-android/src/main/res/values/styles.xml -->
<style name="MycelToolbar">       <!-- was: BriarToolbar -->
<style name="MycelAvatar">        <!-- was: BriarAvatar -->
<style name="MycelCard">          <!-- was: BriarCard -->
```

#### **6.3 Layout Files** (76+ files)
- Update all `org.briarproject.briar.android.view.*` → `com.quantumresearch.mycel.app.android.view.*`
- Update custom view references
- Update color references: `@color/briar_*` → `@color/mycel_*`

#### **6.4 Custom View Class Names**
- `BriarRecyclerView` → `MycelRecyclerView`
- Update class names and XML references simultaneously

#### **6.5 String Resources** (Remaining)
- Complete any missed localized string updates
- Update string values containing "briar" references

#### **6.6 Verification**
```bash
./gradlew clean build
./gradlew :mycel-android:assembleDebug
# Test app installation and basic functionality
```

---

### **Phase 7: Final Cleanup** 🔄 LOW PRIORITY
**Goal:** Clean up project files, scripts, and test resources

#### **7.1 Project Files**
- `briar.iml` → `mycel.iml` (or regenerate)
- Update `.idea/modules/` configurations
- Clean IDE-specific references

#### **7.2 Script Files** ⚠️ **EVALUATE SAFETY FIRST**
- `convert-bramble-to-spore.sh` - Keep as historical reference
- `convert-briar-to-mycel.sh` - Keep as historical reference  
- `deep-search-briar-bramble.sh` - Update or remove

#### **7.3 Test Resources**
- `briarproject.org_news_index.xml` → Update or use generic test data
- Update test resource references

#### **7.4 Documentation Comments**
- Update code comments referencing Briar/Bramble
- Update README files in subdirectories

---

## **JAR FILE SAFETY ANALYSIS** ⚠️

### **SAFE TO RENAME:**
- ❌ **DO NOT RENAME JAR FILES**
- `/spore-java/libs/bluecove-2.1.1-SNAPSHOT-briar.jar`
- `/spore-java/libs/jssc-0.9-briar.jar`

**Reasons:**
1. **Build Dependencies:** These are compiled libraries with internal metadata
2. **Gradle References:** Build scripts likely reference exact filenames
3. **Version Integrity:** Custom builds of upstream libraries (notice `-briar` suffix)
4. **Checksum Verification:** Witness files verify exact JAR hashes
5. **Source Mapping:** Associated source JARs need filename consistency

### **RECOMMENDED APPROACH:**
1. **Keep JAR filenames unchanged** - They're third-party dependencies
2. **Update only the build script references** if needed
3. **Document the origin** in build comments
4. **Consider rebuilding** only if you have source and build infrastructure

---

## **EXECUTION ORDER & SAFETY**

### **Critical Success Factors:**
1. **Sequential Execution:** Complete phases 3-4 before 5-7
2. **Test After Each Phase:** Build + run tests + basic app functionality
3. **Git Commits:** Commit after each successful phase
4. **Backup Strategy:** Ensure clean git state before starting

### **Risk Mitigation:**
```bash
# Before each phase
git status                    # Ensure clean state
git add . && git commit -m "[PHASE-X] Starting phase X"

# After each phase
./gradlew clean build test    # Verify build
./gradlew :mycel-android:assembleDebug
# Manual verification of app launch

git add . && git commit -m "[PHASE-X] Completed phase X - build verified"
```

### **Dependencies Between Phases:**
- **Phase 3 → Phase 4:** Infrastructure must be renamed before application
- **Phase 4 → Phase 5:** Package names must be correct before config updates
- **Phase 5 → Phase 6:** Build configs must work before resource changes
- **Phase 6 → Phase 7:** App must function before final cleanup

---

## **TESTING STRATEGY**

### **After Each Phase:**
```bash
# Build verification
./gradlew clean build test

# Android build verification  
./gradlew :mycel-android:assembleDebug

# Basic functionality test
adb install -r mycel-android/build/outputs/apk/official/debug/mycel-android-official-debug.apk
# Launch app and verify no crashes
```

### **Integration Testing:**
- Test messaging functionality
- Test contact addition
- Test forum creation
- Test transport switching (WiFi, Bluetooth, Tor)

---

## **CURRENT TODO STATUS**

- ✅ **Phase 1:** String Resources (Completed)
- ✅ **Phase 2:** Application ID (Completed)
- 🔄 **Phase 3:** Infrastructure Package Renaming (Ready to start)
- ⏳ **Phase 4:** Application Package Renaming (Depends on Phase 3)
- ⏳ **Phase 5:** Configuration Updates (Depends on Phase 4)
- ⏳ **Phase 6:** Resource Renaming (Depends on Phase 5)
- ⏳ **Phase 7:** Final Cleanup (Depends on Phase 6)

**Estimated Completion:** ~70% of rebranding work remains
**Critical Path:** Phases 3-4 are blocking for app functionality
**Timeline:** Each phase requires 1-2 hours + testing time

---

## **SUCCESS CRITERIA**

### **Phase 3 Complete When:**
- All `org.briarproject.bramble.*` packages renamed
- All imports updated across codebase
- Infrastructure classes renamed (`Bramble*` → `Spore*`)
- Build succeeds without compilation errors

### **Phase 4 Complete When:**
- All `org.briarproject.briar.*` packages renamed  
- All imports updated across codebase
- Application classes renamed (`Briar*` → `Mycel*`)
- Android Manifest updated
- App launches without crashes

### **Phase 5 Complete When:**
- Gradle properties updated
- Build scripts have correct references
- Deep links work correctly

### **Phase 6 Complete When:**
- All color resources renamed and functional
- All style resources renamed and functional
- All custom views updated in layouts
- App UI renders correctly

### **Phase 7 Complete When:**
- No remaining briar/bramble references (except external deps)
- Clean project structure
- Documentation updated

**Final Success:** Fully functional Mycel app with no Briar/Bramble branding except in external dependencies and git history.