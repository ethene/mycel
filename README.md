# Mycel

[![CI/CD Pipeline](https://github.com/ethene/mycel/actions/workflows/ci.yml/badge.svg)](https://github.com/ethene/mycel/actions/workflows/ci.yml)
[![Release Pipeline](https://github.com/ethene/mycel/actions/workflows/release.yml/badge.svg)](https://github.com/ethene/mycel/actions/workflows/release.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub release](https://img.shields.io/github/release/ethene/mycel.svg)](https://github.com/ethene/mycel/releases/latest)
[![GitHub downloads](https://img.shields.io/github/downloads/ethene/mycel/total.svg)](https://github.com/ethene/mycel/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop%20%7C%20Headless-green.svg)](#platform-support)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Android API](https://img.shields.io/badge/Android-API%2021+-brightgreen.svg)](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)
[![Security](https://img.shields.io/badge/Security-End--to--End%20Encrypted-red.svg)](#security)
[![Decentralized](https://img.shields.io/badge/Network-Decentralized-purple.svg)](#features)
[![Privacy](https://img.shields.io/badge/Privacy-No%20Servers-ff69b4.svg)](#features)
[![Tor](https://img.shields.io/badge/Tor-Integrated-9932cc.svg)](#features)
[![Open Source](https://img.shields.io/badge/Open%20Source-❤️-brightgreen.svg)](https://github.com/ethene/mycel)
[![GitHub issues](https://img.shields.io/github/issues/ethene/mycel.svg)](https://github.com/ethene/mycel/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/ethene/mycel.svg)](https://github.com/ethene/mycel/pulls)
[![GitHub last commit](https://img.shields.io/github/last-commit/ethene/mycel.svg)](https://github.com/ethene/mycel/commits/main)
[![GitHub repo size](https://img.shields.io/github/repo-size/ethene/mycel.svg)](https://github.com/ethene/mycel)
[![Lines of code](https://img.shields.io/tokei/lines/github/ethene/mycel)](https://github.com/ethene/mycel)
[![CodeQL](https://github.com/ethene/mycel/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/ethene/mycel/actions/workflows/codeql-analysis.yml)
[![Maintainability](https://img.shields.io/badge/Maintainability-A-brightgreen.svg)](#architecture)
[![Documentation](https://img.shields.io/badge/Documentation-Extensive-blue.svg)](docs/)
[![API](https://img.shields.io/badge/API-RESTful-orange.svg)](#api-documentation)

**Mycel** is a secure, decentralized messaging application maintained by **Quantum Research Pty Ltd** that enables peer-to-peer communication without relying on central servers. Built for privacy, resilience, and true digital sovereignty.

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
- [Acknowledgments](#acknowledgments)

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
| **Android** | ✅ Stable | [📱 Download Page](https://ethene.github.io/mycel/) \| [Direct APK](https://github.com/ethene/mycel/releases/latest) | Android 5.0+ (API 21+) |
| **Desktop** | ✅ Stable | [🖥️ Download Page](https://ethene.github.io/mycel/) \| [Direct JAR](https://github.com/ethene/mycel/releases/latest) | Java 17+, Linux/macOS/Windows |
| **Headless** | ✅ Stable | [🖥️ Download Page](https://ethene.github.io/mycel/) \| [Build from Source](#building-from-source) | Java 17+, Any OS |

---

## Installation

### 🌐 **Easy Installation (Recommended)**

Visit our **[📥 Download Page](https://ethene.github.io/mycel/)** for the latest releases with installation instructions, checksums, and system requirements.

### 📱 **Android**

#### Option 1: Download Page (Recommended)
1. Visit **[📱 Mycel Download Page](https://ethene.github.io/mycel/)**
2. Click **"Download Android APK"** for the latest version
3. Enable "Install from unknown sources" in Android settings
4. Install the downloaded APK file

#### Option 2: Direct Download via Command Line
```bash
# Download latest release APK (replace VERSION with actual version)
curl -L -o mycel-android.apk "https://github.com/ethene/mycel/releases/latest/download/mycel-android-v$(curl -s https://api.github.com/repos/ethene/mycel/releases/latest | grep -Po '"tag_name": "\K.*?(?=")' | sed 's/v//').apk"

# Install APK (requires adb or manual installation)
adb install mycel-android.apk
```

#### Option 3: Build from Source
See [Building from Source](#building-from-source) section below.

### 🖥️ **Desktop / Headless**

#### Option 1: Download Page (Recommended)
1. Visit **[🖥️ Mycel Download Page](https://ethene.github.io/mycel/)**
2. Click **"Download Server JAR"** for the latest version
3. Run with: `java -jar mycel-headless-vX.Y.Z.jar`

#### Option 2: Direct Download via Command Line
```bash
# Download latest headless JAR (replace VERSION with actual version)
curl -L -o mycel-headless.jar "https://github.com/ethene/mycel/releases/latest/download/mycel-headless-v$(curl -s https://api.github.com/repos/ethene/mycel/releases/latest | grep -Po '"tag_name": "\K.*?(?=")' | sed 's/v//').jar"

# Run Mycel
java -jar mycel-headless.jar
```

#### Option 3: Build from Source
See [Building from Source](#building-from-source) section below.

### 🔒 **Verify Downloads (Recommended)**

Always verify the integrity of downloaded files using the provided checksums:

```bash
# Download checksum file for verification
curl -L -o mycel-android.apk.sha256 "https://github.com/ethene/mycel/releases/latest/download/mycel-android-v$(curl -s https://api.github.com/repos/ethene/mycel/releases/latest | grep -Po '"tag_name": "\K.*?(?=")' | sed 's/v//').apk.sha256"

# Verify APK integrity
sha256sum -c mycel-android.apk.sha256

# For JAR files
curl -L -o mycel-headless.jar.sha256 "https://github.com/ethene/mycel/releases/latest/download/mycel-headless-v$(curl -s https://api.github.com/repos/ethene/mycel/releases/latest | grep -Po '"tag_name": "\K.*?(?=")' | sed 's/v//').jar.sha256"
sha256sum -c mycel-headless.jar.sha256
```

> **Security Best Practice**: Always verify checksums before installing any downloaded software to ensure it hasn't been tampered with.

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

> **Security Note**: Only share invitation links through trusted, secure channels as they contain cryptographic material for contact addition.

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
git clone https://github.com/ethene/mycel.git
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

- **Android Debug APK**: `mycel-android/build/outputs/apk/official/debug/mycel-android-official-debug.apk`
- **Android Release APK**: `mycel-android/build/outputs/apk/official/release/mycel-android-official-release-unsigned.apk`
- **Headless JAR**: `mycel-headless/build/libs/mycel-headless-fat.jar`

### 🔍 **Verify Build Integrity**

After building, verify checksums match official releases:
```bash
# Generate checksums for your builds
sha256sum mycel-android/build/outputs/apk/official/release/mycel-android-official-release-unsigned.apk
sha256sum mycel-headless/build/libs/mycel-headless-fat.jar

# Compare with official checksums from GitHub releases
```

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

Found a bug? Have a feature request? Please [open an issue](https://github.com/ethene/mycel/issues) with:

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
- **Issues**: Search [existing issues](https://github.com/ethene/mycel/issues)
- **Community**: Join our discussions (see [Community](#community))

---

## Community

### 💬 **Connect with Us**

- **Website**: [https://qntrs.com](https://qntrs.com)
- **GitHub**: [https://github.com/ethene/mycel](https://github.com/ethene/mycel)
- **Issues**: [Bug Reports & Feature Requests](https://github.com/ethene/mycel/issues)

### 🤝 **Support the Project**

- ⭐ **Star** the repository if you find Mycel useful
- 🐛 **Report bugs** and help improve the software
- 💻 **Contribute code** to make Mycel better
- 📢 **Spread the word** about decentralized messaging

---

## Roadmap

### 📋 **Status**: Under Review

> **Note**: The project roadmap is currently being updated to reflect our latest priorities and technical direction. A comprehensive roadmap will be published soon with detailed timelines and feature plans.

### ✅ **Current Stable Features**

- **Core messaging functionality** - Private messages, groups, forums
- **Multi-transport sync** - Tor, Bluetooth, Wi-Fi connectivity
- **File sharing** - Secure attachment support
- **Headless API** - REST API for automation and integration
- **Cross-platform support** - Android, Desktop, Headless modes
- **End-to-end encryption** - All communications secured

### 🔮 **Areas Under Consideration**

The following features are being evaluated for inclusion in future releases:

- **Enhanced privacy features** - Additional anonymity protections
- **Performance optimizations** - Faster sync and reduced resource usage  
- **User experience improvements** - Streamlined interface and workflows
- **Developer tools** - Enhanced API capabilities and documentation
- **Platform expansion** - Additional platform support options

> **Stay Updated**: Watch this repository and check our [releases page](https://github.com/ethene/mycel/releases) for the latest developments.

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

## Acknowledgments

**Mycel** is based on [**Briar**](https://briarproject.org/), an open-source secure messaging application developed by the Briar Project team. We extend our gratitude to the original developers and contributors who created the foundational architecture and protocols that make Mycel possible.

### Original Briar Project
- **Website**: [https://briarproject.org/](https://briarproject.org/)
- **Source Code**: [https://code.briarproject.org/briar/briar](https://code.briarproject.org/briar/briar)
- **License**: GNU General Public License v3.0

Mycel represents an evolution of the Briar codebase, with enhancements and modifications tailored for enhanced usability and extended functionality while maintaining the core principles of privacy, security, and decentralization.

---

## About Quantum Research

**Mycel** is maintained by **[Quantum Research Pty Ltd](https://qntrs.com)**, a technology company focused on privacy, security, and decentralized communication systems. Mycel builds upon the foundation of the Briar Project to provide enhanced decentralized messaging capabilities.

Our mission is to build tools that protect digital privacy and enable secure communication for everyone, everywhere.

---

<div align="center">

**⭐ Star this repository if you find Mycel useful!**

**Built with ❤️ for digital privacy and freedom**

</div>