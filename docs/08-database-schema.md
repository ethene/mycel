# Database Schema and Architecture

## Overview

The Briar/Mycel database uses a sophisticated JDBC-based architecture designed for secure, encrypted peer-to-peer messaging. It supports multiple database backends (H2, HyperSQL) with comprehensive encryption, versioning, and migration capabilities.

## Database Backends

### Primary Backend: H2 Database
- **Location**: `spore-core/src/main/java/org/briarproject/bramble/db/h2/`
- **Encryption**: AES cipher with split files (`CIPHER=AES`)
- **Version**: H2 1.4.192 (legacy version for Java compatibility)
- **Features**: Multi-threaded, file-based storage

### Alternative Backend: HyperSQL Database  
- **Location**: `spore-core/src/main/java/org/briarproject/bramble/db/hsql/`
- **Encryption**: LOB encryption (`encrypt_lobs=true;crypt_type=AES`)
- **Features**: Standards-compliant SQL, compact shutdown

## Schema Version Management

- **Current Schema Version**: 50
- **Location**: `DatabaseConstants.CODE_SCHEMA_VERSION = 50`
- **Migration Range**: Supports migrations from version 38 to 50
- **Migration Files**: Incremental SQL scripts in database implementation

## Core Database Tables

### 1. Identity and Contact Management

#### localAuthors
```sql
CREATE TABLE localAuthors (
    authorId BINARY(32) NOT NULL,           -- Unique author identifier
    formatVersion INT NOT NULL,             -- Author format version
    name VARCHAR NOT NULL,                  -- Display name
    publicKey BINARY NOT NULL,              -- Ed25519 public key
    privateKey BINARY NOT NULL,             -- Ed25519 private key
    handshakePublicKey BINARY,              -- Contact exchange key (nullable)
    handshakePrivateKey BINARY,             -- Contact exchange key (nullable)
    created BIGINT NOT NULL,                -- Creation timestamp
    PRIMARY KEY (authorId)
)
```

#### contacts
```sql
CREATE TABLE contacts (
    contactId INT NOT NULL AUTO_INCREMENT,  -- Internal contact ID
    authorId BINARY(32) NOT NULL,           -- Contact's author ID
    formatVersion INT NOT NULL,             -- Contact format version
    name VARCHAR NOT NULL,                  -- Contact's name
    alias VARCHAR,                          -- User-defined alias
    publicKey BINARY NOT NULL,              -- Contact's public key
    handshakePublicKey BINARY,              -- Handshake public key
    localAuthorId BINARY(32) NOT NULL,      -- Owner's author ID
    verified BOOLEAN NOT NULL,              -- Verification status
    syncVersions BINARY DEFAULT '00' NOT NULL, -- Sync protocol versions
    PRIMARY KEY (contactId),
    FOREIGN KEY (localAuthorId) REFERENCES localAuthors (authorId)
)
```

#### pendingContacts
```sql
CREATE TABLE pendingContacts (
    pendingContactId BINARY(32) NOT NULL,   -- Pending contact ID
    publicKey BINARY NOT NULL,              -- Contact's public key
    alias VARCHAR NOT NULL,                 -- Temporary alias
    timestamp BIGINT NOT NULL,              -- Creation time
    PRIMARY KEY (pendingContactId)
)
```

### 2. Group and Conversation Management

#### groups
```sql
CREATE TABLE groups (
    groupId BINARY(32) NOT NULL,            -- Unique group identifier
    clientId VARCHAR NOT NULL,              -- Client application ID
    majorVersion INT NOT NULL,              -- Client major version
    descriptor BINARY NOT NULL,             -- Group descriptor
    PRIMARY KEY (groupId)
)
```

#### groupMetadata
```sql
CREATE TABLE groupMetadata (
    groupId BINARY(32) NOT NULL,            -- Reference to group
    metaKey VARCHAR NOT NULL,               -- Metadata key
    value BINARY NOT NULL,                  -- Metadata value
    PRIMARY KEY (groupId, metaKey),
    FOREIGN KEY (groupId) REFERENCES groups (groupId)
)
```

#### groupVisibilities
```sql
CREATE TABLE groupVisibilities (
    contactId INT NOT NULL,                 -- Contact reference
    groupId BINARY(32) NOT NULL,            -- Group reference
    shared BOOLEAN NOT NULL,                -- Sharing status
    PRIMARY KEY (contactId, groupId),
    FOREIGN KEY (contactId) REFERENCES contacts (contactId),
    FOREIGN KEY (groupId) REFERENCES groups (groupId)
)
```

### 3. Message Storage and Management

#### messages
```sql
CREATE TABLE messages (
    messageId BINARY(32) NOT NULL,          -- Unique message ID
    groupId BINARY(32) NOT NULL,            -- Parent group
    timestamp BIGINT NOT NULL,              -- Message timestamp
    state INT NOT NULL,                     -- Validation state
    shared BOOLEAN NOT NULL,                -- Sharing status
    temporary BOOLEAN NOT NULL,             -- Temporary message flag
    cleanupTimerDuration BIGINT,            -- Auto-delete duration
    cleanupDeadline BIGINT,                 -- Deletion deadline
    length INT NOT NULL,                    -- Message length
    raw BLOB,                               -- Message content (nullable)
    PRIMARY KEY (messageId),
    FOREIGN KEY (groupId) REFERENCES groups (groupId)
)
```

#### messageMetadata
```sql
CREATE TABLE messageMetadata (
    messageId BINARY(32) NOT NULL,          -- Message reference
    groupId BINARY(32) NOT NULL,            -- Denormalized group ID
    state INT NOT NULL,                     -- Denormalized state
    metaKey VARCHAR NOT NULL,               -- Metadata key
    value BINARY NOT NULL,                  -- Metadata value
    PRIMARY KEY (messageId, metaKey),
    FOREIGN KEY (messageId) REFERENCES messages (messageId),
    FOREIGN KEY (groupId) REFERENCES groups (groupId)
)
```

#### messageDependencies
```sql
CREATE TABLE messageDependencies (
    groupId BINARY(32) NOT NULL,            -- Group reference
    messageId BINARY(32) NOT NULL,          -- Message reference
    dependencyId BINARY(32) NOT NULL,       -- Dependency message ID
    messageState INT NOT NULL,              -- Denormalized message state
    dependencyState INT,                    -- Dependency state (nullable)
    FOREIGN KEY (groupId) REFERENCES groups (groupId),
    FOREIGN KEY (messageId) REFERENCES messages (messageId)
)
```

### 4. Message Status and Delivery Tracking

#### statuses
```sql
CREATE TABLE statuses (
    messageId BINARY(32) NOT NULL,          -- Message reference
    contactId INT NOT NULL,                 -- Contact reference
    groupId BINARY(32) NOT NULL,            -- Denormalized group ID
    timestamp BIGINT NOT NULL,              -- Denormalized timestamp
    length INT NOT NULL,                    -- Denormalized length
    state INT NOT NULL,                     -- Denormalized state
    groupShared BOOLEAN NOT NULL,           -- Denormalized group sharing
    messageShared BOOLEAN NOT NULL,         -- Denormalized message sharing
    deleted BOOLEAN NOT NULL,               -- Denormalized deletion status
    ack BOOLEAN NOT NULL,                   -- Acknowledgment received
    seen BOOLEAN NOT NULL,                  -- Read receipt
    requested BOOLEAN NOT NULL,             -- Request status
    expiry BIGINT NOT NULL,                 -- Retransmission expiry
    txCount INT NOT NULL,                   -- Transmission count
    maxLatency BIGINT,                      -- Transport latency
    PRIMARY KEY (messageId, contactId),
    FOREIGN KEY (messageId) REFERENCES messages (messageId),
    FOREIGN KEY (contactId) REFERENCES contacts (contactId),
    FOREIGN KEY (groupId) REFERENCES groups (groupId)
)
```

### 5. Transport and Cryptographic Management

#### transports
```sql
CREATE TABLE transports (
    transportId VARCHAR NOT NULL,           -- Transport identifier
    maxLatency BIGINT NOT NULL,             -- Maximum latency
    PRIMARY KEY (transportId)
)
```

#### outgoingKeys & incomingKeys
```sql
CREATE TABLE outgoingKeys (
    contactId INT NOT NULL,                 -- Contact reference
    transportId VARCHAR NOT NULL,           -- Transport reference
    keySetId BIGINT NOT NULL,               -- Key set identifier
    rotationPeriod BIGINT NOT NULL,         -- Key rotation period
    tagKey BINARY(16) NOT NULL,             -- Authentication tag key
    headerKey BINARY(32) NOT NULL,          -- Header encryption key
    base BIGINT NOT NULL,                   -- Base stream counter
    bitmap BINARY(8) NOT NULL,              -- Reordering window
    streamCounter BIGINT NOT NULL,          -- Current stream counter
    active BOOLEAN NOT NULL,                -- Key set active status
    PRIMARY KEY (contactId, transportId, keySetId),
    FOREIGN KEY (contactId) REFERENCES contacts (contactId),
    FOREIGN KEY (transportId) REFERENCES transports (transportId)
)

-- Similar structure for incomingKeys with reordering window support
```

### 6. Application Settings

#### settings
```sql
CREATE TABLE settings (
    namespace VARCHAR NOT NULL,             -- Setting namespace
    settingKey VARCHAR NOT NULL,            -- Setting key
    value VARCHAR NOT NULL,                 -- Setting value
    PRIMARY KEY (namespace, settingKey)
)
```

## Database Indexes

Strategic indexes for query performance:

### Primary Indexes
- `contactsByAuthorId` - Fast contact lookup by author
- `groupsByClientIdMajorVersion` - Group lookup by client
- `messagesByCleanupDeadline` - Cleanup job optimization

### Performance Indexes  
- `messageMetadataByGroupIdState` - Message queries by group/state
- `messageDependenciesByDependencyId` - Dependency resolution
- `statusesByContactIdGroupId` - Status queries by contact/group
- `statusesByContactIdTimestamp` - Timeline queries
- `statusesByContactIdTxCountTimestamp` - Transmission tracking

## Message States and Workflows

### Message States
- **UNKNOWN (0)**: Initial state for received messages
- **PENDING (1)**: Awaiting dependency resolution  
- **DELIVERED (2)**: Successfully validated and delivered
- **INVALID (3)**: Failed validation

### Group Visibility States
- **INVISIBLE (0)**: Group not visible to contact
- **VISIBLE (1)**: Group visible but not shared
- **SHARED (2)**: Group visible and actively shared

## Encryption and Security

### Database-Level Encryption
- **H2**: `CIPHER=AES` with split files for security
- **HyperSQL**: `encrypt_lobs=true;crypt_type=AES`
- Database key derived from user password using key strengthening
- All sensitive data encrypted at rest

### Cryptographic Key Storage
- Ed25519 keys for digital signatures
- Transport keys with rotation support
- Handshake keys for contact establishment
- Stream counters for replay protection
- Reordering windows for out-of-order delivery

## Migration System

### Schema Versioning
- Current version: 50 (as of latest analysis)
- Incremental migrations from version 38+
- Migration progress reporting via listeners
- Automatic database compaction after migrations

### Recent Migration Examples
```sql
-- Migration 49→50: Rename eta to maxLatency
ALTER TABLE statuses RENAME COLUMN eta TO maxLatency

-- Migration 48→49: Change maxLatency type
ALTER TABLE transports ALTER COLUMN maxLatency BIGINT

-- Migration 47→48: Add cleanup timers
ALTER TABLE messages ADD COLUMN cleanupTimerDuration BIGINT
ALTER TABLE messages ADD COLUMN cleanupDeadline BIGINT

-- Migration 40→41: Add contact aliases
ALTER TABLE contacts ADD COLUMN alias VARCHAR
```

## Database Component Architecture

### Core Components
- **`Database`** - Low-level database interface
- **`DatabaseComponent`** - High-level API for application layer
- **`JdbcDatabase`** - Generic JDBC implementation
- **`H2Database`** / **`HyperSqlDatabase`** - Backend-specific implementations

### Transaction Management
- ACID transactions with proper isolation levels
- Read/write lock separation for performance
- Connection pooling with configurable limits
- Automatic retry with exponential backoff
- Comprehensive error handling and recovery

### Performance Optimizations
- Extensive denormalization in `statuses` table
- Strategic indexing for common query patterns
- Prepared statements for security and performance
- Batch operations for bulk data changes
- Connection pooling to minimize overhead

## Initialization and Setup

### Database Startup Process
1. Load appropriate JDBC driver (H2 or HyperSQL)
2. Create or open encrypted database file
3. Check dirty flag for unclean shutdown detection
4. Apply any pending schema migrations
5. Create tables and indexes if needed
6. Set dirty flag for crash detection
7. Compact database if migrations applied

### Configuration Options
- Database backend selection (H2 vs HyperSQL)
- Database file location and naming
- Connection pool sizing
- Key strengthening parameters
- Migration progress reporting

## Rebranding Implications

### Database Schema Impact
- **Low Impact**: Core schema structure remains unchanged
- **Configuration Changes**: Database file names and paths may need updating
- **Migration Considerations**: Existing user data compatibility must be preserved

### Key Areas for Mycel Rebranding
1. **Database File Names**: May include "briar" in default names
2. **Settings Namespaces**: Application-specific setting namespaces
3. **Client IDs**: Application identifiers in groups table
4. **Directory Paths**: Default database storage locations

The database schema is designed to be brand-agnostic, with application-specific elements isolated in configuration and metadata rather than core schema structure.