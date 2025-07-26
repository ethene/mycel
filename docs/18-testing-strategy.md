# Testing Strategy and Framework

## Overview

This document outlines the comprehensive testing strategy for the Briar/Mycel application, covering unit tests, integration tests, UI tests, and security testing across all modules and platforms.

## Testing Philosophy

### Testing Pyramid

```
                 E2E Tests (Few)
               ┌─────────────────┐
              ┌───────────────────┐
             │   Integration Tests │ (Some)
            └─────────────────────┘
          ┌─────────────────────────┐
         │       Unit Tests          │ (Many)
        └───────────────────────────┘
```

**Test Distribution**:
- **Unit Tests**: 70% - Fast, isolated, comprehensive coverage
- **Integration Tests**: 20% - Component interaction verification
- **End-to-End Tests**: 10% - Critical user journey validation

### Testing Principles

1. **Test-Driven Development (TDD)**: Write tests before implementation
2. **Fail Fast**: Tests should fail quickly and clearly
3. **Deterministic**: Tests must be reliable and repeatable
4. **Isolated**: Tests should not depend on external services
5. **Maintainable**: Tests should be easy to understand and modify

## Test Framework Architecture

### Core Testing Stack

#### Java/Kotlin Testing
- **JUnit 4**: Primary testing framework
- **JMock**: Mock object framework for dependency isolation
- **Hamcrest**: Expressive assertion library
- **ConcurrentUnit**: Asynchronous testing utilities

#### Android Testing
- **Robolectric**: Android unit testing without emulator
- **Espresso**: UI testing framework
- **AndroidX Test**: Modern Android testing libraries
- **Mockito**: Additional mocking capabilities

#### Build Integration
- **Gradle**: Test execution and reporting
- **Witness Plugin**: Dependency verification
- **Test Orchestrator**: Isolated test execution on Android

## Module-Specific Testing

### Bramble Layer Testing

#### Location: `spore-*/src/test/`

**Core Components Tested**:

#### 1. Database Layer Tests

**File**: `JdbcDatabaseTest.java`
```java
public class JdbcDatabaseTest {
    private Database db;
    private DatabaseConfig config;
    
    @Before
    public void setUp() throws Exception {
        config = new TestDatabaseConfig();
        db = new H2Database(config, messageFactory, clock);
    }
    
    @Test
    public void testTransactionCommit() throws Exception {
        Contact contact = createTestContact();
        
        db.transaction(false, txn -> {
            db.addContact(contact);
            // Verify contact exists within transaction
            assertTrue(db.containsContact(contact.getId()));
        });
        
        // Verify contact persisted after transaction
        assertTrue(db.containsContact(contact.getId()));
    }
    
    @Test
    public void testTransactionRollback() throws Exception {
        Contact contact = createTestContact();
        
        try {
            db.transaction(false, txn -> {
                db.addContact(contact);
                throw new TestException(); // Force rollback
            });
        } catch (TestException e) {
            // Expected
        }
        
        // Verify contact was not persisted
        assertFalse(db.containsContact(contact.getId()));
    }
}
```

#### 2. Cryptographic Tests

**File**: `CryptoComponentImplTest.java`
```java
public class CryptoComponentImplTest {
    private CryptoComponent crypto;
    
    @Before
    public void setUp() {
        crypto = new CryptoComponentImpl(secureRandom);
    }
    
    @Test
    public void testKeyPairGeneration() {
        KeyPair keyPair = crypto.generateKeyPair();
        
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
        assertEquals("Ed25519", keyPair.getPublic().getAlgorithm());
        assertEquals("Ed25519", keyPair.getPrivate().getAlgorithm());
    }
    
    @Test
    public void testSignatureVerification() {
        KeyPair keyPair = crypto.generateKeyPair();
        byte[] data = "test message".getBytes(UTF_8);
        String label = "test_signature";
        
        // Sign data
        byte[] signature = crypto.sign(label, data, keyPair.getPrivate());
        
        // Verify signature
        assertTrue(crypto.verify(signature, label, data, keyPair.getPublic()));
        
        // Verify with wrong data fails
        byte[] wrongData = "wrong message".getBytes(UTF_8);
        assertFalse(crypto.verify(signature, label, wrongData, keyPair.getPublic()));
    }
}
```

#### 3. Synchronization Protocol Tests

**File**: `SyncSessionImplTest.java`
```java
public class SyncSessionImplTest {
    private SyncSession syncSession;
    private MockDatabaseComponent mockDb;
    private ByteArrayInputStream inputStream;
    private ByteArrayOutputStream outputStream;
    
    @Before
    public void setUp() {
        mockDb = new MockDatabaseComponent();
        inputStream = new ByteArrayInputStream(new byte[0]);
        outputStream = new ByteArrayOutputStream();
        
        syncSession = new SyncSessionImpl(contactId, transportId,
            mockDb, encoder, decoder, inputStream, outputStream, false);
    }
    
    @Test
    public void testSendMessage() throws Exception {
        MessageId messageId = new MessageId(getRandomId());
        Message message = createTestMessage(messageId);
        
        // Mock database returns our test message
        mockDb.addMessage(message);
        
        // Send message
        syncSession.sendMessage(messageId);
        
        // Verify message was encoded and written to output stream
        assertTrue(outputStream.size() > 0);
    }
    
    @Test
    public void testReceiveMessageWithSatisfiedDependencies() throws Exception {
        Message message = createTestMessage();
        
        // Mock satisfied dependencies
        when(mockDb.getMessageDependencies(any(), any()))
            .thenReturn(Collections.emptyList());
        
        // Receive message
        syncSession.receiveMessage(message);
        
        // Verify message added as DELIVERED
        assertEquals(DELIVERED, mockDb.getMessageState(message.getId()));
    }
}
```

### Briar Layer Testing

#### Location: `mycel-*/src/test/`

#### 1. Messaging Tests

**File**: `ConversationManagerImplTest.java`
```java
public class ConversationManagerImplTest {
    private ConversationManager conversationManager;
    private MockDatabaseComponent mockDb;
    private MockEventBus mockEventBus;
    
    @Test
    public void testSendPrivateMessage() throws Exception {
        ContactId contactId = new ContactId(getRandomId());
        String text = "Hello, world!";
        long timestamp = clock.currentTimeMillis();
        
        // Send message
        conversationManager.sendPrivateMessage(contactId, text, 
            timestamp, MessageType.TEXT);
        
        // Verify message was added to database
        verify(mockDb).addLocalMessage(any(PrivateMessage.class), 
            any(MessageMetadata.class), eq(true), eq(false));
        
        // Verify event was broadcast
        verify(mockEventBus).broadcast(any(ConversationMessageReceivedEvent.class));
    }
    
    @Test
    public void testGetMessageHeaders() throws Exception {
        ContactId contactId = new ContactId(getRandomId());
        List<MessageHeader> headers = createTestMessageHeaders(contactId);
        
        when(mockDb.getMessageHeaders(any(GroupId.class)))
            .thenReturn(headers);
        
        Collection<ConversationMessageHeader> result = 
            conversationManager.getMessageHeaders(contactId);
        
        assertEquals(headers.size(), result.size());
    }
}
```

#### 2. Forum Tests

**File**: `ForumManagerImplTest.java`
```java
public class ForumManagerImplTest {
    private ForumManager forumManager;
    
    @Test
    public void testAddForum() throws Exception {
        String forumName = "Test Forum";
        
        Forum forum = forumManager.addForum(forumName);
        
        assertNotNull(forum.getId());
        assertEquals(forumName, forum.getName());
        verify(mockEventBus).broadcast(any(ForumAddedEvent.class));
    }
    
    @Test
    public void testAddLocalPost() throws Exception {
        GroupId groupId = new GroupId(getRandomId());
        String text = "Test post content";
        long timestamp = clock.currentTimeMillis();
        
        ForumPostHeader header = forumManager.addLocalPost(groupId, 
            text, timestamp, null);
        
        assertNotNull(header);
        assertEquals(text, header.getText());
        verify(mockEventBus).broadcast(any(ForumPostAddedEvent.class));
    }
}
```

### Android Testing

#### Location: `mycel-android/src/test/` (Unit) and `mycel-android/src/androidTest/` (Instrumented)

#### 1. ViewModel Unit Tests

**File**: `ConversationViewModelTest.java`
```java
@RunWith(RobolectricTestRunner.class)
public class ConversationViewModelTest {
    private ConversationViewModel viewModel;
    private MockConversationManager mockConversationManager;
    private TestObserver<List<ConversationMessageHeader>> messagesObserver;
    
    @Before
    public void setUp() {
        mockConversationManager = new MockConversationManager();
        viewModel = new ConversationViewModel(application, 
            mockConversationManager, eventBus);
        messagesObserver = new TestObserver<>();
        viewModel.getMessages().observeForever(messagesObserver);
    }
    
    @Test
    public void testLoadMessages() {
        ContactId contactId = new ContactId(getRandomId());
        List<ConversationMessageHeader> testMessages = createTestMessages();
        
        when(mockConversationManager.getMessageHeaders(contactId))
            .thenReturn(testMessages);
        
        viewModel.loadMessages(contactId);
        
        // Verify messages were loaded
        messagesObserver.assertValueCount(1);
        assertEquals(testMessages, messagesObserver.getValues().get(0));
    }
}
```

#### 2. UI Instrumentation Tests

**File**: `ConversationActivityTest.java`
```java
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConversationActivityTest {
    @Rule
    public ActivityTestRule<ConversationActivity> activityRule = 
        new ActivityTestRule<>(ConversationActivity.class);
    
    @Test
    public void testSendMessage() {
        String messageText = "Test message";
        
        // Type message
        onView(withId(R.id.message_input))
            .perform(typeText(messageText));
        
        // Click send button
        onView(withId(R.id.send_button))
            .perform(click());
        
        // Verify message appears in conversation
        onView(withText(messageText))
            .check(matches(isDisplayed()));
        
        // Verify input is cleared
        onView(withId(R.id.message_input))
            .check(matches(withText("")));
    }
    
    @Test
    public void testContactVerificationIndicator() {
        // Test with verified contact
        setContactVerified(true);
        
        onView(withId(R.id.trust_indicator))
            .check(matches(hasBackground(R.drawable.trust_verified)));
        
        // Test with unverified contact
        setContactVerified(false);
        
        onView(withId(R.id.trust_indicator))
            .check(matches(hasBackground(R.drawable.trust_unverified)));
    }
}
```

#### 3. Custom View Tests

**File**: `TrustIndicatorViewTest.java`
```java
@RunWith(RobolectricTestRunner.class)
public class TrustIndicatorViewTest {
    private TrustIndicatorView trustIndicator;
    
    @Before
    public void setUp() {
        trustIndicator = new TrustIndicatorView(ApplicationProvider.getApplicationContext());
    }
    
    @Test
    public void testTrustedState() {
        trustIndicator.setTrusted(true);
        assertTrue(trustIndicator.isTrusted());
    }
    
    @Test
    public void testUntrustedState() {
        trustIndicator.setTrusted(false);
        assertFalse(trustIndicator.isTrusted());
    }
}
```

## Integration Testing

### Database Integration Tests

**Multi-Module Database Tests**:
```java
public class DatabaseIntegrationTest {
    private DatabaseComponent db;
    
    @Test
    public void testContactAndMessageFlow() throws Exception {
        // Add contact
        Contact contact = createTestContact();
        db.addContact(contact);
        
        // Create group for contact
        Group group = createContactGroup(contact);
        db.addGroup(group);
        
        // Add message to group
        Message message = createTestMessage(group.getId());
        db.addLocalMessage(message, getTestMetadata(), true, false);
        
        // Verify complete flow
        assertTrue(db.containsContact(contact.getId()));
        assertTrue(db.containsGroup(group.getId()));
        assertTrue(db.containsMessage(message.getId()));
        
        // Verify relationships
        Collection<MessageHeader> headers = db.getMessageHeaders(group.getId());
        assertEquals(1, headers.size());
    }
}
```

### Transport Integration Tests

**Plugin Communication Tests**:
```java
public class TransportIntegrationTest {
    private PluginManager pluginManager;
    private MockTransportPlugin plugin1, plugin2;
    
    @Test
    public void testPluginToPluginCommunication() throws Exception {
        // Setup two plugin instances
        plugin1 = new MockTransportPlugin(TRANSPORT_ID);
        plugin2 = new MockTransportPlugin(TRANSPORT_ID);
        
        // Start plugins
        plugin1.start();
        plugin2.start();
        
        // Create connection from plugin1 to plugin2
        DuplexTransportConnection connection = 
            plugin1.createConnection(contactId);
        
        // Send data
        byte[] testData = "test message".getBytes(UTF_8);
        connection.getWriter().write(testData);
        
        // Verify data received by plugin2
        byte[] receivedData = connection.getReader().read();
        assertArrayEquals(testData, receivedData);
    }
}
```

## Security Testing

### Cryptographic Testing

#### 1. Key Management Tests

**File**: `KeyManagementSecurityTest.java`
```java
public class KeyManagementSecurityTest {
    @Test
    public void testKeyGenerationEntropy() {
        Set<String> generatedKeys = new HashSet<>();
        
        // Generate 1000 keys and verify uniqueness
        for (int i = 0; i < 1000; i++) {
            KeyPair keyPair = crypto.generateKeyPair();
            String publicKeyHex = Hex.encodeHexString(keyPair.getPublic().getEncoded());
            
            assertFalse("Duplicate key generated", generatedKeys.contains(publicKeyHex));
            generatedKeys.add(publicKeyHex);
        }
    }
    
    @Test
    public void testSecretKeyZeroization() {
        SecretKey key = crypto.generateSecretKey();
        byte[] keyBytes = key.getEncoded();
        
        // Use key
        crypto.mac("test", key, "data".getBytes(UTF_8));
        
        // Destroy key
        key.destroy();
        
        // Verify key is destroyed
        assertTrue(key.isDestroyed());
    }
}
```

#### 2. Encryption/Decryption Tests

**File**: `EncryptionSecurityTest.java`
```java
public class EncryptionSecurityTest {
    @Test
    public void testEncryptionNonRepeating() {
        byte[] plaintext = "sensitive data".getBytes(UTF_8);
        SecretKey key = crypto.generateSecretKey();
        
        // Encrypt same plaintext multiple times
        byte[] ciphertext1 = crypto.encrypt(plaintext, key);
        byte[] ciphertext2 = crypto.encrypt(plaintext, key);
        
        // Verify ciphertexts are different (due to random nonce)
        assertFalse(Arrays.equals(ciphertext1, ciphertext2));
        
        // Verify both decrypt to same plaintext
        assertArrayEquals(plaintext, crypto.decrypt(ciphertext1, key));
        assertArrayEquals(plaintext, crypto.decrypt(ciphertext2, key));
    }
    
    @Test
    public void testCiphertextIntegrityProtection() {
        byte[] plaintext = "test data".getBytes(UTF_8);
        SecretKey key = crypto.generateSecretKey();
        byte[] ciphertext = crypto.encrypt(plaintext, key);
        
        // Corrupt ciphertext
        ciphertext[ciphertext.length - 1] ^= 1;
        
        // Verify decryption fails
        assertThrows(GeneralSecurityException.class, () -> {
            crypto.decrypt(ciphertext, key);
        });
    }
}
```

### Vulnerability Testing

#### 1. Input Validation Tests

**File**: `InputValidationTest.java`
```java
public class InputValidationTest {
    @Test
    public void testMessageLengthValidation() {
        // Test maximum message length
        String maxLengthMessage = StringUtils.repeat("a", MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
        assertDoesNotThrow(() -> {
            validator.validateMessage(maxLengthMessage);
        });
        
        // Test oversized message
        String oversizedMessage = StringUtils.repeat("a", MAX_PRIVATE_MESSAGE_TEXT_LENGTH + 1);
        assertThrows(FormatException.class, () -> {
            validator.validateMessage(oversizedMessage);
        });
    }
    
    @Test
    public void testSqlInjectionResistance() {
        // Test malicious contact names
        String[] maliciousInputs = {
            "'; DROP TABLE contacts; --",
            "1' OR '1'='1",
            "\\x00\\x01\\x02"
        };
        
        for (String maliciousInput : maliciousInputs) {
            Contact contact = createTestContact();
            contact.setAlias(maliciousInput);
            
            // Should not throw exception or corrupt database
            assertDoesNotThrow(() -> {
                db.addContact(contact);
                db.getContact(contact.getId());
            });
        }
    }
}
```

## Performance Testing

### Load Testing

**File**: `LoadTest.java`
```java
public class LoadTest {
    @Test
    public void testHighMessageVolume() throws Exception {
        final int MESSAGE_COUNT = 10000;
        final int CONCURRENT_THREADS = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(MESSAGE_COUNT);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            final int messageIndex = i;
            executor.submit(() -> {
                try {
                    Message message = createTestMessage("Message " + messageIndex);
                    db.addLocalMessage(message, getTestMetadata(), true, false);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(60, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        
        // Verify performance: should handle 10k messages in under 30 seconds
        assertTrue("Load test took too long: " + duration + "ms", duration < 30000);
        
        // Verify all messages were stored
        assertEquals(MESSAGE_COUNT, db.getMessageCount());
    }
}
```

### Memory Testing

**File**: `MemoryTest.java`
```java
public class MemoryTest {
    @Test
    public void testMemoryLeakPrevention() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Create and destroy many objects
        for (int i = 0; i < 1000; i++) {
            Contact contact = createTestContact();
            db.addContact(contact);
            db.removeContact(contact.getId());
            
            if (i % 100 == 0) {
                System.gc(); // Suggest garbage collection
                Thread.yield();
            }
        }
        
        System.gc();
        Thread.sleep(1000); // Allow GC to complete
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be minimal (< 10MB)
        assertTrue("Memory leak detected: " + memoryIncrease + " bytes", 
                  memoryIncrease < 10 * 1024 * 1024);
    }
}
```

## Test Data Management

### Test Fixtures

**File**: `TestDataFactory.java`
```java
public class TestDataFactory {
    private static final SecureRandom random = new SecureRandom();
    
    public static Contact createTestContact() {
        return createTestContact("Test Contact " + random.nextInt(1000));
    }
    
    public static Contact createTestContact(String name) {
        AuthorId authorId = new AuthorId(getRandomId());
        Author author = new Author(authorId, name, getRandomBytes(32));
        AuthorId localAuthorId = new AuthorId(getRandomId());
        
        return new Contact(new ContactId(getRandomId()), author, localAuthorId, true);
    }
    
    public static Message createTestMessage() {
        return createTestMessage("Test message content");
    }
    
    public static Message createTestMessage(String content) {
        GroupId groupId = new GroupId(getRandomId());
        MessageId messageId = new MessageId(getRandomId());
        long timestamp = System.currentTimeMillis();
        
        return new Message(messageId, groupId, timestamp, content.getBytes(UTF_8));
    }
    
    public static byte[] getRandomId() {
        byte[] id = new byte[32];
        random.nextBytes(id);
        return id;
    }
}
```

### Database Test Utilities

**File**: `DatabaseTestHelper.java`
```java
public class DatabaseTestHelper {
    public static DatabaseConfig createTestDatabaseConfig() {
        return new H2DatabaseConfig(
            "jdbc:h2:mem:test" + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1",
            "test",
            "test"
        );
    }
    
    public static void cleanDatabase(Database db) throws DbException {
        db.transaction(false, txn -> {
            // Remove test data in proper order
            db.removeAllMessages();
            db.removeAllGroups();
            db.removeAllContacts();
            db.removeAllSettings();
        });
    }
}
```

## Continuous Integration Testing

### Automated Test Execution

**GitHub Actions Configuration** (`.github/workflows/test.yml`):
```yaml
name: Test Suite

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
    
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: ~/.gradle/caches
        key: gradle-${{ hashFiles('**/*.gradle') }}
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Run Android lint
      run: ./gradlew :mycel-android:lint
    
    - name: Build debug APK
      run: ./gradlew :mycel-android:assembleDebug
    
    - name: Upload test reports
      uses: actions/upload-artifact@v3
      if: failure()
      with:
        name: test-reports
        path: '**/build/reports/tests/'
```

### Test Reporting

**Coverage Reports**:
```gradle
// build.gradle
apply plugin: 'jacoco'

jacoco {
    toolVersion = "0.8.8"
}

jacocoTestReport {
    reports {
        xml.enabled true
        html.enabled true
        csv.enabled false
    }
}
```

## Testing for Mycel Rebranding

### Rebranding Test Strategy

#### 1. String Resource Validation
```java
@Test
public void testBrandingConsistency() {
    // Verify no "briar" references in user-facing strings
    Resources resources = context.getResources();
    String[] stringNames = resources.getStringArray(R.array.all_string_names);
    
    for (String stringName : stringNames) {
        int resourceId = resources.getIdentifier(stringName, "string", packageName);
        String value = resources.getString(resourceId);
        
        assertFalse("Briar reference found in string: " + stringName, 
                   value.toLowerCase().contains("briar"));
    }
}
```

#### 2. Package Name Validation
```java
@Test
public void testPackageNameConsistency() {
    String packageName = BuildConfig.APPLICATION_ID;
    assertTrue("Package should be com.quantumresearch.mycel", 
              packageName.startsWith("com.quantumresearch.mycel"));
}
```

#### 3. Asset Validation
```java
@Test
public void testAssetConsistency() {
    // Verify logo assets exist
    assertNotNull("App icon missing", 
                 context.getDrawable(R.mipmap.ic_launcher));
    assertNotNull("Splash logo missing", 
                 context.getDrawable(R.drawable.logo_circle));
}
```

This comprehensive testing strategy ensures robust quality assurance throughout development and provides specific validation for the Mycel rebranding process.