# Cryptographic Implementation and Security Architecture

## Overview

The Briar/Mycel application implements a comprehensive, multi-layered security architecture designed for secure peer-to-peer communication. It emphasizes forward secrecy, contact authentication, and resistance to various attack vectors using modern cryptographic primitives.

## Cryptographic Algorithms

### Core Cryptographic Primitives

#### Elliptic Curve Cryptography
- **Curve25519**: Key agreement (Diffie-Hellman key exchange)
- **Ed25519**: Digital signatures (256-bit keys, 64-byte signatures)
- **brainpoolp512r1**: Message encryption (SEC1 encoding, 512-bit keys)

#### Symmetric Cryptography
- **XSalsa20**: Stream cipher for transport encryption
- **Poly1305**: Message authentication (MAC)
- **AES-256-CBC**: Message-level encryption with PKCS padding

#### Hash Functions
- **Blake2b** (256-bit): Primary hash for key derivation and MACs
- **SHA-256**: Key derivation in message encryption
- **SHA-3** (256-bit): Onion v3 address encoding

#### Key Derivation
- **Scrypt**: Password-based key derivation with adaptive costs
- **Custom KDF**: Blake2b-based for general key derivation

### Security Parameters

```java
// Key sizes and cryptographic parameters
SECRET_KEY_BYTES = 32;           // 256-bit secret keys
AGREEMENT_KEY_BYTES = 32;        // Curve25519 keys  
SIGNATURE_KEY_BYTES = 32;        // Ed25519 keys
MESSAGE_KEY_BYTES = 64;          // brainpoolp512r1 keys
MAC_BYTES = 16;                  // 128-bit MAC
NONCE_BYTES = 24;                // XSalsa20 nonce length
```

## Key Management Architecture

### Key Generation

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/crypto/`

**Secure Random Generation**:
```java
// Platform-specific secure random with fallback
Provider provider = secureRandomProvider.getProvider();
if (provider != null) {
    installSecureRandomProvider(provider);
}
secureRandom = new SecureRandom();
```

### Key Types and Hierarchy

#### Static Keys
- **Identity Keys**: Long-term Ed25519 signature keypairs
- **Handshake Keys**: Curve25519 agreement keypairs for contact exchange

#### Ephemeral Keys  
- **Session Keys**: Generated per communication session
- **Forward Secrecy**: Automatically rotated, previous keys discarded

#### Transport Keys
```java
public class TransportKeys {
    private final SecretKey incomingTagKey;      // Authentication tag key
    private final SecretKey incomingHeaderKey;   // Header encryption key
    private final SecretKey outgoingTagKey;      // Authentication tag key
    private final SecretKey outgoingHeaderKey;   // Header encryption key
    private final boolean rotationMode;          // Key rotation enabled
}
```

### Key Rotation Mechanism

**Time-Based Rotation**:
```java
public TransportKeys deriveRotationKeys(TransportId t, SecretKey rootKey, 
        long timePeriod, boolean weAreAlice, boolean active) {
    // Derive keys for previous, current, and next time periods
    SecretKey inTagPrev = deriveTagKey(rootKey, t, !weAreAlice);
    SecretKey inHeaderPrev = deriveHeaderKey(rootKey, t, !weAreAlice);
    
    // Rotate keys based on time period
    SecretKey inTagCurr = rotateKey(inTagPrev, timePeriod);
    SecretKey inHeaderCurr = rotateKey(inHeaderPrev, timePeriod);
    
    return new TransportKeys(inTagCurr, inHeaderCurr, 
                           outTagCurr, outHeaderCurr, true);
}
```

**Hierarchical Key Structure**:
```
Static Master Key
├── Handshake Root Key (for pending contacts)
├── Contact Root Key (for verified contacts)
    ├── Alice Tag Keys (time-period based)
    ├── Alice Header Keys (time-period based)
    ├── Bob Tag Keys (time-period based)
    └── Bob Header Keys (time-period based)
```

## Transport Layer Security (BTP)

### Briar Transport Protocol

**Frame-Based Encryption**:
- Each frame encrypted with XSalsa20-Poly1305
- Separate encryption for frame headers and payloads
- Frame counter prevents replay attacks

**Protocol Structure**:
```
[Tag (16 bytes)] [Stream Header] [Frame 1] [Frame 2] ... [Frame N]
```

**Stream Header Format**:
```java
public class StreamHeader {
    private final int protocolVersion;    // 2 bytes
    private final long streamNumber;      // 8 bytes
    private final SecretKey frameKey;     // 32 bytes (encrypted)
}
```

### Perfect Forward Secrecy Implementation

**Two-Phase Key Agreement**:
```java
// Phase 1: Static key agreement
SecretKey staticMasterKey = deriveStaticMasterKey(
    theirHandshakePublicKey, ourHandshakeKeyPair);

// Phase 2: Ephemeral key agreement (forward secrecy)
SecretKey masterKey = deriveSharedSecret(label, 
    theirStaticPublicKey, theirEphemeralPublicKey,
    ourStaticKeyPair, ourEphemeralKeyPair, alice, inputs);
```

**Modern Key Agreement (Forward Secrecy)**:
```java
// Ephemeral-ephemeral agreement first (strongest security)
hashInputs[0] = performRawKeyAgreement(ourEphemeralPrivateKey, 
    theirEphemeralPublicKey);
// Followed by static-ephemeral agreements
hashInputs[1] = performRawKeyAgreement(ourStaticPrivateKey, 
    theirEphemeralPublicKey);
hashInputs[2] = performRawKeyAgreement(ourEphemeralPrivateKey, 
    theirStaticPublicKey);
```

## Message-Level Security

### Hybrid Encryption Scheme

**Integrated Encryption Scheme (IES)**:
- **ECDH**: Key agreement using brainpoolp512r1
- **KDF2**: Key derivation with SHA-256
- **AES-256-CBC**: Symmetric encryption with PKCS padding
- **HMAC-SHA256**: Message authentication

**Message Encryption Process**:
```java
public byte[] encrypt(byte[] plaintext, PublicKey publicKey, String label) {
    // Generate ephemeral keypair
    KeyPair ephemeralKeyPair = generateKeyPair();
    
    // Perform ECDH key agreement
    SecretKey sharedSecret = performKeyAgreement(
        ephemeralKeyPair.getPrivate(), publicKey);
    
    // Derive encryption and MAC keys
    byte[] keys = kdf2(sharedSecret, label, AES_KEY_BYTES + MAC_KEY_BYTES);
    SecretKey encryptionKey = new SecretKey(keys, 0, AES_KEY_BYTES);
    SecretKey macKey = new SecretKey(keys, AES_KEY_BYTES, MAC_KEY_BYTES);
    
    // Encrypt and authenticate
    byte[] ciphertext = aesEncrypt(plaintext, encryptionKey);
    byte[] mac = hmacSha256(ciphertext, macKey);
    
    return concatenate(ephemeralKeyPair.getPublic().getEncoded(), 
                      ciphertext, mac);
}
```

## Contact Authentication and Verification

### Contact Exchange Protocol

**Cryptographic Identity Binding**:
```java
public byte[] sign(PrivateKey privateKey, SecretKey masterKey, boolean alice) {
    // Derive protocol-specific nonce to prevent replay
    byte[] nonce = deriveNonce(masterKey, alice);
    
    // Sign nonce with long-term private key
    return crypto.sign(SIGNING_LABEL, nonce, privateKey);
}
```

**Handshake Authentication**:
```java
public byte[] proveOwnership(SecretKey masterKey, boolean alice) {
    String label = alice ? ALICE_PROOF_LABEL : BOB_PROOF_LABEL;
    return crypto.mac(label, masterKey);
}
```

### Role Determination

**Deterministic Alice/Bob Assignment**:
```java
public boolean isAlice(PublicKey theirHandshakePublicKey, 
                      KeyPair ourHandshakeKeyPair) {
    byte[] theirPublic = theirHandshakePublicKey.getEncoded();
    byte[] ourPublic = ourHandshakeKeyPair.getPublic().getEncoded();
    
    // Lexicographic comparison for deterministic role assignment
    return compare(ourPublic, theirPublic) < 0;
}
```

## Password-Based Security

### Scrypt Key Derivation

**Location**: `bramble-core/src/main/java/org/briarproject/bramble/crypto/ScryptKdf.java`

**Adaptive Cost Parameters**:
```java
public int chooseCostParameter() {
    long maxMemory = Runtime.getRuntime().maxMemory();
    long maxCost = min(MAX_COST, maxMemory / BLOCK_SIZE / 256);
    
    int cost = MIN_COST; // 256
    while (cost * 2 <= maxCost && measureDuration(cost) * 2 <= TARGET_MS) {
        cost *= 2; // Double cost until reaching time/memory limit
    }
    return cost;
}
```

**Security Parameters**:
```java
// Scrypt parameters
private static final int MIN_COST = 256;
private static final int MAX_COST = 1_048_576; // 2^20
private static final int BLOCK_SIZE = 8;        // r parameter
private static final int PARALLELIZATION = 1;  // p parameter
private static final long TARGET_MS = 1000;    // 1 second target time
```

### Password Encryption Format

**Ciphertext Structure**:
```
[Format Version (1 byte)] [Salt (32 bytes)] [Cost (4 bytes)] 
[IV (24 bytes)] [Ciphertext] [MAC (16 bytes)]
```

**Format Versions**:
- **Version 0**: Standard Scrypt
- **Version 1**: Scrypt with hardware key strengthening

## Database Security

### Hardware Key Strengthening

**Location**: `bramble-android/src/main/java/org/briarproject/bramble/keyagreement/KeyStrengthenerImpl.java`

**Android Keystore Integration**:
```java
public SecretKey strengthenKey(SecretKey k) {
    if (!isInitialised()) initialise();
    
    Mac mac = Mac.getInstance(KEY_ALGORITHM_HMAC_SHA256);
    mac.init(storedKey); // Key stored in Android Keystore
    
    return new SecretKey(mac.doFinal(k.getBytes()));
}
```

**StrongBox Support**:
- Uses hardware security module when available
- Falls back to software-based TEE (Trusted Execution Environment)
- HMAC-SHA256 based key strengthening

### Database Encryption

**Two-Layer Security**:
1. **Password-based encryption** of database key using Scrypt
2. **Hardware key strengthening** (Android Keystore when available)
3. **Separate key storage** from database data

**Database Configuration**:
```java
public interface DatabaseConfig {
    File getDatabaseDirectory();        // Database location
    File getDatabaseKeyDirectory();     // Key storage location
    KeyStrengthener getKeyStrengthener(); // Hardware integration
}
```

## Security Audit and Compliance

### Secure Coding Practices

**Constant-Time Operations**:
```java
// Side-channel resistant comparisons
public static boolean constantTimeEquals(byte[] a, byte[] b) {
    if (a.length != b.length) return false;
    
    int result = 0;
    for (int i = 0; i < a.length; i++) {
        result |= a[i] ^ b[i];
    }
    return result == 0;
}
```

**Secure Memory Management**:
- Explicit key zeroing after use
- Limited key lifetime in memory
- Secure random number generation

### Logging Security Measures

**Limited Cryptographic Logging**:
```java
// Only log metadata, never key material
if (LOG.isLoggable(INFO)) {
    LOG.info("KDF cost parameter " + cost);
}

logDuration(LOG, "Deriving key from password", start);
```

**Security Events Tracked**:
- Key generation and initialization
- Provider installation success/failure
- KDF cost parameter selection  
- Keystore operations (load/store)

## Security Analysis

### Implemented Security Measures

1. **Defense in Depth**: Multiple encryption layers (transport + message)
2. **Forward Secrecy**: Ephemeral keys with automatic rotation
3. **Perfect Authentication**: Cryptographic proof of identity
4. **Replay Protection**: Sequence numbers and nonces
5. **Hardware Integration**: Keystore utilization where available
6. **Adaptive Security**: Dynamic KDF cost adjustment
7. **Domain Separation**: Unique labels for cryptographic operations
8. **Protocol Versioning**: Support for cryptographic agility

### Attack Resistance

**Traffic Analysis Resistance**:
- Uniform packet sizing with padding
- Consistent traffic patterns
- Onion routing integration (Tor)

**Side-Channel Resistance**:
- Constant-time cryptographic operations
- Secure memory management
- Limited timing information leakage

**Cryptographic Agility**:
- Support for algorithm upgrades
- Protocol versioning system
- Migration path for new primitives

## Rebranding Security Implications

### Security-Neutral Changes
- Application names and branding have no impact on cryptographic security
- Database schemas and key formats remain unchanged
- Transport protocols maintain compatibility

### Security Configuration Updates
- Certificate pinning for new domains
- App Store signing certificates
- Deep link security validation
- Network security configuration updates

### Recommended Security Validations
1. **Cryptographic Testing**: Verify all security functions after rebranding
2. **Protocol Compatibility**: Ensure transport protocols remain functional
3. **Key Management**: Validate database encryption continues working
4. **Certificate Validation**: Update and test new signing certificates
5. **Security Audit**: Review changes for unintended security implications

The cryptographic architecture is designed to be brand-agnostic, with all security-critical components isolated from application branding elements.