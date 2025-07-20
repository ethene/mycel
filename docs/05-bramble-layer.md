# Bramble Layer - Infrastructure Documentation

## Overview

The Bramble layer forms the foundational infrastructure of the Briar/Mycel application, providing core networking, cryptographic, and synchronization services. It implements the low-level protocols that enable secure peer-to-peer communication.

## Bramble Architecture

### Layer Structure

```
┌─────────────────────────────────────────┐
│            Briar Layer                  │  ← Application Logic
├─────────────────────────────────────────┤
│           Bramble Layer                 │  ← Infrastructure
├─────────────────────────────────────────┤
│  bramble-android  │  bramble-java       │  ← Platform Specific
├─────────────────────────────────────────┤
│      bramble-core (Implementation)     │  ← Core Services  
├─────────────────────────────────────────┤
│       bramble-api (Interfaces)         │  ← API Definitions
└─────────────────────────────────────────┘
```

### Module Dependencies

```
bramble-api (base interfaces)
    ↓
bramble-core (core implementation)
    ↓
├── bramble-android (Android platform)
└── bramble-java (Desktop platform)
```

## Bramble-API Module

### Core Interfaces

**Location**: `bramble-api/src/main/java/org/briarproject/bramble/api/`

#### Database Layer (`db/`)
```java
public interface Database {
    // Transaction management
    <E extends Exception> void transaction(boolean readOnly, 
        DatabaseRunnable<E> task) throws DbException, E;
    
    // Basic CRUD operations
    void addContact(Contact contact) throws DbException;
    Collection<Contact> getContacts() throws DbException;
    void removeContact(ContactId contactId) throws DbException;
}

public interface DatabaseComponent extends Database {
    // High-level database operations
    void addLocalMessage(Message message, MessageMetadata metadata, 
        boolean shared, boolean temporary) throws DbException;
}
```

#### Cryptographic Services (`crypto/`)
```java
public interface CryptoComponent {
    // Key generation
    KeyPair generateKeyPair();
    SecretKey generateSecretKey();
    
    // Digital signatures
    byte[] sign(String label, byte[] toSign, PrivateKey privateKey);
    boolean verify(byte[] signature, String label, byte[] signed, 
        PublicKey publicKey);
    
    // Message authentication
    byte[] mac(String label, SecretKey key, byte[]... inputs);
    
    // Key agreement
    SecretKey deriveKey(String label, SecretKey k, byte[]... inputs);
}
```

#### Plugin Architecture (`plugin/`)
```java
public interface Plugin {
    TransportId getId();
    long getMaxLatency();
    
    int getMaxIdleTime();
    void start() throws PluginException;
    void stop() throws PluginException;
    
    boolean isRunning();
    boolean shouldPoll();
    int getPollingInterval();
    void poll(Collection<Pair<TransportProperties, ConnectionHandler>> connections);
}

public interface PluginManager {
    Plugin getPlugin(TransportId transportId);
    Collection<Plugin> getPlugins();
    PluginState getPluginState(TransportId transportId);
}
```

#### Synchronization Protocol (`sync/`)
```java
public interface SyncSession {
    void sendMessage(MessageId messageId) throws DbException;
    void sendAck(MessageId messageId) throws DbException;
    void sendRequest(MessageId messageId) throws DbException;
    
    void receiveMessage(Message message) throws DbException;
    void receiveAck(MessageId messageId) throws DbException;
    void receiveRequest(MessageId messageId) throws DbException;
}

public interface SyncSessionFactory {
    SyncSession createIncomingSession(ContactId contactId, 
        TransportId transportId, InputStream in, OutputStream out);
    SyncSession createOutgoingSession(ContactId contactId, 
        TransportId transportId, InputStream in, OutputStream out);
}
```

#### Event System (`event/`)
```java
public interface EventBus {
    void addListener(EventListener listener);
    void removeListener(EventListener listener);
    void broadcast(Event event);
}

public interface Event {
    // Marker interface for all events
}

// Example events
public class ContactAddedEvent implements Event {
    private final ContactId contactId;
    // ...
}

public class MessageAddedEvent implements Event {
    private final Message message;
    private final ContactId contactId;
    // ...
}
```

#### Contact Management (`contact/`)
```java
public interface Contact {
    ContactId getId();
    Author getAuthor();
    AuthorId getLocalAuthorId();
    boolean isVerified();
    
    // Contact alias (user-defined name)
    @Nullable String getAlias();
}

public interface ContactManager {
    ContactId addContact(Author author, AuthorId localAuthorId, 
        boolean verified) throws DbException;
    void setContactAlias(ContactId contactId, @Nullable String alias) 
        throws DbException;
}
```

### Transport Abstractions

#### Transport Properties
```java
public interface TransportPropertyManager {
    Map<TransportId, TransportProperties> getLocalProperties() 
        throws DbException;
    Map<TransportId, TransportProperties> getRemoteProperties(ContactId c) 
        throws DbException;
    
    void mergeLocalProperties(Map<TransportId, TransportProperties> props) 
        throws DbException;
}
```

#### Connection Management
```java
public interface ConnectionManager {
    void manageOutgoingConnections(ContactId contactId, 
        TransportId transportId, ConnectionHandler connectionHandler);
    void manageIncomingConnections(TransportId transportId, 
        ConnectionHandler connectionHandler);
}
```

## Bramble-Core Module

### Core Implementation

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/`

#### Database Implementation (`db/`)

**JdbcDatabase.java** - Generic JDBC implementation:
```java
public abstract class JdbcDatabase implements Database {
    protected final DatabaseConfig config;
    protected final MessageFactory messageFactory;
    protected final Clock clock;
    
    @Override
    public <E extends Exception> void transaction(boolean readOnly, 
            DatabaseRunnable<E> task) throws DbException, E {
        Connection txn = startTransaction(readOnly);
        try {
            task.run(txn);
            commitTransaction(txn);
        } catch (Exception e) {
            abortTransaction(txn);
            throw e;
        }
    }
    
    // Abstract methods for database-specific implementation
    protected abstract Connection createConnection() throws DbException;
    protected abstract void compactAndClose() throws DbException;
}
```

**H2Database.java** - H2-specific implementation:
```java
public class H2Database extends JdbcDatabase {
    private static final String HASH_TYPE = "BINARY(32)";
    private static final String SECRET_TYPE = "BINARY(32)";
    private static final String BINARY_TYPE = "BINARY";
    private static final String COUNTER_TYPE = "BIGINT";
    
    @Override
    protected Connection createConnection() throws DbException {
        String url = "jdbc:h2:" + config.getDatabaseDirectory().getAbsolutePath() 
            + File.separator + "db;CIPHER=AES;MULTI_THREADED=1";
        
        Properties props = new Properties();
        props.setProperty("user", "user");
        props.setProperty("password", "password " + encryptionKey);
        
        return DriverManager.getConnection(url, props);
    }
}
```

#### Cryptographic Implementation (`crypto/`)

**CryptoComponentImpl.java**:
```java
@Singleton
public class CryptoComponentImpl implements CryptoComponent {
    private final SecureRandom secureRandom;
    private final PasswordBasedEncryption passwordCrypto;
    private final MessageEncryption messageEncryption;
    
    @Override
    public KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGenerator = 
            KeyPairGenerator.getInstance("Ed25519");
        keyPairGenerator.initialize(Ed25519ParameterSpec.Ed25519);
        return keyPairGenerator.generateKeyPair();
    }
    
    @Override
    public byte[] sign(String label, byte[] toSign, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(label.getBytes(UTF_8));
            signature.update(toSign);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

#### Synchronization Implementation (`sync/`)

**SyncSessionImpl.java**:
```java
public class SyncSessionImpl implements SyncSession {
    private final ContactId contactId;
    private final TransportId transportId;
    private final DatabaseComponent db;
    private final MessageEncoder encoder;
    private final MessageDecoder decoder;
    
    @Override
    public void sendMessage(MessageId messageId) throws DbException {
        // Retrieve message from database
        Message message = db.getMessage(messageId);
        
        // Encode and send message
        byte[] encoded = encoder.encode(message);
        outputStream.write(encoded);
    }
    
    @Override
    public void receiveMessage(Message message) throws DbException {
        // Validate message dependencies
        Collection<MessageId> dependencies = 
            db.getMessageDependencies(message.getGroupId(), message.getId());
        
        if (allDependenciesSatisfied(dependencies)) {
            // Add message to database
            db.addMessage(message, DELIVERED, true, null);
            
            // Broadcast message added event
            eventBus.broadcast(new MessageAddedEvent(message, contactId));
        } else {
            // Add as pending
            db.addMessage(message, PENDING, true, null);
        }
    }
}
```

#### Transport Key Management (`crypto/`)

**TransportKeyManagerImpl.java**:
```java
public class TransportKeyManagerImpl implements TransportKeyManager {
    @Override
    public TransportKeys deriveKeys(TransportId transportId, 
            SecretKey rootKey, long timePeriod, boolean alice) {
        
        // Derive transport-specific keys
        SecretKey incomingTagKey = deriveTagKey(rootKey, transportId, !alice);
        SecretKey incomingHeaderKey = deriveHeaderKey(rootKey, transportId, !alice);
        SecretKey outgoingTagKey = deriveTagKey(rootKey, transportId, alice);
        SecretKey outgoingHeaderKey = deriveHeaderKey(rootKey, transportId, alice);
        
        // Apply time-based rotation
        if (timePeriod > 0) {
            incomingTagKey = rotateKey(incomingTagKey, timePeriod);
            incomingHeaderKey = rotateKey(incomingHeaderKey, timePeriod);
            outgoingTagKey = rotateKey(outgoingTagKey, timePeriod);
            outgoingHeaderKey = rotateKey(outgoingHeaderKey, timePeriod);
        }
        
        return new TransportKeys(incomingTagKey, incomingHeaderKey,
                               outgoingTagKey, outgoingHeaderKey, true);
    }
}
```

### Plugin System Implementation

#### Plugin Manager (`plugin/`)

**PluginManagerImpl.java**:
```java
@Singleton
public class PluginManagerImpl implements PluginManager {
    private final Map<TransportId, Plugin> plugins;
    private final Map<TransportId, PluginState> states;
    private final EventBus eventBus;
    
    @Inject
    public PluginManagerImpl(Set<Plugin> plugins, EventBus eventBus) {
        this.plugins = new HashMap<>();
        this.states = new HashMap<>();
        this.eventBus = eventBus;
        
        for (Plugin plugin : plugins) {
            this.plugins.put(plugin.getId(), plugin);
            this.states.put(plugin.getId(), STARTING);
        }
    }
    
    public void startPlugins() throws PluginException {
        for (Plugin plugin : plugins.values()) {
            try {
                plugin.start();
                states.put(plugin.getId(), ACTIVE);
                eventBus.broadcast(new PluginStateEvent(plugin.getId(), ACTIVE));
            } catch (PluginException e) {
                states.put(plugin.getId(), INACTIVE);
                LOG.warning("Failed to start plugin " + plugin.getId());
            }
        }
    }
}
```

#### Connection Management (`plugin/`)

**ConnectionManagerImpl.java**:
```java
public class ConnectionManagerImpl implements ConnectionManager {
    private final PluginManager pluginManager;
    private final EventBus eventBus;
    private final Executor ioExecutor;
    
    @Override
    public void manageOutgoingConnections(ContactId contactId, 
            TransportId transportId, ConnectionHandler handler) {
        Plugin plugin = pluginManager.getPlugin(transportId);
        if (plugin != null && plugin.isRunning()) {
            ioExecutor.execute(() -> {
                try {
                    DuplexTransportConnection connection = 
                        plugin.createConnection(contactId);
                    handler.handleConnection(connection);
                } catch (IOException e) {
                    LOG.warning("Failed to create outgoing connection");
                }
            });
        }
    }
}
```

## Platform-Specific Implementations

### Bramble-Android Module

**Location**: `bramble-android/src/main/java/org/briarproject/bramble/`

#### Android-Specific Services

**AndroidModule.java** - Dependency injection configuration:
```java
@Module
public class AndroidModule {
    @Provides
    @Singleton
    SecureRandomProvider provideSecureRandomProvider() {
        return new AndroidSecureRandomProvider();
    }
    
    @Provides
    @Singleton
    KeyStrengthener provideKeyStrengthener(Context context) {
        return new AndroidKeyStrengthener(context);
    }
}
```

#### Android Secure Random Provider
```java
public class AndroidSecureRandomProvider implements SecureRandomProvider {
    @Override
    public Provider getProvider() {
        // Use Android's crypto provider when available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new AndroidKeyStoreProvider();
        }
        return null; // Fall back to default
    }
}
```

### Bramble-Java Module

**Location**: `bramble-java/src/main/java/org/briarproject/bramble/`

#### Desktop-Specific Services

**JavaModule.java**:
```java
@Module
public class JavaModule {
    @Provides
    @Singleton
    SecureRandomProvider provideSecureRandomProvider() {
        return new JavaSecureRandomProvider();
    }
    
    @Provides
    @Singleton
    KeyStrengthener provideKeyStrengthener() {
        return new NullKeyStrengthener(); // No hardware strengthening
    }
}
```

## Event System Implementation

### Event Bus

**EventBusImpl.java**:
```java
@Singleton
public class EventBusImpl implements EventBus {
    private final Set<EventListener> listeners = 
        Collections.synchronizedSet(new HashSet<>());
    private final Executor eventExecutor;
    
    @Override
    public void broadcast(Event event) {
        eventExecutor.execute(() -> {
            synchronized (listeners) {
                for (EventListener listener : listeners) {
                    try {
                        listener.eventOccurred(event);
                    } catch (Exception e) {
                        LOG.warning("Error in event listener");
                    }
                }
            }
        });
    }
}
```

### Core Events

```java
// Database events
public class ContactAddedEvent implements Event {
    public final ContactId contactId;
    public final boolean verified;
}

public class MessageAddedEvent implements Event {
    public final Message message;
    public final ContactId contactId;
    public final boolean local;
}

// Transport events
public class ContactConnectedEvent implements Event {
    public final ContactId contactId;
    public final TransportId transportId;
}

public class ContactDisconnectedEvent implements Event {
    public final ContactId contactId;
    public final TransportId transportId;
}
```

## Dependency Injection Architecture

### Dagger Configuration

**BrambleModule.java**:
```java
@Module
public class BrambleModule {
    @Provides
    @Singleton
    DatabaseComponent provideDatabaseComponent(DatabaseComponentImpl impl) {
        return impl;
    }
    
    @Provides
    @Singleton  
    CryptoComponent provideCryptoComponent(CryptoComponentImpl impl) {
        return impl;
    }
    
    @Provides
    @Singleton
    EventBus provideEventBus(EventBusImpl impl) {
        return impl;
    }
}
```

### Component Interfaces

**BrambleComponent.java**:
```java
@Singleton
@Component(modules = {BrambleModule.class, CryptoModule.class, 
                     DatabaseModule.class, EventModule.class,
                     PluginModule.class, SyncModule.class})
public interface BrambleComponent {
    DatabaseComponent getDatabaseComponent();
    CryptoComponent getCryptoComponent();
    PluginManager getPluginManager();
    EventBus getEventBus();
    
    void inject(BrambleService service);
}
```

## Testing Infrastructure

### Mock Implementations

**MockDatabaseComponent.java**:
```java
public class MockDatabaseComponent implements DatabaseComponent {
    private final Map<ContactId, Contact> contacts = new HashMap<>();
    private final Map<MessageId, Message> messages = new HashMap<>();
    
    @Override
    public void addContact(Contact contact) {
        contacts.put(contact.getId(), contact);
    }
    
    @Override
    public Collection<Contact> getContacts() {
        return new ArrayList<>(contacts.values());
    }
}
```

## Performance Considerations

### Connection Pooling
- Database connections pooled for efficiency
- Transport connections reused when possible
- Thread pools sized appropriately for platform

### Memory Management
- Large objects (messages, keys) lifecycle managed
- Weak references used for event listeners
- Explicit cleanup in disposal methods

### Caching Strategies
- Transport properties cached
- Contact information cached
- Message metadata indexed for fast lookup

## Rebranding Implications for Bramble Layer

### Low Impact Areas
- **Core Protocols**: Transport and sync protocols remain unchanged
- **Cryptographic Implementation**: All crypto code is brand-agnostic
- **Database Schema**: Table structures independent of branding

### Moderate Impact Areas
- **Package Names**: `org.briarproject.bramble.*` → `com.quantumresearch.mycel.infrastructure.*`
- **Configuration Constants**: Some default values may reference "bramble"
- **Logging Messages**: Log strings may contain "bramble" references

### Implementation Requirements for Mycel
1. **Package Renaming**: Systematic rename of all package declarations
2. **Import Updates**: Update all import statements across modules
3. **Configuration Updates**: Update any bramble-specific configuration values
4. **Documentation Updates**: Update all code comments and documentation

The Bramble layer provides a robust, well-architected foundation that will seamlessly support the Mycel rebranding with minimal changes to core functionality.