# Briar → Mycel Color Mapping Reference

## 🎯 **Visual Consistency Mapping Strategy**

This document provides the **exact color mapping** from original Briar colors to new Mycel colors, ensuring visual consistency (blue→blue, gray→gray, etc.) while maintaining the new Mycel brand identity.

---

## 📊 **Current Mycel Color System v1.0**

### **Core Brand Colors:**
- **Primary Blue**: `#A3BEEA` (Spore Blue) - Main interactions, CTAs
- **Secondary Green**: `#A8C8B3` (Signal Moss) - Success, secondary actions  
- **Accent Green**: `#A8C8B3` (Signal Moss) - For contrast/balance
- **Error Coral**: `#E8A6A1` (Sporeset Coral) - Warnings, errors
- **Background Light**: `#F5F4F1` (Warm off-white)
- **Background Dark**: `#1D1D1B` (Soft black)
- **Text Main**: `#000000` (Pure black)
- **Text Invert**: `#FFFFFF` (Pure white)

---

## 🔄 **Color Mapping: Briar → Mycel**

### **🟢 GREEN COLORS (Briar Primary → Mycel Blue/Green)**
```xml
<!-- ORIGINAL BRIAR GREENS -->
briar_lime_950: #283E0F          → mycel_green_dark: #59866F
briar_lime_600: #67a60f          → mycel_green: #A8C8B3  
briar_lime_500: #82C91E          → mycel_primary: #A3BEEA (Blue - primary brand)
briar_lime_400: #95d220          → mycel_secondary: #A8C8B3 (Green - secondary)
```

### **🔵 BLUE COLORS (Briar Secondary → Mycel Blue)**
```xml
<!-- ORIGINAL BRIAR BLUES -->
briar_blue_900: #184080          → mycel_blue_dark: #5D7EB0
briar_blue_800: #134a81          → mycel_blue_dark: #5D7EB0
briar_blue_600: #1b69b6          → mycel_primary: #A3BEEA
briar_blue_400: #418cd8          → mycel_accent: #A8C8B3 (Green for contrast)
briar_blue_300: #8BCAFD          → mycel_blue_soft: #DCE7F7
briar_blue_100: #DDEDFE          → mycel_blue_soft: #DCE7F7
```

### **🔴 RED COLORS (Error/Warning → Mycel Coral)**
```xml
<!-- ORIGINAL BRIAR REDS -->
briar_red_400: #f87171           → mycel_coral: #E8A6A1
briar_red_500: #dc2626           → mycel_error: #E8A6A1
briar_red_600: #dc2626           → mycel_coral_dark: #B36965
```

### **🟠 ORANGE COLORS (Warning → Mycel Coral)**
```xml
<!-- ORIGINAL BRIAR ORANGES -->
briar_orange_200: #fed69f        → mycel_coral_soft: #F8DDDB
briar_orange_400: #FBBF24        → mycel_coral: #E8A6A1
briar_orange_500: #fc9403        → mycel_warning: #E8A6A1
```

### **⚫ DARK COLORS (Night/Dark Theme → Mycel Dark)**
```xml
<!-- ORIGINAL BRIAR NIGHTS -->
briar_night_950: #0e171f         → mycel_bg_dark: #1D1D1B
briar_night_800: #212d3b         → mycel_bg_dark: #1D1D1B
briar_night_700: #2e3d4f         → mycel_ui_muted: #848482
briar_night_600: #475569         → mycel_ui_muted: #848482
briar_night_500: #435b77         → mycel_ui_muted: #848482
briar_night_100: #F1F5F9         → mycel_bg_light: #F5F4F1
briar_night_50: #ebf3fa          → mycel_bg_light: #F5F4F1
```

### **🔘 GRAY COLORS (Neutral → Mycel Neutrals)**
```xml
<!-- ORIGINAL BRIAR GRAYS -->
briar_gray_900: #2e2e2e          → mycel_text_main: #000000
briar_gray_700: #374151          → mycel_ui_muted: #848482
briar_gray_500: #a7a7a7          → mycel_ui_muted: #848482
briar_gray_300: #cccccc          → mycel_ui_border: #D7D6D3
briar_gray_200: #dfdfdf          → mycel_ui_border: #D7D6D3
briar_gray_100: #f2f2f2          → mycel_bg_light: #F5F4F1
```

### **🎨 BRAND COLORS (Direct Mapping)**
```xml
<!-- ORIGINAL BRIAR BRAND -->
briar_brand_blue: #418cd8        → mycel_primary: #A3BEEA
briar_brand_green: #82C91E       → mycel_secondary: #A8C8B3
briar_primary: #82C91E           → mycel_primary: #A3BEEA
briar_primary_dark: #67a60f      → mycel_bg_dark: #1D1D1B
briar_accent: #418cd8            → mycel_accent: #A8C8B3
```

### **📝 TEXT COLORS (Readability Preserved)**
```xml
<!-- ORIGINAL BRIAR TEXT -->
briar_text_link: #418cd8         → mycel_accent: #A8C8B3
briar_text_link_inverse: #FFFFFF → mycel_text_invert: #FFFFFF
briar_text_primary: #df000000    → mycel_text_main: #000000
briar_text_primary_inverse: #FFFFFF → mycel_text_invert: #FFFFFF
briar_text_secondary_inverse: #b4ffffff → #b4ffffff (keep transparency)
briar_text_tertiary_inverse: #80ffffff → #80ffffff (keep transparency)
```

---

## 🔧 **Implementation Plan: Rename All Briar References**

### **Step 1: Update Color Names**
Replace all `briar_*` color names with `mycel_*` equivalents while maintaining the mapped color values.

```xml
<!-- BEFORE -->
<color name="briar_lime_500">#82C91E</color>
<color name="briar_primary">@color/briar_lime_500</color>

<!-- AFTER -->
<color name="mycel_primary_legacy">#82C91E</color> <!-- Keep old value for reference -->
<color name="mycel_primary">@color/mycel_blue</color> <!-- New brand color -->
```

### **Step 2: Update Style References**
Update all style files to use new `mycel_*` color names:

**Files to update:**
- `briar-android/src/main/res/values/styles.xml`
- `briar-android/src/main/res/values/themes.xml`
- `briar-android/src/main/res/values-night/color.xml`

### **Step 3: Update String Names**
Rename briar-specific string keys:

```xml
<!-- BEFORE -->
<string name="briar_version">Mycel version: %s</string>
<string name="briar_website">Website</string>

<!-- AFTER -->
<string name="mycel_version">Mycel version: %s</string>
<string name="mycel_website">Website</string>
```

---

## ✅ **Color Mapping Validation**

### **Visual Consistency Check:**
- ✅ **Green tones** → Mycel Green family (`#A8C8B3`)
- ✅ **Blue tones** → Mycel Blue family (`#A3BEEA`) 
- ✅ **Red/Orange tones** → Mycel Coral family (`#E8A6A1`)
- ✅ **Gray tones** → Mycel Neutral family (`#D7D6D3`, `#848482`)
- ✅ **Dark tones** → Mycel Dark (`#1D1D1B`)
- ✅ **Light tones** → Mycel Light (`#F5F4F1`)

### **Brand Hierarchy:**
- **Primary**: Spore Blue `#A3BEEA` (was Briar Green)
- **Secondary**: Signal Moss `#A8C8B3` (was Briar Blue)  
- **Accent**: Signal Moss `#A8C8B3` (for contrast)
- **Error**: Sporeset Coral `#E8A6A1` (was Briar Red)

---

## 🚀 **Ready for Implementation**

This mapping ensures:
1. **Visual consistency** - Similar colors map to similar Mycel colors
2. **Brand coherence** - All colors reflect Mycel's earth-tech aesthetic
3. **Accessibility** - Contrast ratios maintained for readability
4. **Systematic approach** - Clear 1:1 mapping for bulk replacement

**Next Step:** Execute the color scheme migration using this reference to ensure consistent branding throughout the application.