# Notification Icons - Detailed Specifications

## Current Briar Notification Icons

### Icon Types (10 Different Notifications)

#### 1. Blog Notifications (`notification_blog.png`)
- **Purpose:** New blog posts, blog comments, blog sharing invitations
- **Current Design:** Document/page icon with folded corner
- **Usage Frequency:** Medium (depends on blog feature usage)

#### 2. Contact Added (`notification_contact_added.png`)
- **Purpose:** New contact requests, contact confirmations
- **Current Design:** Person icon with plus symbol
- **Usage Frequency:** High (core functionality)

#### 3. Forum Activity (`notification_forum.png`)
- **Purpose:** New forum posts, forum invitations, forum updates
- **Current Design:** Multiple overlapping speech bubbles
- **Usage Frequency:** Medium-high (social features)

#### 4. Hotspot Status (`notification_hotspot.png`)
- **Purpose:** WiFi hotspot enabled/disabled, hotspot connection status
- **Current Design:** WiFi signal with hotspot indicator
- **Usage Frequency:** Medium (depends on feature usage)

#### 5. App Lock Status (`notification_lock.png`)
- **Purpose:** App locked, unlock reminders, security alerts
- **Current Design:** Padlock icon
- **Usage Frequency:** High (security is core feature)

#### 6. Mailbox Activity (`notification_mailbox.png`)
- **Purpose:** Mailbox setup, mailbox synchronization, mailbox errors
- **Current Design:** Envelope/mailbox icon
- **Usage Frequency:** Medium (advanced feature)

#### 7. Background Service (`notification_ongoing.png`)
- **Purpose:** App running in background, maintaining connections
- **Current Design:** Network/connection indicator
- **Usage Frequency:** High (persistent while app active)

#### 8. Private Group Messages (`notification_private_group.png`)
- **Purpose:** New group messages, group invitations, group updates
- **Current Design:** Multiple person icons in group formation
- **Usage Frequency:** High (core messaging feature)

#### 9. Private Messages (`notification_private_message.png`)
- **Purpose:** New direct messages, message delivery confirmations
- **Current Design:** Single speech bubble
- **Usage Frequency:** Very High (primary app function)

#### 10. Sign Out Alert (`notification_signout.png`)
- **Purpose:** User signed out, session expired
- **Current Design:** Exit/logout symbol
- **Usage Frequency:** Low (only when signed out)

### File Specifications

#### Density Requirements
Each icon must be provided in 4 densities + vector format:

| Density | Folder | Pixel Size | Usage |
|---------|--------|------------|-------|
| MDPI | `drawable-mdpi/` | 24×24px | Low-density screens |
| HDPI | `drawable-hdpi/` | 36×36px | Medium-density screens |
| XHDPI | `drawable-xhdpi/` | 48×48px | High-density screens |  
| XXHDPI | `drawable-xxhdpi/` | 72×72px | Extra high-density screens |
| Vector | `drawable-anydpi-v24/` | Scalable XML | Modern Android (API 24+) |

#### Design Requirements
- **Color:** Pure white (`#FFFFFF`) or transparency
- **Style:** Simple, monochromatic, high contrast
- **Background:** Transparent PNG
- **Format:** PNG (bitmap) + XML (vector)
- **Guidelines:** Follow Android notification icon guidelines

## Android Notification Icon Guidelines

### Design Principles
1. **Monochromatic:** Single color (usually white) on transparent background
2. **Simple:** Easily recognizable at small sizes (24×24px)  
3. **Universal:** Clear meaning without text labels
4. **Consistent:** Unified style across all notification types

### Technical Requirements
- **Transparency:** Use alpha channel for shape definition
- **Color:** System applies color overlay; icons should be white/transparent
- **Padding:** Leave some margin for system-generated background circles
- **Vector preferred:** XML vectors scale better across densities

### System Behavior
- Android applies colored background circles automatically
- Icons appear in notification bar, lock screen, and notification shade
- Different Android versions may style differently
- Dark/light themes handled by system

## Mycel Notification Icon Requirements

### Design Considerations for Mycel
1. **Brand Consistency:** Icons should align with Mycel visual identity
2. **Mycelium Theme:** Consider organic, network-like design elements
3. **Clarity:** Must remain clear at smallest display size
4. **Differentiation:** Distinct from Briar while maintaining functional clarity

### Icon Redesign Priorities

#### 🔴 Critical (Must Redesign)
1. **Private Messages** - Most frequently used
2. **Background Service** - Always visible when app active
3. **Contact Added** - Core functionality visibility

#### 🟡 High Priority
1. **Private Group Messages** - Important messaging feature
2. **App Lock Status** - Security visibility
3. **Forum Activity** - Social feature prominence

#### 🟢 Medium Priority  
1. **Blog Notifications** - Feature-specific
2. **Mailbox Activity** - Advanced feature
3. **Hotspot Status** - Situational usage
4. **Sign Out Alert** - Infrequent occurrence

### Mycel Icon Concepts

#### Design Direction Options:

**Option 1: Organic Network Theme**
- Use subtle mycelium-like branching patterns
- Maintain functional clarity while adding organic elements
- Network connections with natural flow

**Option 2: Refined Minimalism**
- Clean, geometric approach with Mycel brand elements
- Consistent stroke width and corner radius
- Modern, professional appearance

**Option 3: Hybrid Approach**
- Functional base icons with subtle organic touches  
- Mycel brand elements integrated naturally
- Balance between recognition and brand expression

### Specific Icon Recommendations

#### Private Message (`notification_private_message.png`)
- **Current:** Simple speech bubble
- **Mycel Option:** Speech bubble with subtle network connection points
- **Priority:** Highest - most visible notification

#### Background Service (`notification_ongoing.png`)  
- **Current:** Network connectivity indicator
- **Mycel Option:** Organic network pattern showing active connections
- **Priority:** High - persistent visibility

#### Contact Added (`notification_contact_added.png`)
- **Current:** Person with plus symbol
- **Mycel Option:** Person icon with organic connection/growth element
- **Priority:** High - important for user onboarding

### Deliverable Requirements

For each Mycel notification icon:

1. **Vector Source Files**
   - SVG format for editing and modification
   - Proper layering and organization

2. **PNG Bitmap Files**  
   - 4 density sizes (24×24 to 72×72px)
   - Transparent background
   - Pure white (#FFFFFF) color
   - Optimized file size

3. **XML Vector Files**
   - Android vector drawable format
   - Scalable and efficient
   - Proper viewport and path definitions

4. **Style Guide**
   - Consistent design principles across all icons
   - Usage guidelines and specifications

### Testing Requirements

#### Functional Testing:
- [ ] Visible in notification bar at all system sizes
- [ ] Clear at smallest size (24×24px)  
- [ ] Recognizable with system color overlays
- [ ] Consistent appearance across Android versions

#### Brand Testing:
- [ ] Aligns with Mycel visual identity
- [ ] Maintains functional clarity
- [ ] Professional appearance
- [ ] Distinguishable from competitor apps

### Implementation Plan

#### Phase 1: High-Priority Icons
1. Create designs for private messages, background service, contact added
2. Test at multiple sizes and with different system themes
3. Implement and verify functionality

#### Phase 2: Medium-Priority Icons  
1. Design remaining notification icons
2. Ensure style consistency across full set
3. Complete implementation and testing

#### Files to Update:
```
briar-android/src/main/res/
├── drawable-mdpi/notification_*.png
├── drawable-hdpi/notification_*.png  
├── drawable-xhdpi/notification_*.png
├── drawable-xxhdpi/notification_*.png
└── drawable-anydpi-v24/notification_*.xml
```

### Quality Assurance Checklist

Before deployment:
- [ ] All 10 icon types redesigned and implemented
- [ ] All 4 density sizes generated for each icon
- [ ] Vector XML versions created for modern Android
- [ ] Icons tested on multiple device types and Android versions
- [ ] Visual consistency verified across complete set
- [ ] Brand alignment confirmed with Mycel identity
- [ ] Functional clarity maintained for all notification types

The notification icons are critical for user experience as they provide immediate visual feedback for app activity. The Mycel redesigns should enhance brand recognition while maintaining the clear functional communication users expect from notification systems.