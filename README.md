# Mycel

Mycel is a decentralized messaging app developed by **Quantum Research Pty Ltd** for secure peer-to-peer communication without central servers.

Unlike traditional messaging apps, Mycel doesn't rely on a central server - messages are synchronized directly between the users' devices.

If the Internet's down, Mycel can sync via Bluetooth or Wi-Fi, keeping information flowing in a crisis. If the Internet's up, Mycel can sync via the Tor network, protecting users and their relationships from surveillance.

## Features

- **Decentralized**: No central servers required
- **Multiple transports**: Tor, Bluetooth, Wi-Fi
- **End-to-end encryption**: All communications are encrypted
- **Offline messaging**: Works without internet connectivity
- **Cross-platform**: Android, Desktop, and Headless versions
- **Privacy-focused**: No tracking or data collection

## Building from Source

### Requirements
- Java 17
- Android SDK (for Android builds)
- Gradle 7.6.1 (included via wrapper)

### Build Commands
```bash
# Build all modules
./gradlew build

# Build Android APK
./gradlew :mycel-android:assembleDebug

# Run tests
./gradlew test

# Run headless version
./gradlew :mycel-headless:run
```

## Project Structure

- **Spore Layer** (Infrastructure): Core networking, crypto, and sync
  - `spore-api`: Transport and sync APIs
  - `spore-core`: Networking, crypto, and sync implementation
  - `spore-android`: Android-specific transport implementations
  - `spore-java`: Java/desktop transport implementations

- **Mycel Layer** (Application): High-level messaging features
  - `mycel-api`: Messaging APIs
  - `mycel-core`: Application features implementation
  - `mycel-android`: Android UI and platform integration
  - `mycel-headless`: REST API for headless operation

## Development

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines and project structure details.

For detailed documentation, see the [docs/](docs/) folder.

## License

Licensed under the GNU General Public License v3.0. See [LICENSE.txt](LICENSE.txt) for details.

## Developer

**Quantum Research Pty Ltd**  
https://qntrs.com