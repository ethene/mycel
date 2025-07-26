# Project Overview

## What is Mycel?

Mycel is a decentralized messaging application designed for secure, peer-to-peer communication without relying on central servers. Built by Quantum Research Pty Ltd, it's designed for activists, journalists, and anyone who needs robust, private communication.

**Original Project**: This is a rebranding of the Briar messaging application, maintaining all core functionality while updating the brand identity to Mycel.

## Core Principles

### 1. Decentralization
- No central servers required
- Direct device-to-device communication
- Resilient to network disruption and censorship

### 2. Multi-Transport Communication
- **Tor Network**: Anonymous internet communication
- **Bluetooth**: Local peer-to-peer connectivity
- **Wi-Fi Direct**: Local network communication
- Automatic transport selection and failover

### 3. Security & Privacy
- End-to-end encryption for all communications
- Perfect forward secrecy
- Metadata protection
- Anonymous routing through Tor
- Encrypted local database storage

### 4. Offline Capability
- Messages sync when connections are available
- Works in internet blackouts via Bluetooth/WiFi
- Store-and-forward messaging
- Mailbox system for offline message delivery

## Key Features

### Messaging
- **Private Messages**: One-on-one encrypted conversations
- **Group Chats**: Private group conversations
- **Forums**: Public discussion spaces
- **Blogs**: Publishing and RSS-style feeds
- **File Sharing**: Secure attachment handling

### Networking
- **Transport Abstraction**: Pluggable transport system
- **Automatic Discovery**: Find nearby contacts automatically
- **Contact Introduction**: Secure contact sharing
- **Mailbox Support**: Server-assisted message delivery

### Security
- **Identity Management**: Cryptographic identities
- **Key Management**: Automated key exchange and rotation
- **Database Encryption**: AES-256 encrypted local storage
- **Secure Authentication**: Password-based key derivation

## Technical Architecture

### Layered Design
```
┌─────────────────────────────────────┐
│           Android UI                │  ← mycel-android
├─────────────────────────────────────┤
│        Application Logic            │  ← mycel-core/api
├─────────────────────────────────────┤
│      Infrastructure Layer          │  ← spore-core/api
├─────────────────────────────────────┤
│       Transport Plugins             │  ← spore-android/java
└─────────────────────────────────────┘
```

### Module Organization
- **Spore**: Low-level infrastructure (networking, crypto, database)
- **Mycel**: High-level application features (messaging, forums, blogs)
- **Platform**: Platform-specific implementations (Android, Java/Desktop)

## Use Cases

### Primary Use Cases
1. **Activists**: Secure communication under surveillance
2. **Journalists**: Source protection and secure reporting
3. **Emergency Communication**: During network outages
4. **Privacy-Conscious Users**: General secure messaging

### Deployment Scenarios
1. **Mobile App**: Primary Android application
2. **Headless Server**: REST API for automation/bots
3. **Desktop Client**: Java-based desktop application
4. **Embedded Systems**: Lightweight messaging nodes

## Project Goals

### Functional Goals
- Provide secure, decentralized messaging
- Support multiple communication transports
- Maintain usability without sacrificing security
- Enable offline and censorship-resistant communication

### Technical Goals
- Modular, testable architecture
- Cross-platform compatibility
- Extensible plugin system
- Robust security implementation

### Mycel Rebranding Strategy
This architecture makes rebranding to Mycel feasible by:
- Clear separation of UI from core logic
- Modular design allowing selective modification
- Well-defined interfaces between components
- Comprehensive configuration system

## Mycel Brand Implementation

### Brand Identity
- **Name**: Mycel
- **Developer**: Quantum Research Pty Ltd
- **Package Structure**: `com.quantumresearch.mycel.*`
- **Domain**: (to be determined by Quantum Research)

### Next Steps for Mycel Implementation

1. **Update Package Names**: Complete transition to `com.quantumresearch.mycel.*`
2. **Rebrand Visual Identity**: Create Mycel logos, icons, and color schemes
3. **Update App Store Presence**: New Quantum Research developer accounts
4. **Maintain Protocol Compatibility**: Ensure messaging still works during transition
5. **Comprehensive Testing**: Validate all Mycel features work correctly