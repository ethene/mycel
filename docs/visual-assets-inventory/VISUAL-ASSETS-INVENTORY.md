# Mycel Visual Assets Inventory

This document catalogs all visual elements that need to be replaced during the Briar → Mycel rebranding process.

## 1. PRIMARY APP ICONS & LAUNCHER ASSETS

### App Launcher Icons (HIGH PRIORITY)
**Location:** `briar-android/src/main/res/mipmap-*/`
**Files to Replace:**
- `ic_launcher.png` (5 densities: mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- `ic_launcher_round.png` (5 densities: mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)

**Current Design:** Green interconnected bars forming a logo pattern
**Description:** The main app icon visible on Android home screens and app drawers. Critical for brand recognition.

### Vector App Icon Foreground
**Location:** `briar-android/src/main/res/drawable/ic_launcher_foreground.xml`
**Current Design:** Vector graphic with green (#87c214, #95d220) interconnected bar pattern
**Usage:** Used in adaptive icon generation for Android 8.0+

### App Icon Background
**Location:** `briar-android/src/main/res/values/ic_launcher_background.xml`
**Current Value:** `<color>#FFFFFF</color>` (White background)
**Usage:** Background for adaptive icons

## 2. SPLASH SCREEN & STARTUP VISUALS

### Main Splash Screen Logo
**Location:** `briar-android/src/main/res/drawable/splash_screen.xml`
**Current Design:** Large green interconnected bars with "BRIAR" text at bottom
**Colors Used:** `#87c214`, `#95d220` (green tones), `#000000` (black text)
**Dimensions:** 235dp × 310dp
**Usage:** First visual users see when app starts

**Critical Note:** This contains the "BRIAR" text that must be changed to "MYCEL"

## 3. NOTIFICATION ICONS (40 Files Total)

### Notification Icons by Density
**Locations:** `drawable-mdpi/`, `drawable-hdpi/`, `drawable-xhdpi/`, `drawable-xxhdpi/`
**Icons to Replace (10 types × 4 densities each):**

1. **notification_blog.png** - Blog post notifications
2. **notification_contact_added.png** - New contact added
3. **notification_forum.png** - Forum activity
4. **notification_hotspot.png** - WiFi hotspot status
5. **notification_lock.png** - App locked/unlocked
6. **notification_mailbox.png** - Mailbox activity
7. **notification_ongoing.png** - Background service running
8. **notification_private_group.png** - Private group messages
9. **notification_private_message.png** - Direct messages
10. **notification_signout.png** - User signed out

**Design Notes:** Small monochrome icons designed for Android notification bar

### Vector Notification Icons
**Location:** `drawable-anydpi-v24/`
**Files:** Same 10 notification types as vector XML files
**Usage:** Modern Android versions (API 24+)

## 4. ILLUSTRATION GRAPHICS & ONBOARDING

### Key User Interface Illustrations
**Location:** `briar-android/src/main/res/drawable/`

#### Contact & Connection Illustrations
- `il_add_contact_remote.xml` - Add remote contact screen
- `il_add_contact_remote_nickname.xml` - Contact nickname setup
- `il_bluetooth_connect.xml` - Bluetooth connection process
- `il_qr_code.xml` - QR code scanning illustration
- `il_qr_code_nearby.xml` - Nearby QR code scanning
- `il_qr_code_error.xml` - QR code scan error

#### Data & Communication Illustrations  
- `il_transfer_data.xml` - Data transfer process
- `il_transfer_data_receive.xml` - Receiving data
- `il_transfer_data_send.xml` - Sending data
- `il_share_app.xml` - App sharing screen

#### System & Setup Illustrations
- `il_mailbox.xml` - Mailbox setup/status
- `il_mailbox_setup.xml` - Mailbox configuration

#### Empty State Illustrations
- `il_empty_state_blogs.xml` - No blog posts
- `il_empty_state_contact_list.xml` - No contacts
- `il_empty_state_forum_list.xml` - No forums  
- `il_empty_state_group_list.xml` - No groups

**Design Characteristics:**
- Flat illustration style
- Green/blue color scheme matching brand
- Minimalist line art approach

## 5. NAVIGATION & UI GRAPHICS

### Navigation Elements
**Location:** `briar-android/src/main/res/drawable/`

- `navigation_drawer_header.xml` - Main nav drawer background
- `navigation_item_background.xml` - Nav item selection states

### Status & Connection Indicators
- `contact_connected.xml` - Online contact indicator  
- `contact_disconnected.xml` - Offline contact indicator
- `contact_offline.xml` - Contact offline status
- `contact_online.xml` - Contact online status

### Transport Icons
- `transport_bt.xml` - Bluetooth transport
- `transport_lan.xml` - LAN/WiFi transport  
- `transport_tor.xml` - Tor transport

## 6. BRAND COLOR SCHEME

### Primary Brand Colors
**Location:** `briar-android/src/main/res/values/color.xml`

#### Briar Green Palette (TO REPLACE)
```xml
<color name="briar_lime_950">#283E0F</color>
<color name="briar_lime_600">#67a60f</color>
<color name="briar_lime_500">#82C91E</color>
<color name="briar_lime_400">#82c91e</color>
<color name="briar_brand_green">@color/briar_lime_400</color>
```

#### Briar Blue Palette (TO REPLACE)
```xml
<color name="briar_blue_900">#184080</color>
<color name="briar_blue_800">#134a81</color>
<color name="briar_blue_600">#1b69b6</color>
<color name="briar_blue_400">#418cd8</color>
<color name="briar_brand_blue">@color/briar_night_700</color>
```

#### Theme Colors (CONSIDER FOR MYCEL BRAND)
```xml
<color name="briar_primary">@color/briar_brand_blue</color>
<color name="briar_accent">@color/briar_brand_blue</color>
```

### Supporting Colors (MAY NEED ADJUSTMENT)
- Orange warnings: `#fc9403`
- Red errors: `#db3b21`  
- Gray neutrals: Various shades
- Night mode colors: Dark theme variants

## 7. DECORATIVE & FUNCTIONAL GRAPHICS

### Message & Communication Graphics
- `msg_in.xml`, `msg_out.xml` - Message bubbles
- `notice_in.xml`, `notice_out.xml` - Notice bubbles
- `bubble.xml`, `bubble_white.xml` - Generic bubbles
- `message_delivered.xml`, `message_sent.xml` - Message status

### Trust & Security Indicators
- `trust_indicator_verified.xml` - Verified contact
- `trust_indicator_unverified.xml` - Unverified contact
- `trust_indicator_unknown.xml` - Unknown trust level

### Action & Control Icons
- Social sharing icons (`social_share_blue.xml`, `social_share_white.xml`)
- Various UI control icons (arrows, close buttons, etc.)

## 8. NIGHT MODE VARIANTS

### Dark Theme Graphics
**Location:** `briar-android/src/main/res/drawable-night/`
**Files:** Night mode versions of key illustrations and UI elements
- All major illustrations have dark variants
- Navigation elements adapted for dark theme
- Different color schemes for dark mode

### Night Mode Colors  
**Location:** `briar-android/src/main/res/values-night/color.xml`
- Dark theme color overrides
- Adjusted accent colors for dark backgrounds

## REPLACEMENT PRIORITY LEVELS

### 🔴 CRITICAL (Must Replace)
1. **App launcher icons** - Primary brand touchpoint
2. **Splash screen logo** - Contains "BRIAR" text
3. **Primary brand colors** - Core visual identity

### 🟡 HIGH (Should Replace)  
1. **Notification icons** - Frequent user exposure
2. **Main UI illustrations** - Key user journey graphics
3. **Navigation graphics** - Core UI elements

### 🟢 MEDIUM (Can Reuse if Appropriate)
1. **Functional icons** - Generic UI elements
2. **Message bubbles** - Communication interface
3. **Status indicators** - System state graphics

## MYCEL ASSET REQUIREMENTS

To complete the visual rebranding, Mycel equivalents needed:

### Required New Assets:
1. **Mycel logo** in multiple formats and sizes
2. **Mycel color palette** with primary/secondary/accent colors
3. **App icons** in all required Android densities  
4. **Splash screen** with "MYCEL" branding
5. **Notification icons** following Android design guidelines
6. **Illustration style guide** for onboarding/UI graphics

### Asset Specifications:
- **App Icons:** PNG format, multiple densities (36×36 to 192×192)
- **Illustrations:** Vector XML preferred for scalability  
- **Colors:** Hex values for consistent theming
- **Style:** Should maintain clean, professional messaging app aesthetic

## FOLDER STRUCTURE FOR MYCEL ASSETS

Recommended organization for replacement assets:
```
docs/visual-assets-inventory/
├── mycel-assets/
│   ├── app-icons/
│   │   ├── ic_launcher_mdpi.png
│   │   ├── ic_launcher_hdpi.png
│   │   └── ...
│   ├── notification-icons/
│   ├── illustrations/
│   ├── colors/
│   │   └── mycel-color-palette.xml
│   └── splash-screen/
└── briar-originals/
    ├── extracted-for-reference/
    └── descriptions/
```

This inventory provides the foundation for systematic visual asset replacement during Phase 6 implementation.