# Messaging Workflow Analysis

## Overview

This document details how messaging works in Briar from a technical perspective, tracing the complete flow from when a user sends a message to when another user receives it.

## Core Messaging Architecture

### Message Flow Overview

```
User A                    Bramble Layer                 Transport Layer                 User B
┌─────┐    ┌─────────────────────────────────────────────────────────────────────┐    ┌─────┐
│ UI  │───→│ Briar Message API │ Bramble Sync │ Transport Plugin │ Network │   │←───│ UI  │
└─────┘    └─────────────────────────────────────────────────────────────────────┘    └─────┘
```

## Detailed Message Workflow

### 1. Message Creation (briar-android)

**Location**: `briar-android/src/main/java/org/briarproject/briar/android/conversation/`

**Files**:
- `ConversationActivity.java` - Main chat interface
- `ConversationController.java` - Message handling logic
- `ConversationViewModel.java` - UI state management

**Process**:
1. User types message in `ConversationActivity`
2. `ConversationController.sendMessage()` called
3. Message validated and packaged
4. Passed to Briar messaging layer

### 2. Briar Message Processing (briar-core)

**Location**: `briar-core/src/main/java/org/briarproject/briar/messaging/`

**Key Classes**:
- `MessagingManager` - High-level messaging operations
- `ConversationManager` - Conversation management
- `PrivateMessageFactory` - Message object creation

**Process**:
1. `MessagingManager.sendPrivateMessage()` called
2. Message encrypted and signed
3. Metadata added (timestamp, message ID)
4. Passed to Bramble sync layer

### 3. Bramble Sync Protocol (bramble-core)

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/sync/`

**Key Classes**:
- `SyncSession` - Manages sync between contacts
- `MessageFactory` - Creates protocol messages
- `ValidationManager` - Validates incoming messages

**Process**:
1. Message added to local database
2. Sync session initiated with target contact
3. Message queued for transmission
4. Transport layer notified

### 4. Transport Layer Selection (bramble-core)

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/plugin/`

**Key Classes**:
- `PluginManager` - Manages available transports
- `TransportPropertyManager` - Transport configuration
- `ConnectionManager` - Connection establishment

**Available Transports**:
1. **Tor Plugin** (`TorPlugin`)
   - Anonymous internet communication
   - Routes through Tor network
   - Primary transport for privacy

2. **LAN Plugin** (`LanTcpPlugin`) 
   - Local network communication
   - Direct TCP connections
   - Fast local messaging

3. **Bluetooth Plugin** (`BluetoothPlugin`)
   - Device-to-device communication
   - Works without internet
   - Emergency/offline scenarios

### 5. Network Transmission

**Transport-Specific Handling**:

#### Tor Transport (bramble-android)
**Location**: `bramble-android/src/main/java/org/briarproject/bramble/plugin/tor/`

**Process**:
1. Establish Tor connection
2. Create hidden service or connect to contact's service
3. Encrypt message with transport keys
4. Transmit over Tor network

#### Bluetooth Transport (bramble-android)
**Location**: `bramble-android/src/main/java/org/briarproject/bramble/plugin/bluetooth/`

**Process**:
1. Discover nearby Bluetooth devices
2. Establish secure Bluetooth connection
3. Exchange messages directly
4. No internet required

#### LAN Transport (bramble-core)
**Location**: `bramble-core/src/main/java/org/briarproject/bramble/plugin/tcp/`

**Process**:
1. Discover contacts on local network
2. Establish TCP connection
3. Fast local message exchange

### 6. Message Reception

**Reverse Process**:
1. Transport receives encrypted message
2. Bramble sync validates and decrypts
3. Briar messaging processes message
4. UI notified and updated

## Database Storage

### Message Storage Schema

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/db/`

**Key Tables**:
- `messages` - Message content and metadata
- `message_metadata` - Additional message properties
- `contacts` - Contact information and keys
- `transport_properties` - Transport configuration

**Process**:
1. Messages stored encrypted in local database
2. H2 database with AES encryption
3. Forward secrecy through key rotation
4. Automatic cleanup of old messages

## Security Implementation

### Encryption Layers

1. **Transport Encryption**
   - Each transport has own encryption
   - Tor: Onion routing encryption
   - Bluetooth: Bluetooth encryption + custom layer
   - LAN: TLS-style encryption

2. **Message Encryption**
   - End-to-end encryption with contact keys
   - Perfect forward secrecy
   - Message authentication

3. **Database Encryption**
   - Local database encrypted with user password
   - Key derivation using Scrypt
   - Encrypted metadata protection

### Key Management

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/crypto/`

**Key Classes**:
- `CryptoComponent` - Main crypto interface
- `KeyManager` - Key generation and storage
- `PasswordBasedEncryption` - Password-based crypto

**Process**:
1. Contact keys established during contact addition
2. Transport keys generated per session
3. Message keys derived from contact keys
4. Automatic key rotation for forward secrecy

## Event System Integration

### Message Events

**Location**: `bramble-api/src/main/java/org/briarproject/bramble/api/event/`

**Key Events**:
- `MessageAddedEvent` - New message received
- `MessageStateChangedEvent` - Message status updated
- `ContactRemovedEvent` - Contact deleted
- `TransportPropertyEvent` - Transport status changed

**Event Flow**:
1. Message events fired when messages sent/received
2. UI components register as event listeners
3. Automatic UI updates when new messages arrive
4. Background sync notifications

## Configuration and Settings

### Message Settings

**Location**: `briar-android/src/main/java/org/briarproject/briar/android/settings/`

**Configurable Options**:
- Message retention policy
- Transport preferences
- Notification settings
- Encryption preferences

### Transport Configuration

**Key Files**:
- `TransportPropertyManager` - Transport settings
- `PluginConfig` - Plugin-specific configuration
- Android shared preferences for user settings

## Offline Message Handling

### Store and Forward

**Mechanism**:
1. Messages stored locally when contact offline
2. Automatic sync when contact comes online
3. Multiple transport attempts
4. Mailbox system for extended offline periods

### Mailbox Integration

**Location**: `briar-mailbox/` (optional module)

**Process**:
1. Messages uploaded to mailbox server when direct connection fails
2. Mailbox provides store-and-forward capability
3. End-to-end encryption maintained
4. Mailbox cannot read message content

## Error Handling and Reliability

### Message Delivery Guarantees

1. **At-Least-Once Delivery**
   - Messages retransmitted until acknowledged
   - Duplicate detection and handling
   - Persistent storage until confirmed

2. **Ordering Guarantees** 
   - Messages delivered in order per conversation
   - Vector clocks for consistency
   - Conflict resolution for concurrent messages

3. **Failure Recovery**
   - Automatic retry with exponential backoff
   - Fallback to alternative transports
   - Persistent queue for failed messages

## Performance Considerations

### Optimization Strategies

1. **Message Batching**
   - Multiple messages sent in single sync session
   - Reduces connection overhead
   - Improves battery life

2. **Selective Sync**
   - Only sync with active contacts
   - Priority-based message transmission
   - Bandwidth-aware transport selection

3. **Caching**
   - Contact keys cached for performance
   - Transport connections reused
   - Message metadata indexed for fast lookup

## Rebranding Implications

### Components Requiring Updates

1. **UI Layer** (`briar-android/`)
   - Message bubble designs
   - Conversation layouts
   - Notification templates

2. **Configuration**
   - Default transport preferences
   - Message retention policies
   - Notification settings

3. **Deep Links**
   - Message sharing URLs
   - Contact invitation links
   - App-to-app communication

4. **Testing**
   - Message flow integration tests
   - Transport-specific test cases
   - UI automation for messaging