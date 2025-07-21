# Mycel Typography System

## 📝 **Typography Overview**

Mycel follows a **conservative, accessibility-first** approach to typography, prioritizing readability and Android design standards over distinctive brand typography. The visual identity comes primarily from colors and layout rather than custom fonts.

**Design Philosophy:**
- **Clean & Professional** - System fonts provide familiarity and reliability
- **Material Design 3 Compliant** - Follows Google's typography standards
- **Accessibility-First** - Proper sizing, contrast, and semantic markup
- **Technical Precision** - Monospace for code and network data

---

## 🎯 **Font Sources and Typefaces**

### **Primary Typefaces**
| Typeface | Usage | Source |
|----------|-------|---------|
| **Android System Default** (Roboto) | Main UI text, headings, body content | Android system |
| **Android System Monospace** (Roboto Mono) | Network addresses, codes, technical data | Android system |

### **Custom Font Files**
**None** - Mycel uses exclusively system fonts. No custom .ttf, .otf, or font XML files.

**Rationale:**
- Consistent with Android ecosystem standards
- Automatic localization support
- Reliable cross-device rendering
- Accessibility compliance
- Reduced app size

---

## 📏 **Typography Scale & Hierarchy**

### **Text Size System**
Mycel implements a 5-tier responsive sizing system:

| Size Name | Value | Usage |
|-----------|-------|-------|
| `text_size_tiny` | 12sp | Timestamps, metadata, notification badges |
| `text_size_small` | 14sp | Secondary information, notices |
| `text_size_medium` | 16sp | Main body text, buttons |
| `text_size_large` | 20sp | Section headings |
| `text_size_xlarge` | 34sp | Major titles, hero text |

### **Specialized Sizes**
| Size Name | Value | Usage |
|-----------|-------|-------|
| `avatar_text_size` | 30sp | Text-based user avatars |
| `unread_bubble_text_size` | 12sp | Notification count badges |

---

## 🎨 **Material Design Typography Integration**

### **Material Components Text Appearances**
Mycel leverages Material Design 3 text styles for consistency:

| Style | Usage | Context |
|-------|-------|---------|
| `TextAppearance.MaterialComponents.Headline5` | Major headings | Error screens, completion screens |
| `TextAppearance.MaterialComponents.Headline6` | Section headings | Feature titles |
| `TextAppearance.MaterialComponents.Subtitle1` | Subheadings | Important secondary text |
| `TextAppearance.MaterialComponents.Body1` | Body content | Main content blocks |
| `TextAppearance.AppCompat.Large` | Legacy large text | Setup screens |
| `TextAppearance.AppCompat.Widget.ActionBar.Title.Inverse` | Action bar titles | Toolbar text |

---

## 💬 **Message-Specific Typography**

### **Chat & Communication Styles**

```xml
<!-- Main message text -->
<style name="TextMessage">
    <item name="android:textSize">16sp</item>              <!-- Medium, readable -->
    <item name="android:textIsSelectable">true</item>      <!-- User can select/copy -->
    <item name="android:textColor">?textColorPrimary</item> <!-- Semantic color -->
</style>

<!-- System notices -->
<style name="TextMessage.Notice">
    <item name="android:textSize">14sp</item>              <!-- Smaller, less prominent -->
    <item name="android:textStyle">italic</item>           <!-- Visual distinction -->
    <item name="android:textColor">?textColorSecondary</item>
</style>

<!-- Timestamps -->
<style name="TextMessage.Timestamp">
    <item name="android:textSize">12sp</item>              <!-- Minimal, metadata -->
    <item name="android:textColor">?textColorTertiary</item>
</style>
```

---

## 🔧 **Technical Typography**

### **Monospace Usage**
Applied specifically for technical precision:

| Context | File | Purpose |
|---------|------|---------|
| **Network Setup** | `fragment_hotspot_manual.xml` | Wi-Fi SSID/password display |
| **Error Display** | `fragment_hotspot_error.xml` | Technical error codes |
| **Link Sharing** | `fragment_link_dialog.xml` | Invitation/link codes |

**Monospace Properties:**
```xml
android:typeface="monospace"
```

**Benefits:**
- Fixed-width alignment for technical data
- Easy scanning of codes/addresses
- Clear distinction from prose text

---

## 🎯 **Button Typography**

### **Button Text Hierarchy**

| Button Type | Size | Context |
|-------------|------|---------|
| `MycelButton` | 16sp | Primary actions |
| `MycelButtonFlat` | 16sp | Secondary actions |
| `MycelButtonFlat.Positive.Tiny` | 12sp | Compact positive actions |

### **Button Text Colors**
Integrated with Mycel color system:

```xml
<!-- Positive actions (confirmations, proceed) -->
<item name="android:textColor">@color/mycel_button_text_positive</item>  <!-- Spore Blue -->

<!-- Negative actions (cancel, delete) -->
<item name="android:textColor">@color/mycel_button_text_negative</item>  <!-- Sporeset Coral -->

<!-- Neutral actions (back, skip) -->
<item name="android:textColor">@color/mycel_button_text_neutral</item>   <!-- Muted gray -->
```

---

## 🌙 **Theme Integration**

### **Automatic Dark/Light Mode**
Typography automatically adapts via semantic color references:

| Text Role | Light Mode | Dark Mode |
|-----------|------------|-----------|
| **Primary Text** | `textColorPrimary` | High contrast on dark bg |
| **Secondary Text** | `textColorSecondary` | Medium contrast |
| **Tertiary Text** | `textColorTertiary` | Low contrast, metadata |
| **Link Text** | `briar_text_link` → Mycel blue | Mycel blue (adjusted) |

### **Theme Definition**
```xml
<style name="MycelTheme" parent="Theme.Material3.DayNight">
    <item name="android:textColorLink">@color/briar_text_link</item>  <!-- Mycel blue -->
    <!-- Other theme attributes -->
</style>
```

---

## ♿ **Accessibility & Localization**

### **Accessibility Features**
- **Scalable Units**: All text uses `sp` units to respect user font size preferences
- **Semantic Markup**: Proper content description for screen readers  
- **Contrast Compliance**: Follows WCAG guidelines via Material Design colors
- **Selectable Text**: Message content can be selected and copied

### **Localization Support**
- **System Font Fallback**: Automatic local font support for all languages
- **RTL Support**: Material Design typography handles right-to-left languages
- **Character Set Coverage**: System fonts support all Android-supported languages

### **Visual Hierarchy**
Clear information hierarchy through:
- **Size Differentiation**: 5-tier sizing system
- **Color Semantics**: Primary/secondary/tertiary text colors
- **Weight Variation**: Regular and italic styles
- **Spatial Grouping**: Consistent margins and padding

---

## 🎨 **Integration with Mycel Visual Identity**

### **Typography + Color Harmony**
Typography works with Mycel color system:

- **Text on Spore Blue**: White text for contrast
- **Text on Signal Moss**: Dark text for readability  
- **Text on Sporeset Coral**: White text for alerts
- **Text on Neutrals**: Semantic color system (primary/secondary/tertiary)

### **Visual Consistency**
- **No Typography Branding**: Brand identity expressed through color and layout
- **Professional Restraint**: Familiar, reliable typography builds trust
- **Technical Clarity**: Monospace for precision, standard fonts for communication

---

## 📋 **Typography Guidelines for Implementation**

### **For New Features:**
1. **Use existing text size definitions** (`text_size_medium`, etc.)
2. **Apply Material Design text appearances** when possible
3. **Use monospace only for technical data** (codes, addresses)
4. **Follow semantic color system** (primary/secondary/tertiary)
5. **Enable text selection** for user-beneficial content

### **For Mycel Rebranding:**
1. **Typography requires no changes** - already appropriate
2. **Focus visual branding on colors and graphics**
3. **Maintain accessibility standards**
4. **Test text contrast** with new Mycel colors

---

## 🔍 **Files Containing Typography Definitions**

### **Core Typography Files:**
- `values/styles.xml` - Text styles and button typography
- `values/themes.xml` - Theme integration and color assignments
- `values/dimens.xml` - Text size definitions
- Layout XML files - Material Design text appearances

### **No Font Asset Files:**
- No custom fonts in `/res/font/`
- No .ttf or .otf files
- No font XML descriptors
- Purely system font dependent

This typography system provides a solid, accessible foundation that lets Mycel's color system and visual elements carry the brand identity effectively.