# App Icons - Detailed Specifications

## Current Briar App Icons Analysis

### Launcher Icon Files Required
Each icon needs to be provided in 5 different pixel densities for Android compatibility:

| Density | Folder | Pixel Size | DPI |
|---------|--------|------------|-----|
| MDPI | `mipmap-mdpi/` | 48×48px | 160 |
| HDPI | `mipmap-hdpi/` | 72×72px | 240 |
| XHDPI | `mipmap-xhdpi/` | 96×96px | 320 |
| XXHDPI | `mipmap-xxhdpi/` | 144×144px | 480 |
| XXXHDPI | `mipmap-xxxhdpi/` | 192×192px | 640 |

### Icon Types Needed

#### 1. Standard Launcher Icon (`ic_launcher.png`)
- **Purpose:** Default app icon for older Android versions
- **Shape:** Can be any shape, but typically square with rounded corners
- **Current Design:** Green interconnected bars/network pattern

#### 2. Round Launcher Icon (`ic_launcher_round.png`)
- **Purpose:** For circular icon display on some Android launchers
- **Shape:** Must fit within a circle
- **Current Design:** Same pattern as standard but optimized for circular boundary

### Vector Launcher Icon Components

#### Foreground Vector (`ic_launcher_foreground.xml`)
```xml
<!-- Current Briar foreground colors -->
android:fillColor="#87c214" (Light green)
android:fillColor="#95d220" (Brighter green)
```
- **Viewport:** 108×108dp (with safe area considerations)
- **Design:** Interconnected rectangular bars suggesting network/connectivity
- **Meaning:** Represents distributed/decentralized communication

#### Background Resource (`ic_launcher_background.xml`)
```xml
<color>#FFFFFF</color> <!-- Pure white -->
```

#### Adaptive Icon Configuration (`mipmap-anydpi-v26/ic_launcher.xml`)
```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

## Mycel Icon Requirements

### Design Considerations for Mycel
1. **Brand Identity:** Icon should represent "Mycel" concept (mycelium/fungal networks)
2. **Visual Consistency:** Maintain professional messaging app appearance
3. **Android Guidelines:** Follow Material Design icon principles
4. **Scalability:** Must be recognizable at smallest size (48×48px)

### Mycel Icon Specifications Needed

#### Color Palette
- **Primary Color:** Define main brand color for Mycel
- **Secondary Color:** Complementary/accent color
- **Background:** Consider whether white background fits new brand

#### Design Elements
- **Concept:** Network/connection theme appropriate for messaging
- **Style:** Modern, clean, professional
- **Differentiation:** Clearly distinct from Briar while maintaining app category recognition

### Deliverable Requirements

For each Mycel icon concept, provide:

1. **Vector Source Files**
   - SVG or AI format for foreground element
   - Scalable and editable

2. **Rendered PNG Files**
   - All 5 density sizes for both standard and round versions
   - High quality, optimized for Android

3. **Vector Android XML**
   - `ic_launcher_foreground.xml` equivalent for adaptive icons
   - Proper viewport and path definitions

4. **Color Specifications**
   - Hex codes for all colors used
   - Background color recommendation

### Icon Testing Checklist

Before finalizing Mycel icons, verify:
- [ ] Legible at 48×48px (smallest size)
- [ ] Recognizable when displayed in grayscale  
- [ ] Fits well in circular boundary (for round version)
- [ ] Consistent with messaging/communication app conventions
- [ ] Unique enough to avoid confusion with other apps
- [ ] Professional appearance suitable for business/personal use

### Implementation Files to Update

Once Mycel assets are ready:

1. **Replace PNG Files:**
   ```
   mycel-android/src/main/res/mipmap-mdpi/ic_launcher.png
   mycel-android/src/main/res/mipmap-mdpi/ic_launcher_round.png
   [... and all other densities]
   ```

2. **Update Vector Foreground:**
   ```
   mycel-android/src/main/res/drawable/ic_launcher_foreground.xml
   ```

3. **Update Background (if changed):**
   ```
   mycel-android/src/main/res/values/ic_launcher_background.xml
   ```

### Design Inspiration Notes

Current Briar icon concept: Interconnected bars/network nodes
- Represents decentralized communication
- Clean, technical aesthetic
- Green color scheme suggests growth/organic networks

Possible Mycel concepts:
- Mycelium network patterns (organic, interconnected)
- Fungal branching structures
- Network nodes with organic flow
- Communication pathways with natural/biological theme

The icon should maintain the core concept of "distributed communication networks" while shifting from the current geometric/technical style to something that better represents the "Mycel" (mycelium) brand concept.