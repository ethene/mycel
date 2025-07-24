# Contributing to Mycel

**Mycel** is a decentralized messaging app developed by **Quantum Research Pty Ltd**. This document provides guidelines for contributing to the project.

## Project Structure

### Module Organization

The project is organized into two main layers:

#### **Spore Layer** (Infrastructure)
Core networking, cryptography, and synchronization protocols:

* `spore-api`: Public interfaces for transport and sync APIs
* `spore-core`: Portable implementations (works on Android/Desktop/Headless)  
* `spore-java`: Desktop & Headless-specific implementations
* `spore-android`: Android-specific transport implementations

#### **Mycel Layer** (Application)
High-level messaging application features:

* `mycel-api`: Public interfaces for messaging features
* `mycel-core`: Portable implementations of messaging logic
* `mycel-android`: Android UI and platform integration
* `mycel-headless`: REST API for headless/server operation

### Module Types

* `*-api`: Public interfaces and utility classes that can be referenced from other packages
* `*-core`: Portable implementations that work across Android/Desktop/Headless
* `*-java`: Desktop & Headless-specific implementations
* `*-android`: Android-specific implementations

## Development Setup

### Requirements
- Java 17
- Android SDK (for Android development)
- Gradle 7.6.1 (included via wrapper)

### Building
```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :mycel-android:assembleDebug

# Run tests
./gradlew test
```

## Code Guidelines

### Architecture Principles

1. **Layered Architecture**: Maintain clear separation between Spore (infrastructure) and Mycel (application) layers
2. **Dependency Injection**: Use Dagger 2 for dependency management
3. **Plugin Architecture**: Transport plugins (Bluetooth, TCP, Tor) should be pluggable
4. **Event-Driven**: Use EventBus for decoupled component communication
5. **Factory Pattern**: Use factory interfaces for object creation

### Security Considerations

- All networking goes through encrypted transports
- Database is encrypted with user-derived keys
- Never log sensitive information (keys, message content)
- Follow existing crypto patterns for new features

### Testing

- Unit tests in `src/test` directories
- Android instrumentation tests in `src/androidTest`
- Use JMock for mocking in unit tests
- Use Robolectric for Android unit tests

## Adding New Features

### Transport Plugins

1. Implement `Plugin` interface in appropriate `spore-*` module
2. Create corresponding `PluginFactory`
3. Register in appropriate Dagger module
4. Add configuration in `TransportPropertyManager`

### Message Types

1. Define API in `mycel-api` messaging package
2. Implement validation in `mycel-core`
3. Add database schema changes if needed
4. Create UI components for display/editing
5. Add comprehensive test coverage

### Working with Events

- Use `EventBus` for loose coupling between components
- Define events in `api.event` package
- Register listeners in appropriate lifecycle methods
- Always consider thread safety when handling events

## Documentation

See the [docs/](docs/) folder for comprehensive documentation including:

- Project architecture details
- Development setup guides
- API documentation
- Deployment guides

## License

This project is licensed under the GNU General Public License v3.0.

## Contact

**Quantum Research Pty Ltd**  
https://qntrs.com