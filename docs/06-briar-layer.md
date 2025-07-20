# Briar Layer - Application Logic Documentation

## Overview

The Briar layer implements the high-level application logic for the messaging application, building upon the Bramble infrastructure layer. It provides messaging, forums, blogs, groups, and other user-facing features while maintaining the security and privacy guarantees of the underlying Bramble protocols.

## Briar Architecture

### Layer Structure

```
┌─────────────────────────────────────────┐
│        User Interface Layer            │  ← briar-android, briar-headless
├─────────────────────────────────────────┤
│         Briar Application Layer         │  ← THIS LAYER
├─────────────────────────────────────────┤
│       briar-core (Implementation)      │  ← Feature Implementation
├─────────────────────────────────────────┤
│        briar-api (Interfaces)          │  ← API Definitions
├─────────────────────────────────────────┤
│          Bramble Layer                  │  ← Infrastructure
└─────────────────────────────────────────┘
```

### Module Dependencies

```
bramble-api + bramble-core (foundation)
    ↓
briar-api (application interfaces)
    ↓
briar-core (application implementation)
    ↓
├── briar-android (Android UI)
└── briar-headless (CLI/REST API)
```

## Briar-API Module

### Core Application Interfaces

**Location**: `briar-api/src/main/java/org/briarproject/briar/api/`

#### Messaging (`messaging/`)

**ConversationManager.java**:
```java
public interface ConversationManager {
    // Send private messages
    void sendPrivateMessage(ContactId contactId, String text, 
        long timestamp, MessageType type) throws DbException;
    
    // Retrieve conversation messages
    Collection<ConversationMessageHeader> getMessageHeaders(ContactId contactId) 
        throws DbException;
    
    // Get conversation with pagination
    ConversationResponse getMessages(ContactId contactId, int count) 
        throws DbException;
    
    // Mark messages as read
    void setReadFlag(ContactId contactId, MessageId messageId, boolean read) 
        throws DbException;
}
```

**PrivateMessageFactory.java**:
```java
public interface PrivateMessageFactory {
    PrivateMessage createPrivateMessage(GroupId groupId, long timestamp, 
        String text) throws FormatException;
    
    PrivateMessage createPrivateMessage(GroupId groupId, long timestamp, 
        String text, List<Attachment> attachments) throws FormatException;
}
```

**Message Types**:
```java
public enum MessageType {
    TEXT,           // Plain text message
    NOTICE,         // System notification
    AUTO_DELETE,    // Auto-deleting message
    DISAPPEARING    // Disappearing message
}

public interface PrivateMessage extends Message {
    String getText();
    MessageType getMessageType();
    List<Attachment> getAttachments();
    @Nullable Long getAutoDeleteTimer();
}
```

#### Forum System (`forum/`)

**ForumManager.java**:
```java
public interface ForumManager {
    // Forum lifecycle management
    Forum addForum(String name) throws DbException;
    void removeForum(GroupId groupId) throws DbException;
    
    // Forum sharing
    void shareForum(GroupId groupId, ContactId contactId) throws DbException;
    
    // Post management
    ForumPostHeader addLocalPost(GroupId groupId, String text, 
        long timestamp, @Nullable MessageId parentId) throws DbException;
    
    // Forum content retrieval
    Collection<ForumPostHeader> getPostHeaders(GroupId groupId) 
        throws DbException;
    
    String getPostText(MessageId messageId) throws DbException;
}
```

**Forum Data Structures**:
```java
public interface Forum {
    GroupId getId();
    String getName();
    long getTimestamp();
}

public interface ForumPost extends Message {
    String getText();
    @Nullable MessageId getParentId();  // For threaded discussions
    Author getAuthor();
}

public interface ForumPostHeader extends MessageHeader {
    String getText();
    @Nullable MessageId getParentId();
    Author getAuthor();
    boolean isRead();
}
```

#### Blog System (`blog/`)

**BlogManager.java**:
```java
public interface BlogManager {
    // Blog management  
    Blog addBlog(Author author) throws DbException;
    void removeBlog(GroupId groupId) throws DbException;
    
    // Blog sharing
    void shareBlog(GroupId groupId, ContactId contactId) throws DbException;
    
    // Post creation and management
    BlogPostHeader addLocalPost(GroupId groupId, String text, 
        long timestamp, @Nullable String title) throws DbException;
    
    // Blog content retrieval
    Collection<BlogPostHeader> getPostHeaders(GroupId groupId) 
        throws DbException;
    
    // RSS feed integration
    void addRssFeed(URL feedUrl) throws DbException;
    Collection<RssFeed> getRssFeeds() throws DbException;
}
```

**Blog Data Structures**:
```java
public interface Blog {
    GroupId getId();
    Author getAuthor();
    boolean isRssFeed();
}

public interface BlogPost extends Message {
    String getText();
    @Nullable String getTitle();
    Author getAuthor();
    long getTimePosted();
}

public interface RssFeed {
    String getUrl();
    String getTitle();
    String getDescription();
    long getLastUpdated();
}
```

#### Private Groups (`privategroup/`)

**PrivateGroupManager.java**:
```java
public interface PrivateGroupManager {
    // Group lifecycle
    PrivateGroup addPrivateGroup(String name, Author creator, 
        byte[] salt) throws DbException;
    void dissolvePrivateGroup(GroupId groupId) throws DbException;
    
    // Member management
    void addMember(GroupId groupId, ContactId contactId) throws DbException;
    void removeMember(GroupId groupId, ContactId contactId) throws DbException;
    Collection<Contact> getMembers(GroupId groupId) throws DbException;
    
    // Group messaging
    void sendGroupMessage(GroupId groupId, String text, long timestamp) 
        throws DbException;
    
    // Invitations
    void sendInvitation(GroupId groupId, ContactId contactId, 
        @Nullable String message) throws DbException;
    void respondToInvitation(ContactId contactId, PrivateGroupId groupId, 
        boolean accept) throws DbException;
}
```

#### Content Sharing (`sharing/`)

**SharingManager.java**:
```java
public interface SharingManager {
    // Share contacts
    void sendContactInvitation(ContactId contactId, ContactId sharedContactId, 
        @Nullable String message) throws DbException;
    
    // Share groups/forums
    void sendGroupInvitation(ContactId contactId, GroupId groupId, 
        @Nullable String message) throws DbException;
        
    // Handle invitations
    void respondToContactInvitation(ContactId contactId, 
        ContactId sharedContactId, boolean accept) throws DbException;
    
    // Get pending invitations
    Collection<ContactInvitation> getContactInvitations() throws DbException;
    Collection<GroupInvitation> getGroupInvitations() throws DbException;
}
```

#### Contact Introduction (`introduction/`)

**IntroductionManager.java**:
```java
public interface IntroductionManager {
    // Introduction protocol
    void makeIntroduction(ContactId contactA, ContactId contactB, 
        @Nullable String message) throws DbException;
    
    // Respond to introduction requests
    void respondToIntroduction(ContactId introducerId, 
        ContactId introduceeId, boolean accept) throws DbException;
    
    // Get introduction requests
    Collection<IntroductionRequest> getIntroductionRequests() 
        throws DbException;
}
```

### Client Architecture

**Client Interface**:
```java
public interface Client {
    ClientId getClientId();
    int getMajorVersion();
    int getMinorVersion();
    
    // Message validation
    void addLocalMessage(Message message) throws DbException;
    MessageValidator<? extends Message> getMessageValidator();
    
    // Group management
    Group createGroup(ClientId clientId, int majorVersion, byte[] descriptor);
}
```

**Client Versioning**:
```java
public class ClientId {
    public static final ClientId MESSAGING = new ClientId("messaging");
    public static final ClientId FORUM = new ClientId("forum");
    public static final ClientId BLOG = new ClientId("blog");
    public static final ClientId PRIVATEGROUP = new ClientId("privategroup");
}
```

## Briar-Core Module

### Core Implementation

**Location**: `briar-core/src/main/java/org/briarproject/briar/`

#### Messaging Implementation (`messaging/`)

**ConversationManagerImpl.java**:
```java
@Singleton
public class ConversationManagerImpl implements ConversationManager {
    private final DatabaseComponent db;
    private final MessageValidator<PrivateMessage> validator;
    private final PrivateMessageFactory messageFactory;
    private final EventBus eventBus;
    
    @Override
    public void sendPrivateMessage(ContactId contactId, String text, 
            long timestamp, MessageType type) throws DbException {
        Contact contact = db.getContact(contactId);
        GroupId groupId = getContactGroup(contact);
        
        // Create private message
        PrivateMessage message = messageFactory.createPrivateMessage(
            groupId, timestamp, text, type);
        
        // Add to database
        db.addLocalMessage(message, getMessageMetadata(type), true, false);
        
        // Broadcast event
        eventBus.broadcast(new ConversationMessageReceivedEvent(message, 
            contactId, true));
    }
    
    @Override
    public Collection<ConversationMessageHeader> getMessageHeaders(
            ContactId contactId) throws DbException {
        Contact contact = db.getContact(contactId);
        GroupId groupId = getContactGroup(contact);
        
        Collection<MessageHeader> headers = db.getMessageHeaders(groupId);
        return headers.stream()
            .map(h -> new ConversationMessageHeaderImpl(h, contactId))
            .collect(toList());
    }
}
```

**PrivateMessageValidator.java**:
```java
@Singleton
public class PrivateMessageValidator implements MessageValidator<PrivateMessage> {
    private final MessageEncoder messageEncoder;
    private final Clock clock;
    
    @Override
    public MessageContext validateMessage(Message message, Group group, 
            BdfList body) throws FormatException {
        // Validate message format
        if (body.size() != 4) throw new FormatException();
        
        String text = body.getString(0);
        long timestamp = body.getLong(1);
        int typeValue = body.getInt(2);
        BdfList attachmentList = body.getOptionalList(3);
        
        // Validate text length
        if (text.length() > MAX_PRIVATE_MESSAGE_TEXT_LENGTH) {
            throw new FormatException();
        }
        
        // Validate timestamp
        if (timestamp < 0 || timestamp > clock.currentTimeMillis()) {
            throw new FormatException();
        }
        
        // Validate message type
        MessageType type = MessageType.valueOf(typeValue);
        if (type == null) throw new FormatException();
        
        // Return validation result
        return new MessageContext(Collections.emptyList());
    }
}
```

#### Forum Implementation (`forum/`)

**ForumManagerImpl.java**:
```java
@Singleton
public class ForumManagerImpl implements ForumManager {
    private final DatabaseComponent db;
    private final ForumPostFactory postFactory;
    private final IdentityManager identityManager;
    
    @Override
    public Forum addForum(String name) throws DbException {
        // Validate forum name
        if (name.length() > MAX_FORUM_NAME_LENGTH) {
            throw new IllegalArgumentException();
        }
        
        // Create forum group
        Author author = identityManager.getLocalAuthor();
        Group group = forumGroupFactory.createGroup(name, author);
        
        // Store in database
        db.addGroup(group);
        
        Forum forum = new ForumImpl(group.getId(), name, 
            clock.currentTimeMillis());
        
        // Broadcast event
        eventBus.broadcast(new ForumAddedEvent(forum));
        
        return forum;
    }
    
    @Override
    public ForumPostHeader addLocalPost(GroupId groupId, String text, 
            long timestamp, @Nullable MessageId parentId) throws DbException {
        // Create forum post
        Author author = identityManager.getLocalAuthor();
        ForumPost post = postFactory.createPost(groupId, timestamp, 
            parentId, author, text);
        
        // Add to database
        MessageMetadata metadata = getPostMetadata(parentId);
        db.addLocalMessage(post, metadata, true, false);
        
        ForumPostHeader header = new ForumPostHeaderImpl(post);
        
        // Broadcast event
        eventBus.broadcast(new ForumPostAddedEvent(header, true));
        
        return header;
    }
}
```

#### Blog Implementation (`blog/`)

**BlogManagerImpl.java**:
```java
@Singleton
public class BlogManagerImpl implements BlogManager {
    private final DatabaseComponent db;
    private final BlogPostFactory postFactory;
    private final RssFeedManager rssFeedManager;
    
    @Override
    public BlogPostHeader addLocalPost(GroupId groupId, String text, 
            long timestamp, @Nullable String title) throws DbException {
        // Validate post content
        if (text.length() > MAX_BLOG_POST_TEXT_LENGTH) {
            throw new IllegalArgumentException();
        }
        
        // Create blog post
        Author author = identityManager.getLocalAuthor();
        BlogPost post = postFactory.createBlogPost(groupId, timestamp, 
            title, text, author);
        
        // Add to database with metadata
        MessageMetadata metadata = getBlogPostMetadata(title);
        db.addLocalMessage(post, metadata, true, false);
        
        BlogPostHeader header = new BlogPostHeaderImpl(post);
        
        // Broadcast event
        eventBus.broadcast(new BlogPostAddedEvent(header, true));
        
        return header;
    }
    
    @Override
    public void addRssFeed(URL feedUrl) throws DbException {
        // Validate RSS feed URL
        RssFeed feed = rssFeedManager.importFeed(feedUrl);
        
        // Create blog for RSS feed
        Author feedAuthor = createRssFeedAuthor(feed);
        Blog blog = addBlog(feedAuthor);
        
        // Mark as RSS feed
        setBlogRssFeed(blog.getId(), true);
        
        // Import existing posts
        rssFeedManager.updateFeed(blog.getId(), feed);
    }
}
```

#### Private Group Implementation (`privategroup/`)

**PrivateGroupManagerImpl.java**:
```java
@Singleton
public class PrivateGroupManagerImpl implements PrivateGroupManager {
    private final DatabaseComponent db;
    private final PrivateGroupFactory groupFactory;
    private final InvitationManager<PrivateGroup, PrivateGroupInvitation> 
        invitationManager;
    
    @Override
    public PrivateGroup addPrivateGroup(String name, Author creator, 
            byte[] salt) throws DbException {
        // Create private group
        PrivateGroup privateGroup = groupFactory.createPrivateGroup(
            name, creator, salt);
        
        // Add to database
        db.addGroup(privateGroup.getGroup());
        
        // Add creator as member
        addMember(privateGroup.getId(), getContactId(creator));
        
        // Broadcast event
        eventBus.broadcast(new PrivateGroupAddedEvent(privateGroup));
        
        return privateGroup;
    }
    
    @Override
    public void sendInvitation(GroupId groupId, ContactId contactId, 
            @Nullable String message) throws DbException {
        // Get private group
        PrivateGroup privateGroup = getPrivateGroup(groupId);
        
        // Create invitation
        PrivateGroupInvitation invitation = invitationFactory
            .createInvitation(privateGroup, contactId, message);
        
        // Send via invitation manager
        invitationManager.sendInvitation(invitation);
    }
}
```

### Message Validation Framework

**MessageValidator Interface**:
```java
public interface MessageValidator<T extends Message> {
    MessageContext validateMessage(Message message, Group group, 
        BdfList body) throws FormatException;
}

public class MessageContext {
    private final Collection<MessageId> dependencies;
    private final Collection<MessageId> dependents;
    
    public MessageContext(Collection<MessageId> dependencies) {
        this.dependencies = dependencies;
        this.dependents = Collections.emptyList();
    }
}
```

**Validation Pipeline**:
```java
@Singleton
public class ValidationManagerImpl implements ValidationManager {
    private final Map<ClientId, MessageValidator<?>> validators;
    
    @Override
    public MessageContext validateMessage(Message message, Group group) 
            throws FormatException {
        ClientId clientId = group.getClientId();
        MessageValidator<?> validator = validators.get(clientId);
        
        if (validator == null) {
            throw new FormatException("No validator for client " + clientId);
        }
        
        // Decode message body
        BdfList body = messageEncoder.decode(message.getBody());
        
        // Validate message
        return validator.validateMessage(message, group, body);
    }
}
```

### Event System Integration

**Application Events**:
```java
// Messaging events
public class ConversationMessageReceivedEvent implements Event {
    private final Message message;
    private final ContactId contactId;
    private final boolean local;
}

// Forum events  
public class ForumPostAddedEvent implements Event {
    private final ForumPostHeader postHeader;
    private final boolean local;
}

// Blog events
public class BlogPostAddedEvent implements Event {
    private final BlogPostHeader postHeader;
    private final boolean local;
}

// Group events
public class PrivateGroupAddedEvent implements Event {
    private final PrivateGroup privateGroup;
}

public class GroupInvitationReceivedEvent implements Event {
    private final GroupInvitation invitation;
}
```

### Data Synchronization

**Group Factories**:
```java
public interface GroupFactory {
    Group createGroup(ClientId clientId, int majorVersion, 
        byte[] descriptor);
}

@Singleton
public class GroupFactoryImpl implements GroupFactory {
    private final CryptoComponent crypto;
    
    @Override
    public Group createGroup(ClientId clientId, int majorVersion, 
            byte[] descriptor) {
        // Generate deterministic group ID
        GroupId groupId = new GroupId(crypto.hash(LABEL, 
            clientId.getBytes(), 
            intToBytes(majorVersion), 
            descriptor));
        
        return new Group(groupId, clientId, majorVersion, descriptor);
    }
}
```

## Client Registration and Management

### Client Registry

**ClientManagerImpl.java**:
```java
@Singleton
public class ClientManagerImpl implements ClientManager {
    private final Map<ClientId, Client> clients;
    private final Map<ClientId, ClientVersions> supportedVersions;
    
    @Inject
    public ClientManagerImpl(Set<Client> clients) {
        this.clients = new HashMap<>();
        this.supportedVersions = new HashMap<>();
        
        for (Client client : clients) {
            this.clients.put(client.getClientId(), client);
            this.supportedVersions.put(client.getClientId(), 
                new ClientVersions(client.getMajorVersion(), 
                               client.getMinorVersion()));
        }
    }
    
    @Override
    public Client getClient(ClientId clientId) {
        return clients.get(clientId);
    }
}
```

### Dependency Injection Configuration

**BriarModule.java**:
```java
@Module
public class BriarModule {
    @Provides
    @IntoSet
    Client provideMessagingClient(MessagingClient client) {
        return client;
    }
    
    @Provides
    @IntoSet
    Client provideForumClient(ForumClient client) {
        return client;
    }
    
    @Provides
    @IntoSet
    Client provideBlogClient(BlogClient client) {
        return client;
    }
    
    @Provides
    @IntoSet
    Client providePrivateGroupClient(PrivateGroupClient client) {
        return client;
    }
}
```

## Data Persistence and Caching

### Metadata Management

**MetadataEncoder/Decoder**:
```java
public interface MetadataEncoder {
    BdfDictionary encode(MessageMetadata metadata);
}

public interface MetadataDecoder<T extends MessageMetadata> {
    T decode(BdfDictionary dictionary) throws FormatException;
}
```

### Content Indexing

**Search and Indexing**:
```java
public interface SearchManager {
    // Full-text search across all content
    Collection<SearchResult> search(String query, int maxResults) 
        throws DbException;
    
    // Search within specific content types
    Collection<ConversationMessageHeader> searchMessages(ContactId contactId, 
        String query) throws DbException;
    
    Collection<ForumPostHeader> searchForumPosts(GroupId groupId, 
        String query) throws DbException;
}
```

## Rebranding Implications for Briar Layer

### High Impact Areas

1. **Package Names**: 
   - `org.briarproject.briar.*` → `com.quantumresearch.mycel.app.*`
   - All import statements and references

2. **Client IDs**: 
   - May need updating if they contain "briar" references
   - Database storage of client identifiers

3. **User-Facing Strings**:
   - Default group names and descriptions
   - System message templates
   - Error messages and notifications

### Moderate Impact Areas

1. **Configuration Values**:
   - Default blog/forum names
   - RSS feed user agents
   - System-generated content

2. **Logging and Debugging**:
   - Log messages containing "briar"
   - Debug identifiers and labels

### Low Impact Areas

1. **Core Algorithms**: Message validation and sync protocols
2. **Database Schema**: Table structures and relationships  
3. **Cryptographic Operations**: Security implementations

### Mycel Implementation Strategy

1. **Systematic Package Renaming**: Update all package declarations
2. **Client ID Review**: Check for hardcoded briar references
3. **String Resource Audit**: Review all user-facing text
4. **Configuration Update**: Update default values and templates
5. **Testing Validation**: Ensure all features work post-rebranding

The Briar application layer provides rich messaging and collaboration features that will seamlessly transition to Mycel with primarily cosmetic changes, maintaining all functional capabilities and security properties.