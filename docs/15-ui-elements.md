# UI Elements and Components Documentation

## Overview

This document catalogs all user interface elements, components, and visual assets in the Briar/Mycel Android application. Understanding these elements is crucial for effective rebranding and maintaining UI consistency.

## Material Design Architecture

### Design System Foundation

**Base Theme**: Material Components for Android (Material Design 3)
- **Primary Color**: `#2E7D32` (Green)
- **Accent Color**: `#4CAF50` (Light Green)
- **Surface Colors**: Adaptive based on light/dark theme
- **Typography**: Roboto font family

### Theme Configuration

**File**: `briar-android/src/main/res/values/styles.xml`

```xml
<style name="BriarTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- Primary brand color -->
    <item name="colorPrimary">@color/briar_primary</item>
    <item name="colorPrimaryDark">@color/briar_primary_dark</item>
    <item name="colorAccent">@color/briar_accent</item>
    
    <!-- Surface colors -->
    <item name="colorSurface">@color/briar_surface</item>
    <item name="colorOnSurface">@color/briar_on_surface</item>
    
    <!-- Material Design 3 tokens -->
    <item name="colorPrimaryContainer">@color/briar_primary_container</item>
    <item name="colorOnPrimaryContainer">@color/briar_on_primary_container</item>
</style>
```

## Core UI Components

### Navigation Components

#### 1. Navigation Drawer

**Location**: `NavDrawerFragment.java`

**Components**:
- **Header Section**: User profile, app logo
- **Contact List**: RecyclerView with contact items
- **Transport Status**: Tor/Bluetooth/LAN indicators
- **Settings Access**: Gear icon button

**Layout**: `fragment_nav_drawer.xml`
```xml
<androidx.drawerlayout.widget.DrawerLayout>
    <include layout="@layout/nav_drawer_header" />
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/contact_list"
        android:layout_below="@id/nav_header" />
    <LinearLayout android:id="@+id/transport_status_bar" />
</androidx.drawerlayout.widget.DrawerLayout>
```

#### 2. Bottom Navigation (Future Enhancement)

Currently using navigation drawer, but architecture supports bottom navigation:
- **Messages Tab**: Private conversations
- **Forums Tab**: Public discussions  
- **Contacts Tab**: Contact management
- **Settings Tab**: Application settings

### Message UI Components

#### 1. Conversation View

**Layout**: `activity_conversation.xml`

**Key Elements**:
- **Toolbar**: Contact name, verification status, options menu
- **Message List**: RecyclerView with message bubbles
- **Input Area**: Text input, send button, attachment button
- **Status Indicators**: Message delivery status

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout>
    <androidx.appcompat.widget.Toolbar android:id="@+id/toolbar" />
    
    <androidx.recyclerview.widget.RecyclerView 
        android:id="@+id/message_list"
        app:layoutManager="LinearLayoutManager" />
        
    <LinearLayout android:id="@+id/input_container"
        android:orientation="horizontal">
        <EditText android:id="@+id/message_input" />
        <ImageButton android:id="@+id/send_button" />
    </LinearLayout>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

#### 2. Message Bubbles

**Incoming Messages**: `list_item_conversation_msg_in.xml`
```xml
<LinearLayout android:orientation="horizontal">
    <org.briarproject.briar.android.view.TrustIndicatorView
        android:id="@+id/trust_indicator" />
    
    <LinearLayout android:orientation="vertical"
        style="@style/MessageBubbleIncoming">
        <TextView android:id="@+id/message_text" />
        <TextView android:id="@+id/timestamp" />
    </LinearLayout>
</LinearLayout>
```

**Outgoing Messages**: `list_item_conversation_msg_out.xml`
```xml
<LinearLayout android:orientation="horizontal"
    android:gravity="end">
    
    <LinearLayout android:orientation="vertical"
        style="@style/MessageBubbleOutgoing">
        <TextView android:id="@+id/message_text" />
        <LinearLayout android:orientation="horizontal">
            <TextView android:id="@+id/timestamp" />
            <ImageView android:id="@+id/delivery_status" />
        </LinearLayout>
    </LinearLayout>
</LinearLayout>
```

### Contact Management UI

#### 1. Contact List

**Layout**: `fragment_contact_list.xml`

**Components**:
- **SwipeRefreshLayout**: Pull-to-refresh functionality
- **RecyclerView**: Contact list with avatars and status
- **FloatingActionButton**: Add new contact
- **Empty State**: Message when no contacts

#### 2. Contact Item

**Layout**: `list_item_contact.xml`
```xml
<androidx.cardview.widget.CardView>
    <LinearLayout android:orientation="horizontal">
        <org.briarproject.briar.android.view.AvatarView
            android:id="@+id/avatar" />
            
        <LinearLayout android:orientation="vertical">
            <TextView android:id="@+id/contact_name" />
            <TextView android:id="@+id/last_message_preview" />
        </LinearLayout>
        
        <LinearLayout android:orientation="vertical"
            android:gravity="end">
            <TextView android:id="@+id/timestamp" />
            <org.briarproject.briar.android.view.UnreadCountView
                android:id="@+id/unread_count" />
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

#### 3. Add Contact Flow

**QR Code Scanner**: `activity_qr_code_scanner.xml`
- **Camera Preview**: Full-screen camera view
- **Overlay**: QR code targeting overlay
- **Instructions**: Text guidance for scanning

**Contact Addition**: `activity_add_contact.xml`
- **Options Card**: Scan QR, nearby contact, remote addition
- **Form Fields**: Contact alias, verification steps

### Forum UI Components

#### 1. Forum List

**Layout**: `activity_forum_list.xml`

**Components**:
- **Toolbar**: Search functionality
- **RecyclerView**: Forum list items
- **FloatingActionButton**: Create new forum
- **Categories**: Grouped forum display

#### 2. Forum Item

**Layout**: `list_item_forum.xml`
```xml
<androidx.cardview.widget.CardView>
    <LinearLayout android:orientation="vertical">
        <LinearLayout android:orientation="horizontal">
            <TextView android:id="@+id/forum_name" />
            <TextView android:id="@+id/member_count" />
        </LinearLayout>
        
        <TextView android:id="@+id/forum_description" />
        
        <LinearLayout android:orientation="horizontal">
            <TextView android:id="@+id/last_post_preview" />
            <TextView android:id="@+id/timestamp" />
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

#### 3. Forum Posts

**Layout**: `list_item_forum_post.xml`
```xml
<androidx.cardview.widget.CardView>
    <LinearLayout android:orientation="vertical">
        <LinearLayout android:orientation="horizontal">
            <org.briarproject.briar.android.view.AvatarView
                android:id="@+id/author_avatar" />
            <TextView android:id="@+id/author_name" />
            <TextView android:id="@+id/timestamp" />
        </LinearLayout>
        
        <TextView android:id="@+id/post_content" />
        
        <LinearLayout android:orientation="horizontal">
            <ImageButton android:id="@+id/reply_button" />
            <ImageButton android:id="@+id/quote_button" />
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

### Settings UI Components

#### 1. Settings Activity

**Layout**: `activity_settings.xml`
- **PreferenceFragment**: Standard Android settings
- **Categories**: Grouped settings organization

#### 2. Preference Categories

**Notifications**:
- Enable/disable notifications
- Sound selection
- Vibration patterns
- LED color

**Security**:
- Screen lock requirement
- Auto-lock timeout
- Fingerprint/biometric unlock
- PIN/password settings

**Network**:
- Tor settings
- Bluetooth settings
- Wi-Fi direct settings
- Mobile data usage

**Appearance**:
- Theme selection (Light/Dark/Auto)
- Language selection
- Font size

## Custom UI Components

### 1. Trust Indicator View

**File**: `TrustIndicatorView.java`

**Purpose**: Visual indicator of contact verification status

```java
public class TrustIndicatorView extends View {
    private boolean trusted = false;
    private Paint trustedPaint;
    private Paint untrustedPaint;
    
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = trusted ? trustedPaint : untrustedPaint;
        float radius = Math.min(getWidth(), getHeight()) / 2f;
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, paint);
    }
}
```

**Visual States**:
- **Verified**: Green circle with checkmark
- **Unverified**: Gray circle with question mark
- **Warning**: Red circle with exclamation mark

### 2. Transport Status Indicator

**File**: `TransportStateIndicator.java`

**Purpose**: Shows connectivity status for Tor, Bluetooth, and LAN

```java
public class TransportStateIndicator extends View {
    private TransportState torState;
    private TransportState bluetoothState;
    private TransportState lanState;
    
    @Override
    protected void onDraw(Canvas canvas) {
        drawTransportIndicator(canvas, torPaint, torState, 0);
        drawTransportIndicator(canvas, bluetoothPaint, bluetoothState, 1);
        drawTransportIndicator(canvas, lanPaint, lanState, 2);
    }
}
```

**States**:
- **Connected**: Bright color, solid circle
- **Connecting**: Medium color, animated pulse
- **Disconnected**: Dim color, empty circle
- **Disabled**: Gray, crossed out

### 3. Avatar View

**File**: `AvatarView.java`

**Purpose**: Displays contact avatars with fallback to initials

```java
public class AvatarView extends ImageView {
    private String contactName;
    private boolean showOnlineStatus;
    
    public void setContact(Contact contact) {
        this.contactName = contact.getDisplayName();
        
        // Try to load avatar image, fallback to initials
        if (contact.hasAvatar()) {
            setImageFromContact(contact);
        } else {
            drawInitials(contactName);
        }
    }
}
```

### 4. Unread Count Badge

**File**: `UnreadCountView.java`

**Purpose**: Shows unread message count on contact items

```java
public class UnreadCountView extends TextView {
    public void setUnreadCount(int count) {
        if (count > 0) {
            setVisibility(VISIBLE);
            setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            setVisibility(GONE);
        }
    }
}
```

## Icon System

### Material Design Icons

**Navigation Icons**:
- `ic_menu_24dp` - Navigation drawer toggle
- `ic_arrow_back_24dp` - Back navigation
- `ic_close_24dp` - Close/cancel actions
- `ic_search_24dp` - Search functionality

**Communication Icons**:
- `ic_send_24dp` - Send message
- `ic_attach_file_24dp` - File attachment
- `ic_photo_24dp` - Photo attachment
- `ic_mic_24dp` - Voice message

**Feature Icons**:
- `ic_forum_24dp` - Forum discussions
- `ic_rss_feed_24dp` - Blog/RSS feeds
- `ic_group_24dp` - Private groups
- `ic_person_add_24dp` - Add contact

**Status Icons**:
- `ic_check_24dp` - Message delivered
- `ic_done_all_24dp` - Message read
- `ic_schedule_24dp` - Message pending
- `ic_error_24dp` - Message failed

**Transport Icons**:
- `ic_tor` - Tor network status
- `ic_bluetooth_24dp` - Bluetooth status
- `ic_wifi_24dp` - Wi-Fi/LAN status

### App Icons and Branding

**App Launcher Icons**:
- `ic_launcher.xml` - Adaptive icon (API 26+)
- `ic_launcher_background.xml` - Adaptive icon background
- `ic_launcher_foreground.xml` - Adaptive icon foreground
- `mipmap-*/ic_launcher.png` - Legacy icons (all densities)

**Notification Icons**:
- `ic_notification` - Status bar notification
- `ic_notification_large` - Notification large icon

**Splash Screen**:
- `logo_circle.xml` - App logo for splash screen
- `splash_background.xml` - Splash screen background

## Color System

### Brand Colors

**Primary Palette**:
```xml
<color name="briar_primary">#2E7D32</color>          <!-- Dark Green -->
<color name="briar_primary_dark">#1B5E20</color>     <!-- Darker Green -->
<color name="briar_accent">#4CAF50</color>           <!-- Light Green -->
<color name="briar_primary_light">#66BB6A</color>    <!-- Lighter Green -->
```

**Material Design 3 Tokens**:
```xml
<color name="briar_primary_container">#A8DADC</color>
<color name="briar_on_primary_container">#001E1F</color>
<color name="briar_surface">#FEFBFF</color>
<color name="briar_on_surface">#1C1B1F</color>
<color name="briar_surface_variant">#E0E3E3</color>
<color name="briar_on_surface_variant">#404648</color>
```

### Semantic Colors

**Message Colors**:
```xml
<color name="message_bubble_incoming">#E8F5E8</color>  <!-- Light green -->
<color name="message_bubble_outgoing">#DCF8C6</color>  <!-- WhatsApp-style green -->
<color name="message_text_incoming">#1B5E20</color>    <!-- Dark green text -->
<color name="message_text_outgoing">#2E7D32</color>    <!-- Green text -->
```

**Status Colors**:
```xml
<color name="status_online">#4CAF50</color>           <!-- Green -->
<color name="status_away">#FF9800</color>             <!-- Orange -->
<color name="status_offline">#9E9E9E</color>          <!-- Gray -->
<color name="status_error">#F44336</color>            <!-- Red -->
```

**Transport Colors**:
```xml
<color name="transport_tor">#7E57C2</color>           <!-- Purple -->
<color name="transport_bluetooth">#2196F3</color>     <!-- Blue -->
<color name="transport_lan">#FF9800</color>           <!-- Orange -->
```

### Dark Theme Support

**Dark Theme Colors**:
```xml
<!-- values-night/colors.xml -->
<color name="briar_surface">#121212</color>
<color name="briar_on_surface">#E3E3E3</color>
<color name="message_bubble_incoming">#2C5F2D</color>
<color name="message_bubble_outgoing">#1B5E20</color>
```

## Typography System

### Font Hierarchy

**File**: `values/styles.xml`

```xml
<!-- Headings -->
<style name="TextAppearance.Briar.Headline1" parent="TextAppearance.MaterialComponents.Headline1">
    <item name="fontFamily">@font/roboto_medium</item>
    <item name="android:textColor">?colorOnSurface</item>
</style>

<!-- Body text -->
<style name="TextAppearance.Briar.Body1" parent="TextAppearance.MaterialComponents.Body1">
    <item name="fontFamily">@font/roboto_regular</item>
    <item name="android:textColor">?colorOnSurface</item>
</style>

<!-- Captions -->
<style name="TextAppearance.Briar.Caption" parent="TextAppearance.MaterialComponents.Caption">
    <item name="fontFamily">@font/roboto_regular</item>
    <item name="android:textColor">?colorOnSurfaceVariant</item>
</style>
```

### Font Files

**Location**: `res/font/`
- `roboto_regular.ttf` - Body text
- `roboto_medium.ttf` - Headings and emphasis
- `roboto_mono_regular.ttf` - Code/technical text

## Accessibility Features

### Content Descriptions

**Navigation Elements**:
```xml
<string name="accessibility_nav_drawer_open">Open navigation drawer</string>
<string name="accessibility_nav_drawer_close">Close navigation drawer</string>
<string name="accessibility_back_button">Navigate back</string>
```

**Message Elements**:
```xml
<string name="accessibility_send_message">Send message</string>
<string name="accessibility_message_from">Message from %1$s</string>
<string name="accessibility_message_timestamp">Sent at %1$s</string>
<string name="accessibility_message_delivered">Message delivered</string>
<string name="accessibility_message_pending">Message pending</string>
```

**Contact Elements**:
```xml
<string name="accessibility_contact_verified">Contact is verified</string>
<string name="accessibility_contact_unverified">Contact is not verified</string>
<string name="accessibility_add_contact">Add new contact</string>
```

### Screen Reader Support

**Live Regions**: Important updates announced automatically
**Focus Management**: Proper focus order for screen readers
**Alternative Text**: All images have meaningful descriptions

## Animation System

### Transition Animations

**Activity Transitions**:
```xml
<!-- res/anim/slide_in_right.xml -->
<translate xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromXDelta="100%p"
    android:toXDelta="0"
    android:duration="300" />
```

**Fragment Transitions**:
- **Slide**: Horizontal sliding between fragments
- **Fade**: Cross-fade for overlay fragments
- **Scale**: Scale in/out for modals

### Material Motion

**Shared Element Transitions**: Contact avatar to conversation header
**Container Transforms**: List item to detail view
**Elevation Changes**: Card lifting on touch

## Responsive Design

### Screen Size Support

**Phone Layouts** (`layout/`):
- Single-pane navigation
- Bottom-oriented UI elements
- Optimized for one-handed use

**Tablet Layouts** (`layout-sw600dp/`):
- Two-pane master-detail
- Side navigation always visible
- More horizontal space utilization

**Landscape Layouts** (`layout-land/`):
- Optimized input areas
- Reduced vertical padding
- Side-by-side content arrangement

## Rebranding UI Elements for Mycel

### High Priority Changes

#### 1. App Icon and Logo
```
Current: Briar logo (interconnected nodes)
New: Mycel logo (to be designed by Quantum Research)

Files to update:
- res/mipmap-*/ic_launcher.png
- res/mipmap-*/ic_launcher_round.png
- res/drawable/ic_launcher_foreground.xml
- res/drawable/logo_circle.xml
```

#### 2. Color Scheme
```
Consider updating primary color scheme:
Current: Green (#2E7D32, #4CAF50)
New: Quantum Research brand colors (TBD)

Files to update:
- res/values/colors.xml
- res/values-night/colors.xml
```

#### 3. App Name References
```xml
<!-- Update all string resources -->
<string name="app_name">Mycel</string>
<string name="app_name_formatted">Mycel</string>
<string name="about_app_title">About Mycel</string>
<string name="copyright_notice">© 2024 Quantum Research Pty Ltd</string>
```

### Medium Priority Changes

#### 1. Splash Screen
- Update splash screen logo
- Consider new brand animation
- Update splash screen colors

#### 2. About Screen
- Update developer information
- Update copyright notices
- Update contact information

#### 3. Notification Icons
- Update notification icon design
- Ensure brand consistency

### Implementation Strategy

1. **Asset Replacement**: Create new Mycel assets matching existing dimensions
2. **Color System**: Define new Mycel color palette
3. **String Updates**: Global find/replace for app name references
4. **Testing**: Verify UI consistency across all screens
5. **Accessibility**: Update accessibility strings with new brand name

The UI system is well-structured and will support Mycel rebranding efficiently through systematic asset replacement and string updates while maintaining all functionality and accessibility features.