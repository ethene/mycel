# Comprehensive Visual Assets Analysis - Mycel

## 📱 Complete Android Resource Analysis

This document provides the definitive analysis of **ALL visual assets** for the Mycel application, organized by priority and type with clear implementation requirements.

**Total Files Requiring Updates: 241+ files**

---

## 🚨 **CRITICAL PRIORITY - App Identity**

### **1. App Launcher Icons**
**Files requiring complete replacement:**

| File Path | Type | Resolution | Changes Required |
|-----------|------|------------|------------------|
| `drawable/ic_launcher_foreground.xml` | Vector XML | Scalable | **Complete redesign** - Replace Briar geometric logo with Mycel logo |
| `values/ic_launcher_background.xml` | Color | N/A | Update background color from current to Mycel brand color |
| `mipmap-mdpi/ic_launcher.png` | PNG | 48×48 | Replace with Mycel standard icon |
| `mipmap-hdpi/ic_launcher.png` | PNG | 72×72 | Replace with Mycel standard icon |
| `mipmap-xhdpi/ic_launcher.png` | PNG | 96×96 | Replace with Mycel standard icon |
| `mipmap-xxhdpi/ic_launcher.png` | PNG | 144×144 | Replace with Mycel standard icon |
| `mipmap-xxxhdpi/ic_launcher.png` | PNG | 192×192 | Replace with Mycel standard icon |
| `mipmap-mdpi/ic_launcher_round.png` | PNG | 48×48 | Replace with Mycel round icon |
| `mipmap-hdpi/ic_launcher_round.png` | PNG | 72×72 | Replace with Mycel round icon |
| `mipmap-xhdpi/ic_launcher_round.png` | PNG | 96×96 | Replace with Mycel round icon |
| `mipmap-xxhdpi/ic_launcher_round.png` | PNG | 144×144 | Replace with Mycel round icon |
| `mipmap-xxxhdpi/ic_launcher_round.png` | PNG | 192×192 | Replace with Mycel round icon |
| `mipmap-anydpi-v26/ic_launcher.xml` | Adaptive Icon | Scalable | Update references to new Mycel assets |
| `mipmap-anydpi-v26/ic_launcher_round.xml` | Adaptive Icon | Scalable | Update references to new Mycel assets |

**Current Design Analysis:**
- **Vector Foreground**: Abstract geometric design in Briar colors (#87c214, #95d220)
- **Design Style**: Rectangular/square patterns with rounded corners
- **Colors**: Primary green (#87c214) and light green (#95d220)

**Mycel Requirements:**
- Create new logo design concept for app icons
- Define Mycel color palette to replace Briar greens
- Maintain professional, modern appearance suitable for app stores

### **2. Splash Screen (Startup Logo)**
**Files with "BRIAR" text requiring replacement:**

| File Path | Type | Changes Required |
|-----------|------|------------------|
| `drawable/splash_screen.xml` | Vector XML | **Replace "BRIAR" text with "MYCEL"** - Lines 40-70 contain vector paths spelling "BRIAR" |
| `drawable-night/splash_screen.xml` | Vector XML | **Replace "BRIAR" text with "MYCEL"** - Dark theme version |

**Current Design Analysis:**
- **Dimensions**: 235dp × 310dp
- **Logo Colors**: #87c214 (primary), #95d220 (secondary)
- **Text**: Black vector paths spelling "BRIAR" (lines 40-70)
- **Layout**: Logo symbols above, text below

**Mycel Requirements:**
- Create "MYCEL" vector text paths
- Update logo colors to Mycel brand palette
- Maintain same overall layout and dimensions

---

## 🔥 **HIGH PRIORITY - User-Visible Elements**

### **3. Notification Icons (Vector - Modern Android)**
**Files requiring redesign:**

| File Path | Type | Usage | Changes Required |
|-----------|------|-------|------------------|
| `drawable-anydpi-v24/notification_ongoing.xml` | Vector XML | Connection status | **Contains Briar logo pattern - complete redesign** |
| `drawable-anydpi-v24/notification_blog.xml` | Vector XML | Blog posts | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_contact_added.xml` | Vector XML | New contacts | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_forum.xml` | Vector XML | Forum activity | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_hotspot.xml` | Vector XML | WiFi hotspot | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_lock.xml` | Vector XML | Security alerts | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_mailbox.xml` | Vector XML | Mailbox sync | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_private_group.xml` | Vector XML | Private groups | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_private_message.xml` | Vector XML | Private messages | Redesign with Mycel iconography |
| `drawable-anydpi-v24/notification_signout.xml` | Vector XML | Sign out | Redesign with Mycel iconography |

### **4. Notification Icons (PNG - Legacy Android)**
**Files requiring replacement (40 total):**

| Density Folder | Icon Count | Resolution Range | Changes Required |
|----------------|------------|------------------|------------------|
| `drawable-mdpi/` | 10 icons | 24×24px | Replace all notification_*.png with Mycel versions |
| `drawable-hdpi/` | 10 icons | 36×36px | Replace all notification_*.png with Mycel versions |
| `drawable-xhdpi/` | 10 icons | 48×48px | Replace all notification_*.png with Mycel versions |
| `drawable-xxhdpi/` | 10 icons | 72×72px | Replace all notification_*.png with Mycel versions |

**Notification Icon Requirements:**
- **Color**: Must remain pure white (#FFFFFFFF) on transparent background
- **Style**: Simple, recognizable silhouettes
- **Design**: Should reflect Mycel brand style while maintaining clarity at small sizes

### **5. Brand Color Definitions**
**Files requiring complete color palette update:**

| File Path | Type | Changes Required |
|-----------|------|------------------|
| `values/color.xml` | Color definitions | **Update all Briar brand colors** |
| `values-night/color.xml` | Dark theme colors | **Update all Briar dark theme colors** |

**Current Briar Color Palette:**

| Color Name | Hex Value | Usage |
|------------|-----------|-------|
| `briar_lime_500` | #82C91E | Primary brand color |
| `briar_lime_400` | #95d220 | Secondary brand color |
| `briar_blue_400` | #418cd8 | Accent color |
| `briar_primary` | References brand_blue | Main UI primary |
| `briar_accent` | References brand_blue | Main UI accent |

**Additional Briar Colors Requiring Update:**
- All color names with `briar_` prefix (47+ color definitions)
- Material Design theme colors referencing Briar colors
- Illustration colors (`il_lime`, `il_orange`, etc.)
- Button text colors (`mycel_button_text_*`)

**Mycel Color Requirements:**
- Define new primary color (replaces #82C91E)
- Define new accent color (replaces #418cd8) 
- Create complete supporting palette
- Ensure accessibility compliance (contrast ratios)
- Provide dark theme variants

---

## 🎨 **MEDIUM PRIORITY - Feature Illustrations**

### **6. Feature Illustrations with Briar Branding**
**Files requiring update:**

| File Path | Type | Changes Required | Priority |
|-----------|------|------------------|----------|
| `drawable/il_share_app.xml` | Vector XML | **Contains embedded Briar logo** - replace with Mycel logo | **HIGH** |
| `drawable/il_empty_state_blogs.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_empty_state_contact_list.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_empty_state_forum_list.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_empty_state_group_list.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_add_contact_remote.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_add_contact_remote_nickname.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_bluetooth_connect.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_mailbox.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_mailbox_setup.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_qr_code.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_qr_code_error.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_qr_code_nearby.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_transfer_data.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_transfer_data_receive.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |
| `drawable/il_transfer_data_send.xml` | Vector XML | Update colors to Mycel palette | MEDIUM |

### **7. Dark Theme Illustrations**
**Files requiring color updates:**

| File Path | Type | Changes Required |
|-----------|------|------------------|
| `drawable-night/il_*.xml` (12 files) | Vector XML | Update to new Mycel dark theme colors |

**Illustration Update Requirements:**
- Replace hardcoded Briar colors (#87c214, #95d220, #418cd8)
- Update color references to new Mycel palette
- Ensure consistency with overall Mycel brand identity
- Maintain illustration functionality and clarity

---

## 🔧 **MEDIUM PRIORITY - Style & Theme References**

### **8. Style and Theme Definitions**
**Files requiring naming updates:**

| File Path | Type | Changes Required |
|-----------|------|------------------|
| `values/styles.xml` | Style definitions | Update any style names containing "Briar" |
| `values/themes.xml` | Theme definitions | Update any theme names containing "Briar" |
| `values/attrs.xml` | Attribute definitions | Update "BriarRecyclerView" → "MycelRecyclerView" |

### **9. Layout Files with Brand References**
**Files requiring updates (134+ files):**
- Update hardcoded color references to brand colors
- Update references to "briar_*" color names
- Update style references containing "Briar"
- Update any drawable references to rebranded assets

---

## 📊 **Implementation Breakdown by File Type**

### **Vector XML Files (Android Vector Drawables)**
- **Total**: 58+ files
- **Editors**: Android Studio (Vector Asset Studio), Adobe Illustrator, Inkscape
- **Format**: Android VectorDrawable XML with `<path>` elements
- **Changes**: Update `android:pathData` and `android:fillColor` attributes

### **PNG Files (Raster Images)**
- **Total**: 52 files
- **Editors**: Adobe Photoshop, GIMP, Figma
- **Format**: PNG with transparency
- **Densities**: mdpi (1x), hdpi (1.5x), xhdpi (2x), xxhdpi (3x), xxxhdpi (4x)

### **Color XML Files**
- **Total**: 2 files
- **Editors**: Any text editor, Android Studio
- **Format**: Android color resource XML
- **Changes**: Update hex values and color names

### **Configuration XML Files**
- **Total**: 8+ files
- **Editors**: Android Studio, text editor
- **Format**: Various Android resource XML formats
- **Changes**: Update references and names

---

## 🎯 **Mycel Asset Creation Requirements**

Based on this comprehensive analysis, the following new Mycel assets are required:

### **1. Logo & Brand Identity**
- **Primary logo** suitable for app icons (vector format)
- **Text logo** ("MYCEL") for splash screen (vector paths)
- **Icon variations** for different contexts (solid, outline, monochrome)

### **2. Color Palette**
- **Primary color** (replaces #82C91E Briar green)
- **Secondary color** (replaces #95d220 light green)
- **Accent color** (replaces #418cd8 Briar blue)
- **Supporting colors** (neutrals, error states, success states)
- **Dark theme variants** of all colors

### **3. Icon Set (10 Notification Types)**
- blog, contact_added, forum, hotspot, lock, mailbox, ongoing, private_group, private_message, signout
- **Vector XML format** for modern Android (anydpi-v24)
- **PNG format** in 4 densities for legacy Android
- **Pure white (#FFFFFFFF)** on transparent background

### **4. Illustrations**
- Update existing 16+ feature illustrations with new color palette
- Redesign app sharing illustration with new Mycel logo
- Ensure consistency across light and dark themes

---

## ✅ **Quality Assurance Checklist**

Before implementing any Mycel assets:

### **Design Validation**
- [ ] Logo works at all required sizes (48px to 192px)
- [ ] Colors meet WCAG accessibility standards
- [ ] Brand identity is consistent across all assets
- [ ] Dark theme variations are properly designed

### **Technical Validation**
- [ ] Vector XMLs are valid Android format
- [ ] PNG files are correct dimensions and densities
- [ ] Color names follow consistent naming convention
- [ ] All file references are maintained

### **Android Compliance**
- [ ] App icons follow Android Adaptive Icon guidelines
- [ ] Notification icons follow Material Design guidelines
- [ ] Colors work in both light and dark themes
- [ ] Assets display correctly across all screen densities

This comprehensive analysis provides the definitive roadmap for completing the Mycel visual rebranding with professional quality and complete coverage of all user-visible brand elements.