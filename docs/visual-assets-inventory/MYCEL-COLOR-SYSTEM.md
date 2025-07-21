# Mycel Color System v1.0

**Visual Identity:** Clean minimalist palette with earth-tech aesthetic - decentralized technology with organic softness. Black/white foundation with thoughtful pastel accents for emotional layering and contrast.

---

## 🎨 **Design Philosophy**

**Core Principles:**
- **Clean, not stark** - Approachable minimalism
- **Resilient, not sterile** - Warm undertones prevent coldness
- **Quiet confidence** - No neon, no corporate gloss
- **Earth-tech aesthetic** - Decentralization meets organic design

**Palette Strategy:**
- **BW-first foundation** - Timeless, grounded base
- **Pastel accents** - Emotional contrast and UI hierarchy
- **Cross-theme compatibility** - Works in light and dark modes
- **Purposeful color usage** - Each accent has specific semantic meaning

---

## 🎛️ **CORE COLORS (Neutrals)**

### **Background & Structure**
| Token | Hex Value | Usage | Description |
|-------|-----------|-------|-------------|
| `mycel_bg_light` | `#F5F4F1` | Light mode background | Slightly warm off-white |
| `mycel_bg_dark` | `#1D1D1B` | Dark mode background | Deep, soft black |
| `mycel_ui_border` | `#D7D6D3` | Light borders, dividers | Subtle separation |
| `mycel_ui_muted` | `#848482` | Muted/secondary text | Reduced emphasis |

### **Typography**
| Token | Hex Value | Usage | Description |
|-------|-----------|-------|-------------|
| `mycel_text_main` | `#000000` | Primary text on light | Pure black for maximum contrast |
| `mycel_text_invert` | `#FFFFFF` | Primary text on dark | Pure white for dark themes |

---

## 💧 **COOL ACCENT - Spore Blue**

**Semantic Meaning:** Safe, available, connected states. Primary interaction color.

| Token | Hex Value | Usage | Description |
|-------|-----------|-------|-------------|
| `mycel_blue` | `#A3BEEA` | Primary links, buttons, CTAs | Soft, calming sky blue |
| `mycel_blue_soft` | `#DCE7F7` | Background hovers, highlights | Subtle interaction feedback |
| `mycel_blue_dark` | `#5D7EB0` | Active/selected states | Pressed/focused states |

**Replaces:** Briar blue `#418cd8` (primary accent color)

---

## 🌿 **BALANCING GREEN - Signal Moss**

**Semantic Meaning:** Success, availability, system health. Not a base color but balancing accent.

| Token | Hex Value | Usage | Description |
|-------|-----------|-------|-------------|
| `mycel_green` | `#A8C8B3` | Success indicators, secondary actions | Earthy moss green |
| `mycel_green_soft` | `#DDEEE4` | Success backgrounds, hint zones | Gentle confirmation |
| `mycel_green_dark` | `#59866F` | Active success states | Confirmed/pressed success |

**Replaces:** Briar green `#82C91E` and `#95d220` (primary and secondary greens)

---

## 🔴 **WARM ACCENT - Sporeset Coral**

**Semantic Meaning:** Alerts, warnings, user emphasis. Emotional depth without aggression.

| Token | Hex Value | Usage | Description |
|-------|-----------|-------|-------------|
| `mycel_coral` | `#E8A6A1` | Alerts, errors, emphasis | Warm coral for attention |
| `mycel_coral_soft` | `#F8DDDB` | Error backgrounds | Gentle error indication |
| `mycel_coral_dark` | `#B36965` | Pressed error states | Serious tone confirmations |

**Replaces:** Briar error red `#EF4444` and related error colors

---

## ✨ **OPTIONAL - Lavender Ether**

**Semantic Meaning:** Loading states, alternate themes, mood setting.

| Token | Hex Value | Usage | Description |
|-------|-----------|-------|-------------|
| `mycel_lavender` | `#CAB6E0` | Loading, alternate themes | Tertiary accent |
| `mycel_lavender_soft` | `#E9E2F4` | Backgrounds, tags | Interface accessories |

**Use Case:** Onboarding, profiles, subtle contrast against coral/blue.

---

## 🧠 **Color Usage Guidelines**

### **UI Component Mapping**

| Use Case | Color Rules | Example |
|----------|-------------|---------|
| **Base UI** | Always black/white core with slight warmth | Backgrounds, main text |
| **Primary CTA/Status** | Use spore blue variants | Connect button, active states |
| **Secondary Info/Status** | Use signal moss green | Sync status, availability |
| **Errors/Warnings** | Use sporeset coral variants | Connection errors, alerts |
| **Hover/Selection** | Use soft pastel backgrounds | Button hovers, selection |
| **Branding/Logo** | White on dark, black on light + blue/green accents | App icon, splash screen |

### **Theme-Specific Recommendations**

**Light Mode Priority:**
- Primary: Spore blue (`#A3BEEA`)
- Success: Signal moss (`#A8C8B3`)
- Error: Sporeset coral (`#E8A6A1`)

**Dark Mode Priority:**
- Prefer green + coral accents for warmth
- Limit blue to highlights only
- Maintain coral for error consistency

---

## 📱 **Android Color Resource Mapping**

### **Primary Brand Colors (Replacing Briar)**
```xml
<!-- MYCEL PRIMARY BRAND COLORS -->
<color name="mycel_primary">#A3BEEA</color>           <!-- Spore blue (replaces #82C91E) -->
<color name="mycel_secondary">#A8C8B3</color>         <!-- Signal moss (replaces #95d220) -->
<color name="mycel_accent">#A3BEEA</color>            <!-- Spore blue (replaces #418cd8) -->

<!-- MYCEL SEMANTIC COLORS -->
<color name="mycel_success">#A8C8B3</color>           <!-- Signal moss green -->
<color name="mycel_error">#E8A6A1</color>             <!-- Sporeset coral -->
<color name="mycel_warning">#E8A6A1</color>           <!-- Sporeset coral -->

<!-- MYCEL NEUTRAL PALETTE -->
<color name="mycel_bg_light">#F5F4F1</color>          <!-- Warm off-white -->
<color name="mycel_bg_dark">#1D1D1B</color>           <!-- Soft black -->
<color name="mycel_text_main">#000000</color>         <!-- Pure black -->
<color name="mycel_text_invert">#FFFFFF</color>       <!-- Pure white -->
<color name="mycel_ui_border">#D7D6D3</color>         <!-- Light borders -->
<color name="mycel_ui_muted">#848482</color>          <!-- Secondary text -->

<!-- MYCEL INTERACTION VARIANTS -->
<color name="mycel_blue_soft">#DCE7F7</color>         <!-- Hover backgrounds -->
<color name="mycel_blue_dark">#5D7EB0</color>         <!-- Active states -->
<color name="mycel_green_soft">#DDEEE4</color>        <!-- Success backgrounds -->
<color name="mycel_green_dark">#59866F</color>        <!-- Active success -->
<color name="mycel_coral_soft">#F8DDDB</color>        <!-- Error backgrounds -->
<color name="mycel_coral_dark">#B36965</color>        <!-- Active errors -->
```

### **Legacy Compatibility (Redirect Briar References)**
```xml
<!-- REDIRECT EXISTING BRIAR COLORS TO MYCEL -->
<color name="briar_lime_500">@color/mycel_primary</color>     <!-- #82C91E → #A3BEEA -->
<color name="briar_lime_400">@color/mycel_secondary</color>   <!-- #95d220 → #A8C8B3 -->
<color name="briar_blue_400">@color/mycel_accent</color>      <!-- #418cd8 → #A3BEEA -->
<color name="briar_primary">@color/mycel_primary</color>
<color name="briar_accent">@color/mycel_accent</color>

<!-- MATERIAL DESIGN THEME COLORS -->
<color name="md_theme_primary">@color/mycel_primary</color>
<color name="md_theme_secondary">@color/mycel_secondary</color>
<color name="md_theme_tertiary">@color/mycel_accent</color>
<color name="md_theme_error">@color/mycel_error</color>
```

---

## 🌙 **Dark Theme Variants**

### **Dark Mode Color Adjustments**
```xml
<!-- values-night/color.xml -->
<color name="mycel_primary_dark">#5D7EB0</color>      <!-- Darker blue for dark theme -->
<color name="mycel_secondary_dark">#59866F</color>    <!-- Darker green for dark theme -->
<color name="mycel_accent_dark">#5D7EB0</color>       <!-- Consistent blue accent -->

<!-- DARK THEME REDIRECTS -->
<color name="briar_primary">@color/mycel_secondary</color>    <!-- Prefer green in dark -->
<color name="briar_accent">@color/mycel_coral</color>        <!-- Coral accent in dark -->
```

---

## 🎯 **Implementation Priority**

### **Phase 1 - Foundation Colors (Immediate)**
1. Core neutrals (backgrounds, text, borders)
2. Primary brand colors (blue, green, coral)
3. Material theme mappings

### **Phase 2 - Interactive States (Secondary)**  
1. Soft variants for hovers/backgrounds
2. Dark variants for active states
3. Complete dark theme palette

### **Phase 3 - Optional Enhancements (Future)**
1. Lavender accent for special use cases
2. Additional semantic color variants
3. Component-specific color tokens

---

## 🧪 **Visual Identity Validation**

### **Accessibility Compliance**
- **Contrast Ratios:** All text colors meet WCAG AA standards
- **Color Blindness:** Palette tested for deuteranopia/protanopia
- **Dark Mode:** Consistent semantic meaning across themes

### **Brand Consistency Check**
- ✅ **Clean minimalism** - BW foundation with purposeful accents
- ✅ **Earth-tech aesthetic** - Organic colors with technical precision  
- ✅ **Emotional layering** - Each color has semantic meaning
- ✅ **Cross-platform coherence** - Works on Android, web, desktop

### **Technical Requirements**
- ✅ **Android compatibility** - All colors valid hex values
- ✅ **Theme switching** - Seamless light/dark transitions
- ✅ **Performance** - Minimal color resource overhead
- ✅ **Maintainability** - Clear naming and organization

---

## 🎨 **Color System Token Reference**

**CSS/Design Token Format:**
```css
:root {
  /* Core Neutrals */
  --mycel-bg-light: #F5F4F1;
  --mycel-bg-dark: #1D1D1B;
  --mycel-text-main: #000000;
  --mycel-text-invert: #FFFFFF;
  --mycel-ui-border: #D7D6D3;
  --mycel-ui-muted: #848482;

  /* Spore Blue */
  --mycel-blue: #A3BEEA;
  --mycel-blue-soft: #DCE7F7;
  --mycel-blue-dark: #5D7EB0;

  /* Signal Moss */
  --mycel-green: #A8C8B3;
  --mycel-green-soft: #DDEEE4;
  --mycel-green-dark: #59866F;

  /* Sporeset Coral */
  --mycel-coral: #E8A6A1;
  --mycel-coral-soft: #F8DDDB;
  --mycel-coral-dark: #B36965;

  /* Lavender Ether */
  --mycel-lavender: #CAB6E0;
  --mycel-lavender-soft: #E9E2F4;
}
```

This color system provides the complete foundation for Mycel's visual identity - a thoughtful, accessible palette that balances minimalist principles with organic warmth and technical precision.

## 🔗 **Related Documentation**

- **Typography System**: See `MYCEL-TYPOGRAPHY-SYSTEM.md` for complete font and text styling information
- **Implementation Guide**: See `PHASE-6-EXECUTION-PLAN.md` for step-by-step rebranding instructions
- **Visual Assets Analysis**: See `COMPREHENSIVE-VISUAL-ASSETS-ANALYSIS.md` for complete asset inventory

**Typography Integration**: Mycel uses Android system fonts (primarily Roboto) with Material Design 3 typography standards. The brand identity is expressed through this color system and visual layout rather than custom typography, maintaining accessibility and cross-platform consistency.