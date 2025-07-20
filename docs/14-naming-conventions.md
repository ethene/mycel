# Naming Conventions and Patterns

## Overview

This document outlines the consistent naming patterns and conventions used throughout the Briar/Mycel codebase. Understanding these patterns is essential for the rebranding process and maintaining code consistency.

## Package Naming Conventions

### Current Briar Package Structure

```
org.briarproject.bramble.*    ← Infrastructure Layer
org.briarproject.briar.*     ← Application Layer
```

#### Bramble Layer Packages
```
org.briarproject.bramble.api.*           ← API interfaces
org.briarproject.bramble.crypto.*        ← Cryptographic services
org.briarproject.bramble.db.*            ← Database layer
org.briarproject.bramble.plugin.*        ← Transport plugins
org.briarproject.bramble.sync.*          ← Synchronization protocol
org.briarproject.bramble.event.*         ← Event system
org.briarproject.bramble.contact.*       ← Contact management
org.briarproject.bramble.identity.*      ← Identity management
org.briarproject.bramble.transport.*     ← Transport layer
org.briarproject.bramble.network.*       ← Network management
org.briarproject.bramble.keyagreement.*  ← Key agreement protocols
org.briarproject.bramble.record.*        ← Data record management
org.briarproject.bramble.versioning.*    ← Version management
```

#### Briar Layer Packages
```
org.briarproject.briar.api.*             ← Application API
org.briarproject.briar.messaging.*       ← Private messaging
org.briarproject.briar.forum.*           ← Forum functionality
org.briarproject.briar.blog.*            ← Blog functionality
org.briarproject.briar.privategroup.*    ← Private groups
org.briarproject.briar.sharing.*         ← Content sharing
org.briarproject.briar.introduction.*    ← Contact introduction
org.briarproject.briar.feed.*            ← RSS feed handling
org.briarproject.briar.client.*          ← Client architecture
org.briarproject.briar.attachment.*      ← File attachments
```

#### Android Application Packages
```
org.briarproject.briar.android.*         ← Android UI layer
org.briarproject.briar.android.activity.*      ← Activities
org.briarproject.briar.android.fragment.*      ← Fragments
org.briarproject.briar.android.adapter.*       ← RecyclerView adapters
org.briarproject.briar.android.view.*          ← Custom views
org.briarproject.briar.android.settings.*      ← Settings screens
org.briarproject.briar.android.contact.*       ← Contact UI
org.briarproject.briar.android.conversation.*  ← Conversation UI
org.briarproject.briar.android.forum.*         ← Forum UI
org.briarproject.briar.android.blog.*          ← Blog UI
org.briarproject.briar.android.util.*          ← Android utilities
org.briarproject.briar.android.viewmodel.*     ← MVVM ViewModels
```

### Proposed Mycel Package Structure

```
com.quantumresearch.mycel.infrastructure.*  ← Infrastructure Layer (was bramble)
com.quantumresearch.mycel.app.*             ← Application Layer (was briar)
com.quantumresearch.mycel.android.*         ← Android UI Layer
```

#### Infrastructure Layer (Mycel)
```
com.quantumresearch.mycel.infrastructure.api.*           ← API interfaces
com.quantumresearch.mycel.infrastructure.crypto.*        ← Cryptographic services
com.quantumresearch.mycel.infrastructure.db.*            ← Database layer
com.quantumresearch.mycel.infrastructure.plugin.*        ← Transport plugins
com.quantumresearch.mycel.infrastructure.sync.*          ← Synchronization protocol
com.quantumresearch.mycel.infrastructure.event.*         ← Event system
com.quantumresearch.mycel.infrastructure.contact.*       ← Contact management
com.quantumresearch.mycel.infrastructure.identity.*      ← Identity management
com.quantumresearch.mycel.infrastructure.transport.*     ← Transport layer
com.quantumresearch.mycel.infrastructure.network.*       ← Network management
com.quantumresearch.mycel.infrastructure.keyagreement.*  ← Key agreement protocols
com.quantumresearch.mycel.infrastructure.record.*        ← Data record management
com.quantumresearch.mycel.infrastructure.versioning.*    ← Version management
```

#### Application Layer (Mycel)
```
com.quantumresearch.mycel.app.api.*             ← Application API
com.quantumresearch.mycel.app.messaging.*       ← Private messaging
com.quantumresearch.mycel.app.forum.*           ← Forum functionality
com.quantumresearch.mycel.app.blog.*            ← Blog functionality
com.quantumresearch.mycel.app.privategroup.*    ← Private groups
com.quantumresearch.mycel.app.sharing.*         ← Content sharing
com.quantumresearch.mycel.app.introduction.*    ← Contact introduction
com.quantumresearch.mycel.app.feed.*            ← RSS feed handling
com.quantumresearch.mycel.app.client.*          ← Client architecture
com.quantumresearch.mycel.app.attachment.*      ← File attachments
```

#### Android Layer (Mycel)
```
com.quantumresearch.mycel.android.*                    ← Android UI layer
com.quantumresearch.mycel.android.activity.*           ← Activities
com.quantumresearch.mycel.android.fragment.*           ← Fragments
com.quantumresearch.mycel.android.adapter.*            ← RecyclerView adapters
com.quantumresearch.mycel.android.view.*               ← Custom views
com.quantumresearch.mycel.android.settings.*           ← Settings screens
com.quantumresearch.mycel.android.contact.*            ← Contact UI
com.quantumresearch.mycel.android.conversation.*       ← Conversation UI
com.quantumresearch.mycel.android.forum.*              ← Forum UI
com.quantumresearch.mycel.android.blog.*               ← Blog UI
com.quantumresearch.mycel.android.util.*               ← Android utilities
com.quantumresearch.mycel.android.viewmodel.*          ← MVVM ViewModels
```

## Class Naming Patterns

### Interface Naming Conventions

#### API Interfaces
```java
// Pattern: [Function]Manager
ContactManager
ConversationManager
ForumManager
BlogManager
PrivateGroupManager
SharingManager
IntroductionManager

// Pattern: [Component]Component
DatabaseComponent
CryptoComponent
EventComponent

// Pattern: [Entity]Factory
MessageFactory
GroupFactory
ContactFactory
```

#### Implementation Classes
```java
// Pattern: [Interface]Impl
ContactManagerImpl
ConversationManagerImpl
ForumManagerImpl
DatabaseComponentImpl
CryptoComponentImpl

// Android pattern: [Function]Activity
MainActivity
ConversationActivity
ForumActivity
SettingsActivity
AddContactActivity

// Android pattern: [Function]Fragment
ContactListFragment
NavDrawerFragment
ConversationFragment
ForumFragment

// Android pattern: [Entity]Adapter
ContactListAdapter
ConversationAdapter
ForumPostAdapter
```

### Data Model Naming

#### Core Entities
```java
// Entities use simple nouns
Contact
Message
Forum
Blog
PrivateGroup
Author
Group

// Headers for list display
ConversationMessageHeader
ForumPostHeader
BlogPostHeader
```

#### Value Objects
```java
// IDs use [Entity]Id pattern
ContactId
MessageId
GroupId
AuthorId
TransportId

// Keys use [Type]Key pattern
PublicKey
PrivateKey
SecretKey
TransportKeys

// States use [Entity]State pattern
ContactState
MessageState
TransportState
PluginState
```

### Service and Component Naming

#### Core Services
```java
// Pattern: [Function]Service
BriarService         ← Main background service
NotificationService  ← Notification management
KeyAgreementService  ← Key exchange service

// Pattern: [Function]Controller
BriarController      ← Main application controller
LifecycleController  ← Application lifecycle
ConnectionController ← Connection management
```

#### Plugin Architecture
```java
// Plugin naming pattern
TorPlugin
BluetoothPlugin
LanTcpPlugin

// Plugin factories
TorPluginFactory
BluetoothPluginFactory
LanTcpPluginFactory

// Transport constants
TorConstants
BluetoothConstants
LanTcpConstants
```

### Test Class Naming

#### Unit Tests
```java
// Pattern: [ClassUnderTest]Test
ContactManagerImplTest
ConversationManagerImplTest
DatabaseComponentImplTest
CryptoComponentImplTest

// Integration tests
ContactIntegrationTest
MessagingIntegrationTest
SyncIntegrationTest
```

#### Mock Objects
```java
// Pattern: Mock[Interface]
MockDatabaseComponent
MockCryptoComponent
MockContactManager
MockEventBus
```

## Resource Naming Conventions

### Android Resources

#### String Resources
```xml
<!-- Pattern: [screen]_[element]_[purpose] -->
<string name="contact_list_title">Contacts</string>
<string name="conversation_send_button">Send</string>
<string name="forum_create_button">Create Forum</string>
<string name="settings_notifications_title">Notifications</string>

<!-- Error messages: error_[context]_[type] -->
<string name="error_contact_not_found">Contact not found</string>
<string name="error_network_unavailable">Network unavailable</string>

<!-- Accessibility: accessibility_[element]_[purpose] -->
<string name="accessibility_send_message">Send message</string>
<string name="accessibility_contact_verified">Contact is verified</string>
```

#### Layout Resources
```xml
<!-- Activities: activity_[name] -->
activity_main.xml
activity_conversation.xml
activity_forum.xml
activity_settings.xml

<!-- Fragments: fragment_[name] -->
fragment_contact_list.xml
fragment_nav_drawer.xml
fragment_conversation.xml

<!-- List items: list_item_[type] -->
list_item_contact.xml
list_item_conversation_msg_in.xml
list_item_conversation_msg_out.xml
list_item_forum_post.xml

<!-- Custom views: view_[name] -->
view_trust_indicator.xml
view_transport_indicator.xml
view_conversation_message.xml
```

#### Drawable Resources
```xml
<!-- Icons: ic_[name]_[variant] -->
ic_send_24dp.xml
ic_contact_24dp.xml
ic_forum_24dp.xml
ic_settings_24dp.xml

<!-- Backgrounds: bg_[purpose] -->
bg_message_bubble_incoming.xml
bg_message_bubble_outgoing.xml
bg_button_primary.xml

<!-- Shapes: shape_[description] -->
shape_circle.xml
shape_rounded_rectangle.xml
```

#### Color Resources
```xml
<!-- Brand colors: [brand]_[purpose] -->
<color name="briar_primary">#2E7D32</color>
<color name="briar_primary_dark">#1B5E20</color>
<color name="briar_accent">#4CAF50</color>

<!-- Semantic colors: [purpose]_[variant] -->
<color name="transport_tor">#7E57C2</color>
<color name="transport_bluetooth">#2196F3</color>
<color name="transport_lan">#FF9800</color>
<color name="message_bubble_incoming">#E8F5E8</color>
<color name="message_bubble_outgoing">#DCF8C6</color>
```

## Database Naming Conventions

### Table Names
```sql
-- Pattern: Use plural nouns for tables
contacts
messages
groups
messageMetadata
transportProperties
settings
offers
requests
messageDependencies
messagesToOffer
messagesToRequest
pendingContacts
contactGroups
groupVisibility
localAuthors
transports
```

### Column Names
```sql
-- Pattern: Use camelCase for multi-word columns
contactId       -- Primary/foreign keys end with 'Id'
messageId
groupId
authorId
transportId
timestamp       -- Time values use 'timestamp'
versionNumber   -- Versions use 'Number' suffix
messageState    -- States use 'State' suffix
raw            -- Binary data often called 'raw'
shared         -- Boolean flags are descriptive
visible
local
verified
```

### Index Names
```sql
-- Pattern: idx_[table]_[columns]
idx_contacts_authorId
idx_messages_groupId
idx_messages_timestamp
idx_messageMetadata_messageId
idx_transportProperties_contactId_transportId
```

## Configuration and Constants

### Configuration Keys
```java
// Pattern: UPPER_SNAKE_CASE
public static final String PREF_SCREEN_LOCK = "pref_screen_lock";
public static final String PREF_NOTIFICATIONS = "pref_notifications";
public static final String PREF_THEME = "pref_theme";

// Network constants
public static final int DEFAULT_POLLING_INTERVAL = 60000;
public static final int MAX_MESSAGE_LENGTH = 65535;
public static final long CONNECTION_TIMEOUT = 30000;
```

### Labels and Tags
```java
// Cryptographic labels use descriptive strings
public static final String LABEL_TRANSPORT_KEY = "transport_key";
public static final String LABEL_MESSAGE_ENCRYPTION = "message_encryption";
public static final String LABEL_CONTACT_EXCHANGE = "contact_exchange";

// Event tags
public static final String TAG_CONTACT_ADDED = "ContactAdded";
public static final String TAG_MESSAGE_RECEIVED = "MessageReceived";
```

## Rebranding Naming Strategy for Mycel

### Systematic Replacement Rules

#### Package Name Changes
```
Search:  org\.briarproject\.bramble
Replace: com.quantumresearch.mycel.infrastructure

Search:  org\.briarproject\.briar
Replace: com.quantumresearch.mycel.app

Search:  org\.briarproject\.briar\.android
Replace: com.quantumresearch.mycel.android
```

#### String Resource Updates
```xml
<!-- OLD (Briar) -->
<string name="app_name">Briar</string>
<string name="app_description">Secure messaging app</string>
<string name="about_briar">About Briar</string>

<!-- NEW (Mycel) -->
<string name="app_name">Mycel</string>
<string name="app_description">Secure messaging app</string>
<string name="about_mycel">About Mycel</string>
```

#### Configuration Updates
```java
// OLD constants that may contain briar references
BRIAR_VERSION_HEADER = "Briar-Version"
DEFAULT_USER_AGENT = "Briar/1.5"

// NEW constants for Mycel
MYCEL_VERSION_HEADER = "Mycel-Version"
DEFAULT_USER_AGENT = "Mycel/1.0"
```

#### File and Directory Naming
```
// Asset files
briar_logo.svg → mycel_logo.svg
briar_icon.png → mycel_icon.png

// Documentation
briar_manual.pdf → mycel_manual.pdf
briar_faq.md → mycel_faq.md
```

### Naming Consistency Rules

1. **Package Names**: Always use lowercase with dots
2. **Class Names**: Use PascalCase (UpperCamelCase)
3. **Method Names**: Use camelCase (lowerCamelCase)
4. **Constants**: Use UPPER_SNAKE_CASE
5. **Resources**: Use lowercase with underscores
6. **Database**: Use camelCase for columns, lowercase for tables

### Brand-Specific Conventions for Mycel

#### Application Identifiers
```gradle
// Application ID
applicationId "com.quantumresearch.mycel"

// Package declarations
package com.quantumresearch.mycel.android;
package com.quantumresearch.mycel.app.messaging;
package com.quantumresearch.mycel.infrastructure.crypto;
```

#### Brand References in Code
```java
// Class and interface names remain functional, not brand-specific
public class ConversationManager { }     // GOOD - describes function
public class MycelManager { }            // AVOID - brand in class name

// Configuration and user-facing strings should reference Mycel
USER_AGENT = "Mycel/1.0"                // GOOD - user-facing
APP_NAME = "Mycel"                      // GOOD - user-facing
```

This naming convention strategy ensures consistent, maintainable code throughout the rebranding process while preserving the functional clarity of the existing architecture.