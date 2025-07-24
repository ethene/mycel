# Mycel

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop%20%7C%20Headless-green.svg)](#platform-support)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Security](https://img.shields.io/badge/Security-End--to--End%20Encrypted-red.svg)](#security)
[![Decentralized](https://img.shields.io/badge/Network-Decentralized-purple.svg)](#features)

**Mycel** is a secure, decentralized messaging application developed by **Quantum Research Pty Ltd** that enables peer-to-peer communication without relying on central servers. Built for privacy, resilience, and true digital sovereignty.

> 🔒 **Privacy First**: No central servers, no tracking, no data collection  
> 🌐 **Always Connected**: Works via Tor, Bluetooth, Wi-Fi, even when the internet is down  
> 🛡️ **Military-Grade Security**: End-to-end encryption for all communications  

---

## Table of Contents

- [Features](#features)
- [Platform Support](#platform-support)
- [Installation](#installation)
- [Usage](#usage)
- [Security](#security)
- [Architecture](#architecture)
- [Building from Source](#building-from-source)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [Troubleshooting](#troubleshooting)
- [Community](#community)
- [License](#license)

---

## Features

### 🔐 **Uncompromising Privacy**
- **Zero central servers** - Messages sync directly between devices
- **No user registration** - No email, phone number, or personal data required
- **Anonymous by design** - No tracking, analytics, or data collection
- **Tor integration** - Route traffic through the Tor network for maximum anonymity

### 🌍 **Resilient Communication**
- **Multi-transport sync** - Tor, Bluetooth, Wi-Fi connectivity
- **Offline messaging** - Continue conversations without internet access
- **Crisis communication** - Bluetooth mesh networking for emergency scenarios
- **Censorship resistant** - Works even when internet is restricted or monitored

### 🛡️ **Advanced Security**
- **End-to-end encryption** - All messages, files, and metadata encrypted
- **Perfect forward secrecy** - Past communications remain secure if keys are compromised
- **Authenticated encryption** - Cryptographic proof of message authenticity
- **Secure key exchange** - Safe contact addition via QR codes or invitation links

### 💬 **Rich Messaging**
- **Private messaging** - Secure 1-on-1 conversations
- **Group messaging** - Private group conversations with friends
- **Public forums** - Community discussions with topic-based channels
- **Blog posts** - Share thoughts and updates with your network
- **File sharing** - Send images, documents, and files securely

### 🔧 **Developer Friendly**
- **Headless API** - REST API for automation and integration
- **Cross-platform** - Android, Desktop (Linux/macOS/Windows), and Headless
- **Open source** - GPL v3 licensed, fully auditable code
- **Modular architecture** - Clean separation between transport and application layers

---

## Platform Support

| Platform | Status | Download | Requirements |
|----------|--------|----------|--------------|
| **Android** | ✅ Stable | [Download APK](#installation) | Android 5.0+ (API 21+) |
| **Desktop** | ✅ Stable | [Download JAR](#installation) | Java 17+, Linux/macOS/Windows |
| **Headless** | ✅ Stable | [Build from Source](#building-from-source) | Java 17+, Any OS |
| **iOS** | 🔄 Planned | - | Future release |

---

## Installation

### 📱 **Android**

#### Option 1: Direct APK Download (Recommended)
```bash
# Download latest release APK
curl -L -o mycel.apk https://github.com/quantumresearch/mycel/releases/latest/download/mycel-android.apk

# Install APK (requires adb or manual installation)
adb install mycel.apk
```

#### Option 2: Build from Source
See [Building from Source](#building-from-source) section below.

### 🖥️ **Desktop**

#### Option 1: Download JAR
```bash
# Download headless JAR
curl -L -o mycel.jar https://github.com/quantumresearch/mycel/releases/latest/download/mycel-headless.jar

# Run Mycel
java -jar mycel.jar
```

#### Option 2: Build from Source
See [Building from Source](#building-from-source) section below.

---

## Usage

### 🚀 **Getting Started**

1. **Install Mycel** on your device using the instructions above
2. **Create your identity** - Choose a username (no registration required)
3. **Add contacts** via QR code scanning or invitation links  
4. **Start messaging** - Send encrypted messages instantly

### 👥 **Adding Contacts**

#### Method 1: QR Code (Recommended)
1. Open Mycel on both devices
2. Go to **Contacts** → **Add Contact** → **Nearby**
3. One person shows QR code, other person scans it
4. Both devices will exchange keys and add each other

#### Method 2: Invitation Links
1. Go to **Contacts** → **Add Contact** → **At Distance**  
2. Share the generated `mycel://` link via any secure channel
3. Recipient opens the link in Mycel to add you as a contact

### 💬 **Messaging**

- **Private Messages**: Tap any contact to start a private conversation
- **Groups**: Create private groups with multiple contacts
- **Forums**: Join or create public discussion forums
- **Blogs**: Share posts with your network

### 🔧 **Advanced Usage**

#### Headless Mode (API Server)
```bash
# Start Mycel as REST API server
java -jar mycel-headless.jar --port 8080

# API will be available at http://localhost:8080
curl http://localhost:8080/v1/contacts
```

#### Configuration
- **Settings** → **Network** - Configure Tor, Bluetooth, Wi-Fi
- **Settings** → **Security** - Set up screen lock, backup options
- **Settings** → **Privacy** - Configure data retention policies

---

## Security

### 🔒 **Encryption Details**

- **Message Encryption**: AES-256 in GCM mode
- **Key Exchange**: Curve25519 elliptic curve Diffie-Hellman
- **Message Authentication**: Poly1305 MAC
- **Hash Functions**: SHA-256, BLAKE2b
- **Random Number Generation**: Cryptographically secure RNG

### 🛡️ **Security Model**

- **Threat Model**: Protects against mass surveillance, censorship, and targeted attacks
- **Perfect Forward Secrecy**: New keys for each conversation, old messages remain secure
- **Metadata Protection**: Connection patterns obscured through Tor and transport diversity
- **Secure Contact Addition**: Cryptographic verification prevents impersonation attacks

### 🔍 **Security Audits**

- **Code Transparency**: All cryptographic code is open source and auditable
- **Peer Review**: Based on proven cryptographic protocols and libraries
- **Reproducible Builds**: Verify that releases match the source code

> ⚠️ **Security Notice**: While Mycel implements strong cryptographic protection, no software is 100% secure. Use additional operational security measures for high-risk communications.

---

## Architecture

### 🏗️ **System Design**

Mycel uses a **two-layer architecture** that separates transport concerns from application logic:

```
┌─────────────────────────────────────────┐
│           Mycel Layer                   │
│     (Application Logic)                 │
│                                         │
│  • Private messaging    • Groups        │
│  • Forums              • Blogs         │
│  • File sharing        • UI/UX         │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│           Spore Layer                   │
│        (Infrastructure)                 │
│                                         │
│  • Transport plugins   • Cryptography   │
│  • Sync protocol      • Database       │
│  • Network stack      • Key management │
└─────────────────────────────────────────┘
```

### 📦 **Module Structure**

#### **Spore Layer** (Infrastructure)
- `spore-api`: Core transport and sync APIs
- `spore-core`: Networking, cryptography, and sync implementation
- `spore-android`: Android-specific transport implementations  
- `spore-java`: Desktop and headless transport implementations

#### **Mycel Layer** (Application)
- `mycel-api`: High-level messaging APIs
- `mycel-core`: Application features (messaging, forums, blogs)
- `mycel-android`: Android UI and platform integration
- `mycel-headless`: REST API for headless operation

### 🔌 **Transport Plugins**

- **Tor Plugin**: Routes traffic through Tor network for anonymity
- **Bluetooth Plugin**: Direct device-to-device communication  
- **Wi-Fi Plugin**: Local network communication
- **TCP Plugin**: Direct internet connections (when appropriate)

---

## Building from Source

### 📋 **Prerequisites**

- **Java 17+** ([OpenJDK](https://openjdk.org/) recommended)
- **Android SDK** (for Android builds)
- **Git** for cloning the repository

### 🛠️ **Build Commands**

```bash
# Clone repository
git clone https://github.com/quantumresearch/mycel.git
cd mycel

# Build all modules
make build

# Build Android APK
make android-debug

# Build headless JAR  
make headless-build

# Run tests
make test

# Run headless version
make headless
```

### 🔧 **Alternative: Gradle Commands**

```bash
# Set Java 17 (required)
export JAVA_HOME=$(/usr/libexec/java_home -v17)

# Build all modules
./gradlew build

# Build Android APK
./gradlew :mycel-android:assembleDebug

# Run tests
./gradlew test

# Run headless version
./gradlew :mycel-headless:run
```

### 📁 **Build Outputs**

- **Android APK**: `mycel-android/build/outputs/apk/debug/mycel-android-debug.apk`
- **Headless JAR**: `mycel-headless/build/libs/mycel-headless-fat.jar`

---

## API Documentation

The **Mycel Headless** component provides a REST API for programmatic access:

### 🔗 **Base URL**
```
http://localhost:7000/v1/
```

### 📡 **Core Endpoints**

#### Contacts
```bash
# List all contacts
GET /v1/contacts

# Get contact details  
GET /v1/contacts/{contactId}

# Add pending contact
POST /v1/contacts/add/pending
{
  "link": "mycel://...",
  "alias": "Friend Name"  
}

# Remove contact
DELETE /v1/contacts/{contactId}
```

#### Messages
```bash
# List conversations
GET /v1/conversations

# Get conversation messages
GET /v1/conversations/{conversationId}/messages  

# Send message
POST /v1/conversations/{conversationId}/messages
{
  "text": "Hello, world!"
}
```

### 📖 **Full API Documentation**

See [mycel-headless/README.md](mycel-headless/README.md) for complete API documentation with examples.

---

## Contributing

We welcome contributions from the community! 🎉

### 🤝 **How to Contribute**

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### 📋 **Contribution Guidelines**

- Follow the [CONTRIBUTING.md](CONTRIBUTING.md) guidelines
- Ensure all tests pass before submitting
- Add tests for new features
- Update documentation as needed
- Follow the existing code style

### 🐛 **Reporting Issues**

Found a bug? Have a feature request? Please [open an issue](https://github.com/quantumresearch/mycel/issues) with:

- Clear description of the problem/request
- Steps to reproduce (for bugs)
- Expected vs actual behavior
- System information (OS, Java version, etc.)

---

## Troubleshooting

### ❓ **Common Issues**

#### **Build Failures**

**Problem**: `JAVA_HOME` not set correctly
```bash
# Solution: Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v17)
```

**Problem**: Android SDK not found
```bash
# Solution: Set ANDROID_HOME
export ANDROID_HOME=/path/to/android-sdk
```

#### **Runtime Issues**

**Problem**: Mycel won't connect to contacts
- **Check**: Network connectivity (Tor, Wi-Fi, Bluetooth)
- **Try**: Toggle transport settings in **Settings** → **Network**
- **Verify**: Firewall isn't blocking connections

**Problem**: Messages not syncing
- **Wait**: Sync can take time, especially over Tor
- **Check**: Both devices are online with same transport
- **Restart**: Try restarting the app

#### **Android Installation**

**Problem**: "App not installed" error
- **Enable**: Settings → Security → Unknown Sources
- **Check**: Sufficient storage space
- **Try**: Uninstall previous version first

### 📚 **Getting Help**

- **Documentation**: Check the [docs/](docs/) folder
- **Issues**: Search [existing issues](https://github.com/quantumresearch/mycel/issues)
- **Community**: Join our discussions (see [Community](#community))

---

## Community

### 💬 **Connect with Us**

- **Website**: [https://qntrs.com](https://qntrs.com)
- **GitHub**: [https://github.com/quantumresearch/mycel](https://github.com/quantumresearch/mycel)
- **Issues**: [Bug Reports & Feature Requests](https://github.com/quantumresearch/mycel/issues)

### 🤝 **Support the Project**

- ⭐ **Star** the repository if you find Mycel useful
- 🐛 **Report bugs** and help improve the software
- 💻 **Contribute code** to make Mycel better
- 📢 **Spread the word** about decentralized messaging

---

## Roadmap

### 🚀 **Current Status**: Stable Release

- ✅ **Phase 1**: Core messaging functionality
- ✅ **Phase 2**: Multi-transport sync (Tor, Bluetooth, Wi-Fi)  
- ✅ **Phase 3**: Groups and forums
- ✅ **Phase 4**: File sharing and media support
- ✅ **Phase 5**: Headless API and automation

### 🔮 **Future Plans**

- 🔄 **iOS Support**: Native iOS application
- 🔄 **Voice Messages**: Encrypted voice note support
- 🔄 **Video Calls**: Peer-to-peer encrypted video calling
- 🔄 **Desktop GUI**: Native desktop applications with GUI
- 🔄 **Plugin System**: Third-party transport and feature plugins

---

## License

This project is licensed under the **GNU General Public License v3.0**.

**You are free to:**
- ✅ Use this software for any purpose
- ✅ Study and modify the source code  
- ✅ Distribute copies of the software
- ✅ Distribute modified versions

**Under the conditions:**
- 📋 Include the same license in derivative works
- 📋 State changes made to the code
- 📋 Include copyright and license notices
- 📋 Make source code available for distributed software

See [LICENSE.txt](LICENSE.txt) for full license text.

---

## About Quantum Research

**Mycel** is developed by **[Quantum Research Pty Ltd](https://qntrs.com)**, a technology company focused on privacy, security, and decentralized communication systems.

Our mission is to build tools that protect digital privacy and enable secure communication for everyone, everywhere.

---

<div align="center">

**⭐ Star this repository if you find Mycel useful!**

**Built with ❤️ for digital privacy and freedom**

</div>