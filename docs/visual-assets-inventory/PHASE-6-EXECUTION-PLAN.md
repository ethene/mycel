# Phase 6 Execution Plan - Mycel Visual Rebranding

## 🎯 **Strategic Execution Order**

This plan follows a **dependencies-first approach** to minimize build errors and visual inconsistencies during implementation.

**Total Estimated Time:** 4-6 hours (depending on asset creation time)

---

## ✅ **STEP 1: Define Mycel Brand Colors** [COMPLETED]
**⏱️ Time: 30 minutes | 🔥 Priority: CRITICAL**

### **1.1 Create Mycel Color Palette** ✅

**Implemented Mycel Color System v1.0:**
- **Primary Color**: Spore Blue `#A3BEEA` (replaced Briar green `#82C91E`)
- **Secondary Color**: Signal Moss `#A8C8B3` (replaced light green `#95d220`) 
- **Accent Color**: Signal Moss `#A8C8B3` (replaced blue `#418cd8`)
- **Error/Warning**: Sporeset Coral `#E8A6A1`
- **Core Neutrals**: Warm off-white `#F5F4F1`, soft black `#1D1D1B`

**Recommended Color Selection Tools:**
- [Material Design Color Tool](https://m3.material.io/theme-builder#/custom)
- [Adobe Color](https://color.adobe.com)
- [Coolors.co](https://coolors.co)

**Example Mycel Color Palette:**
```
Option 1 - Purple/Tech Theme:
- Primary: #7C3AED (purple)
- Secondary: #A855F7 (light purple)  
- Accent: #06B6D4 (cyan)

Option 2 - Blue/Professional Theme:
- Primary: #2563EB (blue)
- Secondary: #3B82F6 (light blue)
- Accent: #10B981 (green)

Option 3 - Keep Green/Update Palette:
- Primary: #16A34A (modern green)
- Secondary: #4ADE80 (light green)
- Accent: #F59E0B (amber)
```

### **1.2 Update Main Color Definitions** ✅
**File:** `briar-android/src/main/res/values/color.xml` [COMPLETED]

**Action:** Replace these key color definitions:
```xml
<!-- ORIGINAL BRIAR COLORS -->
<color name="briar_lime_500">#82C91E</color>      <!-- Replace with Mycel primary -->
<color name="briar_lime_400">#95d220</color>      <!-- Replace with Mycel secondary -->
<color name="briar_blue_400">#418cd8</color>      <!-- Replace with Mycel accent -->

<!-- BRAND REFERENCE UPDATES -->
<color name="briar_primary">@color/briar_brand_blue</color>     <!-- Update reference -->
<color name="briar_accent">@color/briar_brand_blue</color>      <!-- Update reference -->
<color name="md_theme_primary">@color/briar_lime_500</color>    <!-- Material theme -->
```

**Recommended Approach:**
```xml
<!-- ADD NEW MYCEL COLORS -->
<color name="mycel_primary">#[YOUR_PRIMARY_HEX]</color>
<color name="mycel_secondary">#[YOUR_SECONDARY_HEX]</color>
<color name="mycel_accent">#[YOUR_ACCENT_HEX]</color>

<!-- UPDATE EXISTING REFERENCES -->
<color name="briar_lime_500">@color/mycel_primary</color>
<color name="briar_lime_400">@color/mycel_secondary</color>
<color name="briar_blue_400">@color/mycel_accent</color>
<color name="briar_primary">@color/mycel_primary</color>
<color name="briar_accent">@color/mycel_accent</color>
<color name="md_theme_primary">@color/mycel_primary</color>
```

### **1.3 Update Dark Theme Colors** ✅
**File:** `briar-android/src/main/res/values-night/color.xml` [COMPLETED]

**Action:** Update corresponding dark theme variants:
```xml
<!-- Ensure dark theme compatibility -->
<color name="briar_primary">@color/mycel_primary_dark</color>
<color name="briar_accent">@color/mycel_accent_dark</color>
<!-- Add darker variants of your Mycel colors -->
<color name="mycel_primary_dark">#[DARKER_VERSION_OF_PRIMARY]</color>
```

### **1.4 Test Color Changes** ✅
```bash
# Build and test
./gradlew :briar-android:assembleDebug
# Install APK and verify app still functions with new colors
```

**Result:** ✅ Color definitions successfully implemented. App ready to build with new Mycel colors throughout the UI.

---

## ✅ **STEP 2: Update Splash Screen Logo** [COMPLETED]
**⏱️ Time: 45-60 minutes | 🔥 Priority: CRITICAL**

### **2.1 Create "MYCEL" Text Vector**

**Option A - Simple Text Replacement:**
1. Use online text-to-SVG converter (e.g., [SVG Text Generator](https://www.svgtextgenerator.com/))
2. Generate "MYCEL" text in similar font/size as "BRIAR"
3. Convert SVG paths to Android vector format

**Option B - Custom Logo Design:**
1. Design custom "MYCEL" logo in Illustrator/Figma
2. Export as Android Vector Drawable
3. Maintain 235dp × 310dp overall dimensions

### **2.2 Update Splash Screen File**
**File:** `briar-android/src/main/res/drawable/splash_screen.xml`

**Action:** Replace lines 40-70 (BRIAR text paths) with new MYCEL paths:
```xml
<!-- BEFORE (lines 39-70): -->
<path android:fillColor="#000000" 
      android:pathData="M0,253.9 L0,310 L26.2656,310 C38.6498,310,45.1426,303.8..."/>

<!-- AFTER: -->
<path android:fillColor="#000000" 
      android:pathData="[NEW_MYCEL_TEXT_PATHS]"/>
```

**Also update logo colors:**
```xml
<!-- Update these color references -->
android:fillColor="#87c214"  →  android:fillColor="@color/mycel_primary"
android:fillColor="#95d220"  →  android:fillColor="@color/mycel_secondary"
```

### **2.3 Update Night Theme Splash Screen**
**File:** `briar-android/src/main/res/drawable-night/splash_screen.xml`
**Action:** Apply same changes to dark theme version.

### **2.4 Test Splash Screen** ✅
```bash
./gradlew :briar-android:assembleDebug
# Install and verify splash screen shows "MYCEL" text
```

**Result:** ✅ Both light and dark theme splash screens successfully updated with "MYCEL" branding.

---

## 📱 **STEP 3: Update App Launcher Icons**
**⏱️ Time: 60-90 minutes | 🔥 Priority: CRITICAL**

### **3.1 Design New App Icon**

**Option A - Update Existing Design:**
Update geometric shapes in `ic_launcher_foreground.xml` with new colors:
```xml
<!-- Replace all instances -->
android:fillColor="#87c214"  →  android:fillColor="@color/mycel_primary"
android:fillColor="#95d220"  →  android:fillColor="@color/mycel_secondary"
```

**Option B - Complete Redesign:**
1. Create new logo design for app icon (108dp × 108dp)
2. Ensure content fits in 66dp × 66dp safe area
3. Replace entire `ic_launcher_foreground.xml` content

### **3.2 Update Vector Foreground**
**File:** `briar-android/src/main/res/drawable/ic_launcher_foreground.xml`

**Action:** Update colors or replace entire design:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
  <!-- Your new Mycel icon design here -->
  <path android:pathData="[MYCEL_ICON_PATHS]" 
        android:fillColor="@color/mycel_primary"/>
</vector>
```

### **3.3 Generate PNG Icons**

**Using Android Studio:**
1. Right-click `res/` folder → New → Image Asset
2. Icon Type: Launcher Icons (Adaptive and Legacy)  
3. Foreground Layer: Select your updated `ic_launcher_foreground.xml`
4. Background Layer: Choose solid color or keep existing
5. Generate all density versions

**Manual Approach:**
Create PNG versions in all required sizes:
- `mipmap-mdpi/ic_launcher.png` (48×48px)
- `mipmap-hdpi/ic_launcher.png` (72×72px)  
- `mipmap-xhdpi/ic_launcher.png` (96×96px)
- `mipmap-xxhdpi/ic_launcher.png` (144×144px)
- `mipmap-xxxhdpi/ic_launcher.png` (192×192px)
- Same for `ic_launcher_round.png` versions

### **3.4 Test App Icons**
```bash
./gradlew :briar-android:assembleDebug
# Install APK and check launcher icon in app drawer
```

---

## 🔔 **STEP 4: Update Notification Icons**
**⏱️ Time: 90-120 minutes | 🔥 Priority: HIGH**

### **4.1 Identify Critical Notification Icon**
**File:** `briar-android/src/main/res/drawable-anydpi-v24/notification_ongoing.xml`

**This file contains embedded Briar logo and needs complete redesign.**

**Action:** Replace the complex path with simpler Mycel-branded icon:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="24dp"
        android:height="24dp"
        android:viewportWidth="24.0"
        android:viewportHeight="24.0">
    <path android:fillColor="#ffffff"
          android:pathData="[NEW_MYCEL_NOTIFICATION_ICON_PATH]"/>
</vector>
```

### **4.2 Update All Vector Notification Icons**
**Files:** `drawable-anydpi-v24/notification_*.xml` (10 files)

**Action:** For each file, ensure:
- Design reflects Mycel brand style (not Briar)  
- Color remains `#ffffff` (pure white)
- Paths are simple and recognizable at 24dp size

### **4.3 Generate PNG Notification Icons**
**Required:** 40 PNG files (10 types × 4 densities)

**Using design software:**
1. Create 10 different notification icons in white
2. Export in 4 sizes:
   - mdpi: 24×24px
   - hdpi: 36×36px  
   - xhdpi: 48×48px
   - xxhdpi: 72×72px
3. Save to respective `drawable-*dpi/` folders

### **4.4 Test Notifications**
```bash
./gradlew :briar-android:assembleDebug
# Test app and trigger notifications to verify icons appear correctly
```

---

## 🎨 **STEP 5: Update Feature Illustrations**
**⏱️ Time: 60-90 minutes | 🔥 Priority: MEDIUM**

### **5.1 Critical Illustration - App Sharing**
**File:** `briar-android/src/main/res/drawable/il_share_app.xml`

**This contains embedded Briar logo (lines 65-88) that users will see.**

**Action:** Replace logo section with Mycel branding:
```xml
<!-- Lines 65-88: Update the embedded logo colors and possibly design -->
<path android:fillColor="#65A30D"  →  android:fillColor="@color/mycel_primary"
<path android:fillColor="#82C91E"  →  android:fillColor="@color/mycel_primary"  
<path android:fillColor="#A3E635"  →  android:fillColor="@color/mycel_secondary"
```

### **5.2 Update Color References in All Illustrations**
**Files:** `drawable/il_*.xml` (24 files) + `drawable-night/il_*.xml` (12 files)

**Action:** Find and replace color references:
```bash
# Use find/replace in IDE or text editor
Find: #82C91E    Replace: @color/mycel_primary
Find: #95d220    Replace: @color/mycel_secondary  
Find: #418cd8    Replace: @color/mycel_accent
Find: #65A30D    Replace: @color/mycel_primary
Find: #A3E635    Replace: @color/mycel_secondary
```

### **5.3 Test Illustrations**
- Check onboarding screens
- Verify empty state graphics
- Test both light and dark themes

---

## 🔧 **STEP 6: Clean Up References**
**⏱️ Time: 30 minutes | 🔥 Priority: MEDIUM**

### **6.1 Update Style Names (Optional)**
**Files:** `values/styles.xml`, `values/themes.xml`

**Action:** Update any style names containing "Briar":
```xml
<!-- BEFORE -->
<style name="BriarTheme" parent="Theme.Material3">

<!-- AFTER -->  
<style name="MycelTheme" parent="Theme.Material3">
```

### **6.2 Update Attribute Names**
**File:** `values/attrs.xml`
```xml
<!-- BEFORE -->
<declare-styleable name="BriarRecyclerView">

<!-- AFTER -->
<declare-styleable name="MycelRecyclerView">
```

---

## ✅ **STEP 7: Final Testing & Validation**
**⏱️ Time: 30 minutes | 🔥 Priority: CRITICAL**

### **7.1 Complete Build Test**
```bash
./gradlew clean
./gradlew :briar-android:assembleDebug  
./gradlew test  # Run unit tests
```

### **7.2 Visual Verification Checklist**
- [ ] App launcher icon shows Mycel design
- [ ] Splash screen displays "MYCEL" text with new colors
- [ ] App UI uses new Mycel color scheme throughout
- [ ] Notifications show Mycel-branded icons  
- [ ] App sharing screen shows Mycel logo
- [ ] Dark theme works correctly
- [ ] No visual artifacts or broken layouts

### **7.3 Functional Testing**
- [ ] App launches without crashes
- [ ] All major features work (messaging, contacts, settings)
- [ ] Notifications appear and are clickable
- [ ] No build errors or warnings

---

## 💾 **STEP 8: Commit Changes**
**⏱️ Time: 10 minutes**

```bash
# Stage all changes
git add .

# Commit with proper format
git commit -m "[PHASE-6] Replace Briar visual assets with Mycel branding

Complete visual identity transformation:
- Updated brand colors: Mycel primary/secondary/accent palette
- Splash screen: Replaced BRIAR text with MYCEL logo
- App launcher icons: Updated with Mycel design and colors  
- Notification icons: Redesigned 50+ icons with Mycel branding
- Feature illustrations: Updated app sharing and onboarding graphics
- Style references: Updated theme names and attributes

Testing:
- ✅ Build: ./gradlew :briar-android:assembleDebug
- ✅ APK: Installs and displays Mycel branding correctly
- ✅ Icons: All launcher and notification icons working
- ✅ Splash: MYCEL branding displays on startup
- ✅ UI: New color scheme consistent throughout app

Phase Progress: 6/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## 🚀 **Quick Start Checklist**

### **Before You Begin:**
- [x] Choose your Mycel color palette (primary, secondary, accent) ✅
- [ ] Have design tools ready (Android Studio, Illustrator, or online editors)
- [ ] Backup current state: `git checkout -b phase-6-visual-assets`

### **Recommended Execution:**
1. ✅ **Colors first** (Step 1) - Foundation for everything else [COMPLETED]
2. ✅ **Splash screen** (Step 2) - Most visible change [COMPLETED]  
3. **App icons** (Step 3) - Critical for app store presence [NEXT]
4. **Notification icons** (Step 4) - High user visibility
5. **Illustrations** (Step 5) - Polish and consistency
6. **Clean up** (Steps 6-8) - Professional finish

### **Time-Saving Tips:**
- Use Android Studio's Vector Asset Studio for icon generation
- Batch process color replacements with find/replace
- Test after each major step to catch issues early
- Use Material Design color tools for accessibility compliance

**Estimated Total Time: 4-6 hours** (plus any custom design work)

This plan ensures a systematic, low-risk implementation of the complete Mycel visual rebrand.