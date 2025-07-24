# Mycel Visual Assets Implementation Guide

This guide provides step-by-step instructions for implementing Mycel visual assets to complete the rebranding from Briar to Mycel.

## Phase 6 Implementation Workflow

### Prerequisites
- ✅ Phase 1-5 completed (strings, packages, configuration)
- ✅ Visual asset inventory completed
- ✅ Mycel brand assets created and ready
- ✅ Asset folders prepared

### Implementation Steps Overview

#### Step 1: Asset Validation
Before implementation, verify all Mycel assets meet specifications:
- [ ] App icons in all required densities
- [ ] Notification icons (40 files total)
- [ ] Color palette defined with hex codes
- [ ] Splash screen with "MYCEL" text
- [ ] All assets tested for quality and consistency

#### Step 2: Systematic Replacement
Replace assets in priority order to minimize disruption:
1. **App Icons** (highest visibility)
2. **Splash Screen** (startup experience)  
3. **Color Scheme** (brand consistency)
4. **Notification Icons** (user interaction)
5. **UI Illustrations** (onboarding/empty states)

#### Step 3: Testing & Validation
After each asset type replacement:
- [ ] Build APK successfully
- [ ] Test on device/emulator
- [ ] Verify visual appearance
- [ ] Check dark mode compatibility

## Detailed Implementation Instructions

### 1. App Icons Replacement

#### Files to Replace:
```
mycel-android/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png → Replace with mycel_launcher_mdpi.png
│   └── ic_launcher_round.png → Replace with mycel_launcher_round_mdpi.png
├── mipmap-hdpi/
│   ├── ic_launcher.png → Replace with mycel_launcher_hdpi.png  
│   └── ic_launcher_round.png → Replace with mycel_launcher_round_hdpi.png
├── mipmap-xhdpi/
│   ├── ic_launcher.png → Replace with mycel_launcher_xhdpi.png
│   └── ic_launcher_round.png → Replace with mycel_launcher_round_xhdpi.png
├── mipmap-xxhdpi/
│   ├── ic_launcher.png → Replace with mycel_launcher_xxhdpi.png
│   └── ic_launcher_round.png → Replace with mycel_launcher_round_xxhdpi.png
├── mipmap-xxxhdpi/
│   ├── ic_launcher.png → Replace with mycel_launcher_xxxhdpi.png
│   └── ic_launcher_round.png → Replace with mycel_launcher_round_xxxhdpi.png
└── drawable/
    └── ic_launcher_foreground.xml → Replace with Mycel vector art
```

#### Implementation Commands:
```bash
# Copy new app icons (example - adjust paths as needed)
cp docs/visual-assets-inventory/mycel-assets/app-icons/mycel_launcher_*.png mycel-android/src/main/res/mipmap-*/ic_launcher.png

# Update vector foreground
cp docs/visual-assets-inventory/mycel-assets/app-icons/ic_launcher_foreground.xml mycel-android/src/main/res/drawable/
```

#### Update Background Color (if needed):
```xml
<!-- mycel-android/src/main/res/values/ic_launcher_background.xml -->
<color>#[NEW_MYCEL_BACKGROUND_COLOR]</color>
```

### 2. Splash Screen Replacement

#### File to Update:
```
mycel-android/src/main/res/drawable/splash_screen.xml
```

#### Critical Requirements:
- Replace "BRIAR" text with "MYCEL"
- Update color scheme to match Mycel brand
- Maintain same overall dimensions (235dp × 310dp)
- Preserve layout structure for proper rendering

#### Text Update Location:
```xml
<!-- Lines 40-70 contain the "BRIAR" text paths -->
<!-- Replace with "MYCEL" text paths -->
<path android:fillColor="#000000" 
      android:pathData="[NEW_MYCEL_TEXT_PATHS]"/>
```

### 3. Color Scheme Implementation

#### Primary File to Update:
```
mycel-android/src/main/res/values/color.xml
```

#### Implementation Strategy:
1. **Add Mycel Colors** (new brand colors):
```xml
<!-- Add Mycel brand colors -->
<color name="mycel_primary">#[PRIMARY_COLOR]</color>
<color name="mycel_accent">#[ACCENT_COLOR]</color>
<color name="mycel_secondary">#[SECONDARY_COLOR]</color>
```

2. **Update Brand References** (redirect existing references):
```xml
<!-- Update these existing color references -->
<color name="briar_primary">@color/mycel_primary</color>
<color name="briar_accent">@color/mycel_accent</color>
<color name="md_theme_primary">@color/mycel_primary</color>
```

3. **Gradual Migration** (optional - can replace briar colors entirely):
```xml
<!-- Replace briar_lime_* and briar_blue_* with mycel equivalents -->
<color name="briar_lime_500">@color/mycel_primary</color>
<color name="briar_blue_400">@color/mycel_accent</color>
```

#### Dark Theme Colors:
```
mycel-android/src/main/res/values-night/color.xml
```
Update dark mode variants of new Mycel colors.

### 4. Notification Icons Replacement

#### Files to Replace (40 total):
```bash
# Replace all notification PNG files
for density in mdpi hdpi xhdpi xxhdpi; do
    for icon in blog contact_added forum hotspot lock mailbox ongoing private_group private_message signout; do
        # Replace: mycel-android/src/main/res/drawable-${density}/notification_${icon}.png
        # With: docs/visual-assets-inventory/mycel-assets/notification-icons/notification_${icon}_${density}.png
    done
done

# Replace vector notification icons  
# mycel-android/src/main/res/drawable-anydpi-v24/notification_*.xml
```

### 5. UI Illustrations Update

#### Priority Illustrations to Replace:
1. **Empty state graphics** (`il_empty_state_*.xml`)
2. **Onboarding illustrations** (`il_add_contact_*.xml`, `il_qr_code_*.xml`)
3. **Feature illustrations** (`il_transfer_data_*.xml`, `il_mailbox_*.xml`)

#### Implementation Approach:
- Update color schemes to match Mycel brand
- Replace any Briar-specific visual elements
- Maintain functional clarity and recognition

### 6. Testing Protocol

#### After Each Asset Type Replacement:

1. **Build Test:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v17)
./gradlew :mycel-android:assembleOfficialDebug
```

2. **Visual Verification:**
- Install APK on test device
- Check app icon in launcher
- Test splash screen appearance
- Verify notification icons in notification bar
- Test both light and dark modes

3. **Functional Testing:**
- Ensure app launches correctly
- Verify all major features work
- Check that visual changes don't break functionality

### 7. Implementation Checklist

#### Pre-Implementation:
- [ ] All Mycel assets ready and validated
- [ ] Backup current Briar assets  
- [ ] Development environment prepared
- [ ] Test device available

#### Implementation Progress:
- [ ] App launcher icons replaced and tested
- [ ] App icon background color updated (if needed)
- [ ] Splash screen updated with MYCEL branding
- [ ] Color scheme updated in values/color.xml
- [ ] Dark theme colors updated in values-night/color.xml
- [ ] Notification icons replaced (all 40 files)
- [ ] Key UI illustrations updated

#### Post-Implementation Testing:
- [ ] APK builds without errors
- [ ] App installs and launches correctly  
- [ ] All notification types display properly
- [ ] Visual consistency maintained across app
- [ ] Dark mode works correctly
- [ ] No functional regressions

#### Final Validation:
- [ ] Brand consistency verified
- [ ] Professional appearance confirmed
- [ ] User experience maintained
- [ ] Ready for Phase 6 commit

### 8. Rollback Plan

If issues occur during implementation:

1. **Restore Original Assets:**
```bash
git checkout HEAD -- mycel-android/src/main/res/mipmap-*/
git checkout HEAD -- mycel-android/src/main/res/drawable/ic_launcher_foreground.xml
git checkout HEAD -- mycel-android/src/main/res/drawable/splash_screen.xml
```

2. **Selective Rollback:**
- Individual asset files can be restored as needed
- Color changes can be reverted in color.xml
- Test after each rollback to isolate issues

### 9. Commit Strategy

#### Approach: Single Comprehensive Commit
```bash
git add .
git commit -m "[PHASE-6] Replace Briar visual assets with Mycel branding

Updated visual identity from Briar to Mycel:
- App launcher icons: All densities (standard and round)
- Splash screen: Updated logo and MYCEL text
- Color scheme: New Mycel primary/accent colors
- Notification icons: All 40 icons across 4 densities
- Brand consistency: Updated color references throughout

Testing:
- ✅ Build: ./gradlew :mycel-android:assembleOfficialDebug
- ✅ APK: Installs and displays Mycel branding correctly
- ✅ Icons: All launcher and notification icons working
- ✅ Splash: MYCEL branding displays on startup

Phase Progress: 6/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

## Asset Organization

### Expected Mycel Asset Delivery Structure:
```
docs/visual-assets-inventory/mycel-assets/
├── app-icons/
│   ├── ic_launcher_foreground.xml
│   ├── mycel_launcher_mdpi.png (48×48)
│   ├── mycel_launcher_hdpi.png (72×72)
│   ├── mycel_launcher_xhdpi.png (96×96)
│   ├── mycel_launcher_xxhdpi.png (144×144)
│   ├── mycel_launcher_xxxhdpi.png (192×192)
│   ├── mycel_launcher_round_mdpi.png (48×48)
│   ├── mycel_launcher_round_hdpi.png (72×72)
│   ├── mycel_launcher_round_xhdpi.png (96×96)
│   ├── mycel_launcher_round_xxhdpi.png (144×144)
│   └── mycel_launcher_round_xxxhdpi.png (192×192)
├── notification-icons/
│   ├── [10 icon types] × [4 densities] = 40 PNG files
│   └── [10 vector XML files for anydpi-v24]
├── splash-screen/
│   └── splash_screen_mycel.xml
├── colors/
│   ├── mycel_colors.xml
│   └── mycel_colors_night.xml
└── illustrations/
    └── [Updated UI illustration files as needed]
```

This implementation guide provides the complete roadmap for Phase 6 visual asset replacement. Once Mycel assets are delivered, follow this guide systematically to complete the visual rebranding while maintaining app functionality and quality.