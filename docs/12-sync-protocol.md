# Sync Protocol Documentation

## Overview

The Briar/Mycel sync protocol is responsible for maintaining consistency of messages and data across all devices and contacts in the peer-to-peer network. It ensures reliable message delivery, handles conflicts, and maintains message ordering while preserving privacy and security.

## Sync Protocol Architecture

### Protocol Layers

```
┌─────────────────────────────────────────┐
│        Application Messages            │  ← User content
├─────────────────────────────────────────┤
│         Sync Protocol Layer            │  ← THIS LAYER
├─────────────────────────────────────────┤
│        Transport Encryption            │  ← BTP (Briar Transport Protocol)
├─────────────────────────────────────────┤
│         Transport Plugins              │  ← Tor, Bluetooth, LAN
└─────────────────────────────────────────┘
```

### Core Components

**Location**: `spore-core/src/main/java/org/briarproject/bramble/sync/`

#### Sync Session Management

**SyncSessionFactory.java**:
```java
public interface SyncSessionFactory {
    // Create sessions for different connection types
    SyncSession createIncomingSession(ContactId contactId, 
        TransportId transportId, InputStream in, OutputStream out);
    
    SyncSession createOutgoingSession(ContactId contactId, 
        TransportId transportId, InputStream in, OutputStream out);
}

@Singleton
public class SyncSessionFactoryImpl implements SyncSessionFactory {
    private final DatabaseComponent db;
    private final MessageEncoder messageEncoder;
    private final MessageDecoder messageDecoder;
    
    @Override
    public SyncSession createIncomingSession(ContactId contactId, 
            TransportId transportId, InputStream in, OutputStream out) {
        return new SyncSessionImpl(contactId, transportId, db, 
            messageEncoder, messageDecoder, in, out, false);
    }
}
```

#### Message Synchronization

**SyncSession.java**:
```java
public interface SyncSession {
    // Message transmission
    void sendMessage(MessageId messageId) throws DbException, IOException;
    void sendAck(MessageId messageId) throws DbException, IOException;
    void sendRequest(MessageId messageId) throws DbException, IOException;
    void sendOffer(MessageId messageId) throws DbException, IOException;
    
    // Message reception
    void receiveMessage(Message message) throws DbException;
    void receiveAck(MessageId messageId) throws DbException;
    void receiveRequest(MessageId messageId) throws DbException;
    void receiveOffer(MessageId messageId) throws DbException;
    
    // Session management
    void start() throws IOException;
    void stop() throws IOException;
    boolean isConnected();
}
```

## Sync Protocol Messages

### Message Types

#### 1. OFFER Messages
```java
public class OfferMessage {
    private final Collection<MessageId> messageIds;
    
    // Offers available messages to peer
    public OfferMessage(Collection<MessageId> messageIds) {
        this.messageIds = messageIds;
    }
}
```

**Purpose**: Inform peer about available messages without sending content

#### 2. REQUEST Messages  
```java
public class RequestMessage {
    private final Collection<MessageId> messageIds;
    
    // Requests specific messages from peer
    public RequestMessage(Collection<MessageId> messageIds) {
        this.messageIds = messageIds;
    }
}
```

**Purpose**: Request specific messages from peer

#### 3. MESSAGE Messages
```java
public class MessageMessage {
    private final Message message;
    private final MessageMetadata metadata;
    
    // Delivers actual message content
    public MessageMessage(Message message, MessageMetadata metadata) {
        this.message = message;
        this.metadata = metadata;
    }
}
```

**Purpose**: Deliver actual message content and metadata

#### 4. ACK Messages
```java
public class AckMessage {
    private final Collection<MessageId> messageIds;
    
    // Acknowledges receipt of messages
    public AckMessage(Collection<MessageId> messageIds) {
        this.messageIds = messageIds;
    }
}
```

**Purpose**: Confirm successful receipt and processing of messages

## Sync Protocol Flow

### Typical Sync Session

```
Alice                           Bob
  |                              |
  |--- OFFER [msg1, msg2] ------>|
  |                              |
  |<--- REQUEST [msg1] -----------|
  |                              |
  |--- MESSAGE [msg1] ---------->|
  |                              |
  |<--- ACK [msg1] --------------|
  |                              |
  |<--- OFFER [msg3] ------------|
  |                              |
  |--- REQUEST [msg3] ---------->|
  |                              |
  |<--- MESSAGE [msg3] ----------|
  |                              |
  |--- ACK [msg3] -------------->|
```

### Protocol State Machine

**SyncState.java**:
```java
public enum SyncState {
    START,          // Initial state
    SENDING_OFFERS, // Sending available message IDs
    SENDING_MESSAGES, // Sending requested messages
    RECEIVING_MESSAGES, // Receiving messages from peer
    FINISHED        // Session complete
}

public class SyncStateMachine {
    private SyncState currentState = SyncState.START;
    private final Set<MessageId> offeredByUs = new HashSet<>();
    private final Set<MessageId> offeredByThem = new HashSet<>();
    private final Set<MessageId> requestedByUs = new HashSet<>();
    private final Set<MessageId> requestedByThem = new HashSet<>();
    
    public void transition(SyncEvent event) {
        switch (currentState) {
            case START:
                handleStartState(event);
                break;
            case SENDING_OFFERS:
                handleSendingOffersState(event);
                break;
            // ... other states
        }
    }
}
```

## Message Ordering and Dependencies

### Dependency Management

**MessageDependencyManager.java**:
```java
public interface MessageDependencyManager {
    // Check if message dependencies are satisfied
    boolean dependenciesSatisfied(GroupId groupId, MessageId messageId) 
        throws DbException;
    
    // Get missing dependencies for a message
    Collection<MessageId> getMissingDependencies(GroupId groupId, 
        MessageId messageId) throws DbException;
    
    // Update dependency state when message arrives
    void updateDependencies(GroupId groupId, MessageId messageId) 
        throws DbException;
}
```

**Dependency Resolution**:
```java
@Singleton
public class MessageDependencyManagerImpl implements MessageDependencyManager {
    private final DatabaseComponent db;
    
    @Override
    public boolean dependenciesSatisfied(GroupId groupId, MessageId messageId) 
            throws DbException {
        Collection<MessageId> dependencies = 
            db.getMessageDependencies(groupId, messageId);
        
        for (MessageId dependency : dependencies) {
            MessageState state = db.getMessageState(dependency);
            if (state != DELIVERED) {
                return false;
            }
        }
        return true;
    }
}
```

### Message Ordering Guarantees

#### 1. Causal Ordering
- Messages delivered in causal order within conversations
- Dependencies tracked through message references
- Logical timestamps used for ordering

#### 2. Vector Clocks
```java
public class VectorClock {
    private final Map<AuthorId, Long> clock;
    
    public VectorClock increment(AuthorId author) {
        Map<AuthorId, Long> newClock = new HashMap<>(clock);
        newClock.put(author, newClock.getOrDefault(author, 0L) + 1);
        return new VectorClock(newClock);
    }
    
    public boolean happensBefore(VectorClock other) {
        for (Map.Entry<AuthorId, Long> entry : clock.entrySet()) {
            AuthorId author = entry.getKey();
            Long ourTime = entry.getValue();
            Long theirTime = other.clock.get(author);
            
            if (theirTime == null || ourTime > theirTime) {
                return false;
            }
        }
        return !equals(other);
    }
}
```

## Message State Management

### Message States

```java
public enum MessageState {
    UNKNOWN(0),     // Initial state for received messages
    PENDING(1),     // Awaiting dependency resolution
    DELIVERED(2),   // Successfully validated and stored
    INVALID(3);     // Failed validation
    
    private final int value;
}
```

### State Transitions

**MessageStateManager.java**:
```java
public class MessageStateManager {
    public void updateMessageState(MessageId messageId, MessageState newState) 
            throws DbException {
        MessageState currentState = db.getMessageState(messageId);
        
        if (isValidTransition(currentState, newState)) {
            db.setMessageState(messageId, newState);
            
            // Trigger dependent message processing
            if (newState == DELIVERED) {
                processDependentMessages(messageId);
            }
            
            // Broadcast state change event
            eventBus.broadcast(new MessageStateChangedEvent(messageId, 
                currentState, newState));
        }
    }
    
    private boolean isValidTransition(MessageState from, MessageState to) {
        switch (from) {
            case UNKNOWN:
                return to == PENDING || to == DELIVERED || to == INVALID;
            case PENDING:
                return to == DELIVERED || to == INVALID;
            case DELIVERED:
            case INVALID:
                return false; // Final states
            default:
                return false;
        }
    }
}
```

## Conflict Resolution

### Message Conflicts

#### 1. Duplicate Messages
```java
public class DuplicateMessageDetector {
    public boolean isDuplicate(Message message) throws DbException {
        // Check if message ID already exists
        return db.containsMessage(message.getId());
    }
    
    public void handleDuplicate(Message message) {
        // Log duplicate but don't store
        LOG.info("Ignoring duplicate message: " + message.getId());
    }
}
```

#### 2. Concurrent Updates
```java
public class ConflictResolver {
    public Message resolveConflict(Message local, Message remote) {
        // Use timestamp-based resolution
        if (local.getTimestamp() > remote.getTimestamp()) {
            return local;
        } else if (remote.getTimestamp() > local.getTimestamp()) {
            return remote;
        } else {
            // Same timestamp - use message ID comparison
            return local.getId().compareTo(remote.getId()) < 0 ? local : remote;
        }
    }
}
```

## Transport Integration

### Transport-Specific Sync

**TransportSyncManager.java**:
```java
public class TransportSyncManager {
    private final Map<TransportId, SyncSession> activeSessions;
    private final PluginManager pluginManager;
    
    public void startSync(ContactId contactId, TransportId transportId) {
        Plugin plugin = pluginManager.getPlugin(transportId);
        if (plugin != null && plugin.isRunning()) {
            try {
                DuplexTransportConnection connection = 
                    plugin.createConnection(contactId);
                
                SyncSession session = syncSessionFactory.createOutgoingSession(
                    contactId, transportId, 
                    connection.getInputStream(), 
                    connection.getOutputStream());
                
                activeSessions.put(transportId, session);
                session.start();
                
            } catch (IOException e) {
                LOG.warning("Failed to start sync session");
            }
        }
    }
}
```

### Priority-Based Transmission

```java
public class MessagePriorityManager {
    public int getPriority(Message message) {
        // Higher priority for newer messages
        long age = clock.currentTimeMillis() - message.getTimestamp();
        
        if (age < TimeUnit.HOURS.toMillis(1)) {
            return PRIORITY_HIGH;
        } else if (age < TimeUnit.DAYS.toMillis(1)) {
            return PRIORITY_MEDIUM;
        } else {
            return PRIORITY_LOW;
        }
    }
    
    public Collection<MessageId> getMessagesToSync(ContactId contactId, 
            int maxMessages) throws DbException {
        // Get unsent messages ordered by priority
        return db.getMessagesToSend(contactId, maxMessages)
            .stream()
            .sorted((m1, m2) -> Integer.compare(
                getPriority(m2), getPriority(m1)))
            .map(Message::getId)
            .collect(toList());
    }
}
```

## Reliability and Error Handling

### Retry Mechanism

**RetryManager.java**:
```java
public class RetryManager {
    private final ScheduledExecutorService scheduler;
    private final Map<MessageId, RetryState> retryStates;
    
    public void scheduleRetry(MessageId messageId, ContactId contactId, 
            long delay) {
        RetryState state = retryStates.computeIfAbsent(messageId, 
            id -> new RetryState());
        
        if (state.getAttempts() < MAX_RETRY_ATTEMPTS) {
            scheduler.schedule(() -> {
                retryMessage(messageId, contactId);
            }, delay, TimeUnit.MILLISECONDS);
            
            state.incrementAttempts();
        } else {
            // Give up after max attempts
            markMessageFailed(messageId);
        }
    }
    
    private void retryMessage(MessageId messageId, ContactId contactId) {
        try {
            // Attempt to resend message
            syncSessionManager.sendMessage(contactId, messageId);
        } catch (Exception e) {
            // Schedule another retry with exponential backoff
            long delay = calculateBackoffDelay(messageId);
            scheduleRetry(messageId, contactId, delay);
        }
    }
}
```

### Flow Control

**FlowController.java**:
```java
public class FlowController {
    private final int maxConcurrentMessages;
    private final AtomicInteger inFlightMessages = new AtomicInteger(0);
    
    public boolean canSendMessage() {
        return inFlightMessages.get() < maxConcurrentMessages;
    }
    
    public void messageSent(MessageId messageId) {
        inFlightMessages.incrementAndGet();
        pendingAcks.add(messageId);
    }
    
    public void ackReceived(MessageId messageId) {
        if (pendingAcks.remove(messageId)) {
            inFlightMessages.decrementAndGet();
        }
    }
}
```

## Performance Optimizations

### Batch Processing

**BatchSyncProcessor.java**:
```java
public class BatchSyncProcessor {
    private final int batchSize;
    private final long batchTimeout;
    
    public void processBatch(Collection<MessageId> messageIds, 
            ContactId contactId) throws DbException {
        // Group messages by batch size
        List<List<MessageId>> batches = Lists.partition(
            new ArrayList<>(messageIds), batchSize);
        
        for (List<MessageId> batch : batches) {
            sendOfferBatch(batch, contactId);
        }
    }
    
    private void sendOfferBatch(List<MessageId> messageIds, 
            ContactId contactId) throws DbException {
        OfferMessage offer = new OfferMessage(messageIds);
        syncSession.sendOffer(offer);
    }
}
```

### Compression

**MessageCompressor.java**:
```java
public class MessageCompressor {
    public byte[] compress(byte[] messageData) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(messageData);
        }
        return out.toByteArray();
    }
    
    public byte[] decompress(byte[] compressedData) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try (GZIPInputStream gzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return out.toByteArray();
    }
}
```

## Monitoring and Diagnostics

### Sync Statistics

**SyncStatistics.java**:
```java
public class SyncStatistics {
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong bytesTransferred = new AtomicLong(0);
    private final AtomicLong syncSessions = new AtomicLong(0);
    
    public void recordMessageSent(int bytes) {
        messagesSent.incrementAndGet();
        bytesTransferred.addAndGet(bytes);
    }
    
    public void recordMessageReceived(int bytes) {
        messagesReceived.incrementAndGet();
        bytesTransferred.addAndGet(bytes);
    }
    
    public SyncStatisticsSnapshot getSnapshot() {
        return new SyncStatisticsSnapshot(
            messagesSent.get(),
            messagesReceived.get(),
            bytesTransferred.get(),
            syncSessions.get()
        );
    }
}
```

## Security Considerations

### Authentication
- All sync sessions authenticated using contact keys
- Message integrity verified through transport-level MACs
- Replay protection through sequence numbers

### Privacy Protection
- Message metadata minimized in sync protocol
- Timing analysis resistance through padding
- No plaintext content exposed during sync

### Attack Resistance
- DoS protection through rate limiting
- Message validation before processing
- Resource limits on pending messages

## Rebranding Implications

### Protocol Compatibility
- Sync protocol is brand-agnostic
- No protocol changes needed for Mycel
- Message formats remain unchanged

### Configuration Updates
- Sync timeouts and parameters configurable
- No hardcoded briar references in protocol
- Logging messages may reference "sync" generically

The sync protocol provides robust, ordered message delivery with strong consistency guarantees while maintaining the security and privacy properties essential for the Briar/Mycel messaging system.