# XML Editing Technical Guide - Mycel Visual Assets

## 🛠️ **Understanding Android Vector XML Files**

### **File Format Explained**

Android Vector Drawable XMLs are **scalable vector graphics** similar to SVG, but optimized for Android. They contain mathematical path descriptions that define shapes and colors.

**Basic Structure:**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="24dp"           <!-- Display width -->
        android:height="24dp"          <!-- Display height -->
        android:viewportWidth="24.0"   <!-- Internal coordinate system width -->
        android:viewportHeight="24.0"> <!-- Internal coordinate system height -->
    <path
        android:fillColor="#FFFFFF"    <!-- Color (hex value) -->
        android:pathData="M12,2L2,7..."/> <!-- Vector path instructions -->
</vector>
```

---

## 🎯 **Critical Files Requiring XML Editing**

### **1. SPLASH SCREEN - Contains "BRIAR" Text**
**File:** `drawable/splash_screen.xml`

**Current Analysis:**
```xml
<!-- Lines 40-70: Vector paths that spell "BRIAR" -->
<path android:fillColor="#000000" 
      android:pathData="M0,253.9 L0,310 L26.2656,310 C38.6498,310,45.1426,303.8,45.1426,294.1..."/>
```

**What needs changing:**
- **Text paths (lines 40-70)**: Mathematical coordinates that draw "BRIAR" letters
- **Logo colors**: `#87c214` (green), `#95d220` (light green) 
- **Overall dimensions**: 235dp × 310dp (keep same)

**Required Mycel Changes:**
1. **Replace text paths** with new "MYCEL" letter paths
2. **Update colors** from Briar green to Mycel brand colors
3. **Maintain layout** (logo above, text below)

### **2. APP ICON - Geometric Logo**
**File:** `drawable/ic_launcher_foreground.xml`

**Current Analysis:**
```xml
<!-- Geometric shapes in Briar colors -->
<path android:pathData="M94,144.5V264c0,9.7..." 
      android:fillColor="#87c214"/>  <!-- Primary green -->
<path android:pathData="M87,190.8H64.5c-9.7,0..." 
      android:fillColor="#95d220"/>  <!-- Light green -->
```

**What needs changing:**
- **All `android:fillColor` values**: Update from Briar greens to Mycel colors
- **Path shapes (optional)**: Can modify geometric design for new brand identity
- **Overall concept**: Currently abstract rectangles/squares - can redesign entirely

### **3. NOTIFICATION ONGOING - Contains Briar Logo**
**File:** `drawable-anydpi-v24/notification_ongoing.xml`

**Current Analysis:**
```xml
<!-- Single path that contains embedded Briar logo pattern -->
<path android:fillColor="#ffffff" 
      android:pathData="M164.7,0C73.7,0 0,73.7 0,164.7c0,91 73.7,164.7 164.7,164.7..."/>
```

**What needs changing:**
- **Embedded logo pattern**: Path data contains Briar geometric logo within circle
- **Must remain white**: `#ffffff` color must stay (Android notification requirement)
- **Complete redesign**: Need new Mycel logo pattern within circular boundary

### **4. APP SHARING ILLUSTRATION - Contains Briar Logo**
**File:** `drawable/il_share_app.xml`

**Current Analysis:**
```xml
<!-- Central logo area (lines 65-88) -->
<path android:fillColor="#65A30D" android:fillType="evenOdd" .../>  <!-- Border -->
<path android:fillColor="#82C91E" android:pathData="M88.93,94.44V106.65..."/> <!-- Logo parts -->
<path android:fillColor="#A3E635" android:pathData="M88.21,99.18H85.91..."/> <!-- Logo parts -->
```

**What needs changing:**
- **Logo section (lines 65-88)**: Contains embedded Briar geometric logo
- **Brand colors**: Multiple Briar greens (`#65A30D`, `#82C91E`, `#A3E635`)
- **Context**: Shows app sharing with recognizable Briar logo in center

---

## 🎨 **Color Definitions Requiring Updates**

### **Primary Color File**
**File:** `values/color.xml`

**Current Briar Brand Colors:**
```xml
<!-- PRIMARY BRAND COLORS -->
<color name="briar_lime_500">#82C91E</color>      <!-- Main brand green -->
<color name="briar_lime_400">#95d220</color>      <!-- Secondary green -->  
<color name="briar_blue_400">#418cd8</color>      <!-- Accent blue -->

<!-- BRAND REFERENCE COLORS -->
<color name="briar_brand_blue">@color/briar_night_700</color>    <!-- #2e3d4f -->
<color name="briar_brand_green">@color/briar_lime_400</color>    <!-- #95d220 -->
<color name="briar_primary">@color/briar_brand_blue</color>
<color name="briar_accent">@color/briar_brand_blue</color>

<!-- SUPPORTING PALETTE -->
<color name="briar_lime_950">#283E0F</color>      <!-- Dark green -->
<color name="briar_lime_600">#67a60f</color>      <!-- Medium green -->
<color name="briar_blue_900">#184080</color>      <!-- Dark blue -->
<color name="briar_blue_300">#8BCAFD</color>      <!-- Light blue -->

<!-- UI ELEMENT COLORS -->
<color name="il_lime">@color/briar_lime_600_new</color>         <!-- Illustration green -->
<color name="mycel_button_text_positive">@color/briar_blue_400</color>  <!-- Button text -->
```

**What needs changing:**
1. **All hex values**: Replace Briar colors with new Mycel palette
2. **Color names**: Update "briar_*" to "mycel_*" (optional but recommended)
3. **Brand references**: Update primary/accent definitions
4. **Dark theme**: Update corresponding `values-night/color.xml`

---

## 🛠️ **Recommended Editing Tools**

### **For Vector XML Editing:**

#### **1. Android Studio (RECOMMENDED)**
**Advantages:**
- **Vector Asset Studio**: Visual editor for Android vectors
- **Real-time preview**: See changes immediately
- **Path validation**: Ensures Android compatibility
- **Color picker**: Easy hex value selection

**How to use:**
1. Open XML file in Android Studio
2. Switch to "Design" tab (if available)
3. Edit path data and colors in "Text" view
4. Preview shows real-time changes

#### **2. Online SVG Editor → Convert**
**Recommended: editor.method.ac**
1. Convert Android XML to SVG format
2. Edit visually in browser
3. Convert back to Android XML

#### **3. Adobe Illustrator (Professional)**
**Workflow:**
1. Import existing paths or create new design
2. Export as Android Vector Drawable
3. Fine-tune XML for perfect Android compatibility

#### **4. Inkscape (Free Alternative)**
**Workflow:**
1. Create/modify vector designs
2. Use SVG to Android XML converter
3. Manual cleanup of path data

### **For Color XML Editing:**
- **Any text editor** (VS Code, Sublime, Notepad++)
- **Android Studio** (syntax highlighting)
- **Color picker tools** for hex value selection

---

## 📐 **Technical Specifications**

### **App Icon Requirements:**
- **Foreground dimensions**: 108dp × 108dp
- **Safe area**: Content within 66dp × 66dp (centered)
- **Colors**: Can be any Mycel brand colors
- **Format**: Vector XML for scalability

### **Splash Screen Requirements:**
- **Dimensions**: 235dp × 310dp (maintain current)
- **Text area**: Lines 40-70 in current file
- **Colors**: Update to Mycel brand palette
- **Layout**: Logo above, text below (maintain)

### **Notification Icon Requirements:**
- **Size**: 24dp × 24dp
- **Color**: Must be pure white (#FFFFFF)
- **Background**: Transparent
- **Style**: Simple, recognizable silhouettes
- **Compatibility**: Support API 24+ (vector) and legacy PNG

### **Color Definition Requirements:**
- **Format**: Hex values (#RRGGBB)
- **Naming**: Consistent convention (e.g., mycel_primary)
- **Accessibility**: Meet WCAG contrast requirements
- **Dark theme**: Provide night mode variants

---

## 🎯 **Step-by-Step XML Editing Process**

### **Step 1: Backup Original Files**
```bash
cp drawable/splash_screen.xml drawable/splash_screen_briar_backup.xml
```

### **Step 2: Edit Vector Paths**
**For splash screen text replacement:**
1. **Identify text section** (lines 40-70 in splash_screen.xml)
2. **Create new "MYCEL" paths** using vector design software
3. **Replace path data** maintaining same structure:
```xml
<!-- Replace this -->
<path android:fillColor="#000000" 
      android:pathData="M0,253.9 L0,310 L26.2656,310 C38.6498,310..."/>

<!-- With new MYCEL paths -->
<path android:fillColor="#000000" 
      android:pathData="[NEW_MYCEL_PATH_DATA]"/>
```

### **Step 3: Update Colors**
```xml
<!-- Replace throughout all files -->
android:fillColor="#87c214"  →  android:fillColor="#[NEW_MYCEL_PRIMARY]"
android:fillColor="#95d220"  →  android:fillColor="#[NEW_MYCEL_SECONDARY]" 
android:fillColor="#418cd8"  →  android:fillColor="#[NEW_MYCEL_ACCENT]"
```

### **Step 4: Test and Validate**
1. **Build project**: `./gradlew build`
2. **Install APK**: Check visual appearance
3. **Test all densities**: Verify on different screen sizes
4. **Dark mode**: Check night theme variants

---

## ⚠️ **Common Pitfalls & Solutions**

### **Path Data Format Issues:**
**Problem**: Invalid path data causes build errors
**Solution**: Use vector editing tools that export valid Android XML

### **Color Reference Errors:**
**Problem**: Referencing non-existent colors
**Solution**: Update all color references when renaming colors

### **Notification Icon Visibility:**
**Problem**: Notification icons not visible in status bar
**Solution**: Ensure pure white (#FFFFFF) on transparent background

### **Density Folder Issues:**
**Problem**: Missing PNG files for different screen densities
**Solution**: Generate all required density versions (mdpi, hdpi, xhdpi, xxhdpi)

---

## ✅ **Validation Checklist**

### **Before Implementation:**
- [ ] New Mycel logo designed and approved
- [ ] Brand color palette finalized
- [ ] All asset files prepared in correct formats

### **During Implementation:**
- [ ] Vector XMLs validate without errors
- [ ] Colors meet accessibility standards
- [ ] All references updated consistently
- [ ] Dark theme variants created

### **After Implementation:**
- [ ] App builds successfully
- [ ] All icons display correctly
- [ ] Splash screen shows MYCEL text
- [ ] Notification icons visible in status bar
- [ ] Brand consistency across all screens

This technical guide provides the complete roadmap for editing Android vector XML files to implement the Mycel visual rebrand with professional quality and technical accuracy.