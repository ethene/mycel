# Transport System Analysis

## Overview

Briar's transport system is a pluggable architecture that allows communication over multiple networks simultaneously. Each transport plugin handles a different communication method while presenting a unified interface to the upper layers.

## Transport Architecture

### Plugin Interface Design

**Location**: `spore-api/src/main/java/org/briarproject/bramble/api/plugin/`

**Core Interfaces**:
- `Plugin` - Base transport plugin interface
- `PluginFactory` - Creates plugin instances
- `TransportConnectionReader` - Reads incoming connections
- `TransportConnectionWriter` - Writes outgoing connections

### Transport Plugin Manager

**Location**: `spore-core/src/main/java/org/briarproject/bramble/plugin/`

**Key Classes**:
- `PluginManager` - Manages all transport plugins
- `ConnectionManager` - Handles connections across transports
- `TransportPropertyManager` - Stores transport configuration

## Individual Transport Plugins

### 1. Tor Transport Plugin

**Purpose**: Anonymous internet communication through Tor network

#### Android Implementation
**Location**: `spore-android/src/main/java/org/briarproject/bramble/plugin/tor/`

**Key Classes**:
- `AndroidTorPlugin` - Main Tor plugin for Android
- `AndroidTorPluginFactory` - Creates Tor plugin instances
- `TorConnectionManager` - Manages Tor connections

**Dependencies**:
- `org.briarproject:tor-android:0.4.8.14` - Tor binary for Android
- `org.briarproject:lyrebird-android:0.5.0-3` - Obfuscation proxy
- `org.briarproject:onionwrapper-android:0.1.3` - Tor integration library

**Configuration**:
```java
// Tor transport properties
PROP_ONION_ADDRESS = "onionAddress"
PROP_BRIDGE_TYPES = "bridgeTypes"  
PROP_CONNECTION_TIMEOUT = "connectionTimeout"
PROP_CIRCUIT_TIMEOUT = "circuitTimeout"
```

**Process Flow**:
1. Start embedded Tor process
2. Create hidden service for incoming connections
3. Discover contact onion addresses
4. Establish encrypted connections through Tor network
5. Route all traffic through 3-hop circuits

#### Desktop Implementation  
**Location**: `spore-java/src/main/java/org/briarproject/bramble/plugin/tor/`

**Differences from Android**:
- Uses system Tor or bundled Tor binary
- Different process management
- Desktop-specific Tor configuration

### 2. Bluetooth Transport Plugin

**Purpose**: Direct device-to-device communication without internet

#### Android Implementation
**Location**: `spore-android/src/main/java/org/briarproject/bramble/plugin/bluetooth/`

**Key Classes**:
- `AndroidBluetoothPlugin` - Main Bluetooth plugin
- `BluetoothConnectionLimiter` - Manages connection limits
- `BluetoothTransportConnection` - Individual Bluetooth connections

**Features**:
- Device discovery using Bluetooth scanning
- Secure connection establishment
- Works without internet connectivity
- Automatic reconnection on connection drop

**Configuration**:
```java
// Bluetooth transport properties  
PROP_ADDRESS = "address"
PROP_UUID = "uuid"
PROP_DISCOVERY_MODE = "discoveryMode"
PROP_CONNECTION_TIMEOUT = "connectionTimeout"
```

**Process Flow**:
1. Enable Bluetooth advertising
2. Scan for nearby Briar devices
3. Establish RFCOMM connections
4. Exchange cryptographic handshake
5. Begin message synchronization

#### Desktop Implementation
**Location**: `spore-java/src/main/java/org/briarproject/bramble/plugin/bluetooth/`

**Dependencies**:
- BlueCove Bluetooth stack (custom build)
- Native Bluetooth libraries via JNA

**Platform Support**:
- Linux BlueZ stack
- Windows Bluetooth API
- macOS Core Bluetooth (limited)

### 3. LAN TCP Transport Plugin

**Purpose**: Fast local network communication

**Location**: `spore-core/src/main/java/org/briarproject/bramble/plugin/tcp/`

**Key Classes**:
- `LanTcpPlugin` - Main LAN plugin
- `TcpTransportConnection` - TCP connection wrapper
- `PortMapper` - UPnP port mapping for NAT traversal

**Features**:
- Automatic local network discovery
- UPnP for NAT traversal
- High-speed local communication
- Works on Wi-Fi and wired networks

**Configuration**:
```java
// LAN transport properties
PROP_IP_PORTS = "ipPorts"
PROP_PORT = "port"  
PROP_DISCOVERY_MODE = "discoveryMode"
PROP_LAN_ADDRESS = "lanAddress"
```

**Process Flow**:
1. Bind to local network interfaces
2. Advertise availability on local network
3. Discover other Briar instances via multicast
4. Establish direct TCP connections
5. Optionally use UPnP for port forwarding

### 4. Modem Transport Plugin (Desktop Only)

**Purpose**: Communication over dial-up modems and serial connections

**Location**: `spore-java/src/main/java/org/briarproject/bramble/plugin/modem/`

**Key Classes**:
- `ModemPlugin` - Main modem plugin
- `SerialPortList` - Enumerates available serial ports
- `ModemConnection` - Modem connection management

**Dependencies**:
- JSSC (Java Simple Serial Connector) - custom build
- Native serial port access

**Use Cases**:
- Emergency communication when internet is down
- Remote areas with only phone line access
- Air-gapped network scenarios

## Transport Priority and Selection

### Transport Priority System

**Location**: `spore-core/src/main/java/org/briarproject/bramble/plugin/`

**Priority Order** (configurable):
1. **LAN TCP** - Fastest, local network
2. **Bluetooth** - Direct, no internet required  
3. **Tor** - Anonymous but slower
4. **Modem** - Last resort for emergencies

### Connection Strategy

**Simultaneous Connections**:
- Multiple transports can be active simultaneously
- Messages sent over first available transport
- Automatic failover to backup transports
- Load balancing for large messages

**Transport Selection Logic**:
```java
// Simplified transport selection
if (lanAvailable && localNetwork) {
    return LAN_TRANSPORT;
} else if (bluetoothEnabled && nearbyDevice) {
    return BLUETOOTH_TRANSPORT;  
} else if (internetAvailable) {
    return TOR_TRANSPORT;
} else {
    return MODEM_TRANSPORT; // if configured
}
```

## Transport Configuration Management

### Property Management

**Location**: `spore-core/src/main/java/org/briarproject/bramble/api/plugin/`

**Key Classes**:
- `TransportPropertyManager` - Manages transport settings
- `TransportId` - Unique transport identifiers
- `TransportProperties` - Transport-specific configuration

**Stored Properties**:
```java
// Per-transport configuration
Map<TransportId, TransportProperties> transportProperties;

// Example Tor properties:
TorProperties {
    onionAddress: "abc123...onion",
    bridgeTypes: ["obfs4", "meek"],
    circuitTimeout: 60000,
    connectionTimeout: 30000
}
```

### Contact Transport Discovery

**Discovery Process**:
1. Each contact has transport properties per plugin
2. Properties exchanged during contact addition
3. Periodic updates when transport config changes
4. Automatic discovery for local transports

**Storage**:
- Contact transport properties stored in encrypted database
- Synchronized across devices when contact is shared
- Versioned for consistency during updates

## Security Implementation

### Transport-Level Encryption

Each transport implements its own encryption layer:

1. **Tor Transport**:
   - Built-in Tor encryption (3 layers)
   - Additional application-layer encryption
   - Onion routing provides anonymity

2. **Bluetooth Transport**:
   - Bluetooth native encryption
   - Additional Briar handshake protocol
   - Protection against MITM attacks

3. **LAN Transport**:
   - TLS-style handshake
   - Perfect forward secrecy
   - Certificate pinning equivalent

### Authentication

**Handshake Protocol**:
1. Transport connection established
2. Cryptographic handshake using contact keys
3. Session key establishment
4. Message authentication codes (MAC)

**Key Exchange**:
- Uses contact public keys for authentication
- Ephemeral keys for each session
- Perfect forward secrecy guarantees

## Plugin Discovery and Loading

### Plugin Factory Registration

**Location**: `spore-core/src/main/java/org/briarproject/bramble/plugin/`

**Dagger Module**: `PluginModule`

```java
@Module
public class PluginModule {
    @Provides
    @Singleton
    Collection<PluginFactory> providePluginFactories(
        // Inject all available plugin factories
        AndroidTorPluginFactory torFactory,
        AndroidBluetoothPluginFactory bluetoothFactory,
        LanTcpPluginFactory lanFactory
    ) {
        return Arrays.asList(torFactory, bluetoothFactory, lanFactory);
    }
}
```

### Dynamic Plugin Loading

**Process**:
1. Plugin factories registered during dependency injection
2. Plugins instantiated based on platform capabilities
3. Plugin availability checked at runtime
4. Graceful degradation when plugins unavailable

## Transport State Management

### Plugin Lifecycle

**States**:
- `STARTING` - Plugin initializing
- `ACTIVE` - Plugin ready for connections  
- `INACTIVE` - Plugin temporarily disabled
- `STOPPING` - Plugin shutting down
- `STOPPED` - Plugin completely stopped

**State Transitions**:
```java
// State machine for plugin lifecycle
STOPPED → STARTING → ACTIVE
ACTIVE → INACTIVE → ACTIVE (temporary issues)
ACTIVE → STOPPING → STOPPED (shutdown)
```

### Connection State Tracking

**Per-Contact State**:
- Last successful connection time
- Transport-specific connection history
- Failure count and backoff timers
- Preferred transport ordering

## Error Handling and Reliability

### Transport Failures

**Failure Types**:
1. **Network failures** - Internet connection lost
2. **Authentication failures** - Invalid contact keys  
3. **Timeout failures** - Connection establishment timeout
4. **Protocol failures** - Malformed messages

**Recovery Strategies**:
- Exponential backoff for connection attempts
- Automatic fallback to alternative transports
- Persistent connection state across app restarts
- Circuit breaker pattern for repeatedly failing connections

### Monitoring and Diagnostics

**Metrics Collected**:
- Connection success/failure rates per transport
- Message transmission latency
- Data throughput per transport
- Battery usage per transport (Android)

**Logging**:
- Transport-specific debug logging
- Connection establishment traces
- Error logging with context
- Performance metrics logging

## Platform Differences

### Android-Specific Features

**Battery Optimization**:
- Background connection management
- Doze mode compatibility
- Battery usage optimization per transport

**Permissions**:
- Bluetooth permissions
- Network access permissions
- Location permissions (for Bluetooth discovery)

**Background Limitations**:
- Background service restrictions
- Foreground service for persistent connections
- Job scheduler for periodic sync

### Desktop-Specific Features

**Native Libraries**:
- Direct access to Bluetooth stack
- Serial port access for modem
- System-level network configuration

**Resource Management**:
- More relaxed background processing
- Higher bandwidth allowances
- Multiple simultaneous connections

## Rebranding Considerations

### Configuration Changes

**Transport Identifiers**:
- Transport IDs may contain branding strings
- Default port numbers may need changing
- Service discovery names and UUIDs

**Network Constants**:
```java
// May need rebranding:
BLUETOOTH_UUID = "org.briarproject.briar.BLUETOOTH"
MULTICAST_ADDRESS = "briar.local"  
DEFAULT_TOR_PORT = 9050
```

### User Interface

**Transport Status Display**:
- Transport names in UI
- Connection status indicators  
- Transport preference settings
- Diagnostic information display

**Error Messages**:
- Transport-specific error messages
- Connection troubleshooting guides
- User-facing transport names

### Testing Requirements

**Transport Testing**:
- Each transport requires integration testing
- Cross-platform compatibility testing
- Performance testing under various conditions
- Security testing for each transport layer