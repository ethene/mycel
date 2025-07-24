# Mycel Class Renaming Reference - Spore Infrastructure

## 🎯 **Naming Strategy Overview**

**Biological Metaphor:**
- **Spore** = Infrastructure layer (networking, crypto, transport) - *was Bramble*
- **Mycel** = Application layer (messaging, UI, features) - *was Briar*

**Package Structure:**
```
com.quantumresearch.mycel.spore.*     (infrastructure - was org.briarproject.bramble.*)
com.quantumresearch.mycel.app.*       (application - was org.briarproject.briar.*)
```

---

## 🔧 **Infrastructure Layer: Bramble → Spore**

### **Core Spore Classes**

#### **API Layer (spore-api)**
```java
// BEFORE: spore-api
org.briarproject.bramble.api.crypto.CryptoComponent
org.briarproject.bramble.api.sync.SyncManager
org.briarproject.bramble.api.transport.TransportManager
org.briarproject.bramble.api.plugin.PluginManager
org.briarproject.bramble.api.contact.ContactManager
org.briarproject.bramble.api.db.DatabaseComponent

// AFTER: spore-api
com.quantumresearch.mycel.spore.api.crypto.CryptoComponent
com.quantumresearch.mycel.spore.api.sync.SyncManager
com.quantumresearch.mycel.spore.api.transport.TransportManager
com.quantumresearch.mycel.spore.api.plugin.PluginManager
com.quantumresearch.mycel.spore.api.contact.ContactManager
com.quantumresearch.mycel.spore.api.db.DatabaseComponent
```

#### **Core Implementation (spore-core)**
```java
// BEFORE: spore-core
org.briarproject.bramble.sync.SyncManagerImpl
org.briarproject.bramble.transport.TransportManagerImpl
org.briarproject.bramble.crypto.CryptoComponentImpl
org.briarproject.bramble.plugin.PluginManagerImpl
org.briarproject.bramble.contact.ContactManagerImpl
org.briarproject.bramble.db.DatabaseComponentImpl

// AFTER: spore-core
com.quantumresearch.mycel.spore.sync.SyncManagerImpl
com.quantumresearch.mycel.spore.transport.TransportManagerImpl
com.quantumresearch.mycel.spore.crypto.CryptoComponentImpl
com.quantumresearch.mycel.spore.plugin.PluginManagerImpl
com.quantumresearch.mycel.spore.contact.ContactManagerImpl
com.quantumresearch.mycel.spore.db.DatabaseComponentImpl
```

#### **Platform-Specific Implementations**

**Android Spore (spore-android)**
```java
// BEFORE: spore-android
org.briarproject.bramble.android.AndroidComponent
org.briarproject.bramble.android.BrambleAndroidModule
org.briarproject.bramble.plugin.bluetooth.AndroidBluetoothPlugin
org.briarproject.bramble.plugin.tor.AndroidTorPlugin
org.briarproject.bramble.plugin.wifi.WifiPlugin

// AFTER: spore-android
com.quantumresearch.mycel.spore.android.AndroidComponent
com.quantumresearch.mycel.spore.android.SporeAndroidModule
com.quantumresearch.mycel.spore.plugin.bluetooth.AndroidBluetoothPlugin
com.quantumresearch.mycel.spore.plugin.tor.AndroidTorPlugin
com.quantumresearch.mycel.spore.plugin.wifi.WifiPlugin
```

**Java Spore (spore-java)**
```java
// BEFORE: spore-java
org.briarproject.bramble.java.JavaComponent
org.briarproject.bramble.java.BrambleJavaModule
org.briarproject.bramble.plugin.tor.JavaTorPlugin
org.briarproject.bramble.plugin.tcp.TcpPlugin

// AFTER: spore-java
com.quantumresearch.mycel.spore.java.JavaComponent
com.quantumresearch.mycel.spore.java.SporeJavaModule
com.quantumresearch.mycel.spore.plugin.tor.JavaTorPlugin
com.quantumresearch.mycel.spore.plugin.tcp.TcpPlugin
```

---

## 📱 **Application Layer: Briar → Mycel**

### **Core Mycel Classes**

#### **API Layer (mycel-api)**
```java
// BEFORE: mycel-api
org.briarproject.briar.api.messaging.MessagingManager
org.briarproject.briar.api.forum.ForumManager
org.briarproject.briar.api.blog.BlogManager
org.briarproject.briar.api.privategroup.PrivateGroupManager
org.briarproject.briar.api.sharing.SharingManager
org.briarproject.briar.api.introduction.IntroductionManager

// AFTER: mycel-api
com.quantumresearch.mycel.app.api.messaging.MessagingManager
com.quantumresearch.mycel.app.api.forum.ForumManager
com.quantumresearch.mycel.app.api.blog.BlogManager
com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager
com.quantumresearch.mycel.app.api.sharing.SharingManager
com.quantumresearch.mycel.app.api.introduction.IntroductionManager
```

#### **Core Implementation (mycel-core)**
```java
// BEFORE: mycel-core
org.briarproject.briar.messaging.MessagingManagerImpl
org.briarproject.briar.forum.ForumManagerImpl
org.briarproject.briar.blog.BlogManagerImpl
org.briarproject.briar.privategroup.PrivateGroupManagerImpl
org.briarproject.briar.sharing.SharingManagerImpl
org.briarproject.briar.introduction.IntroductionManagerImpl

// AFTER: mycel-core
com.quantumresearch.mycel.app.messaging.MessagingManagerImpl
com.quantumresearch.mycel.app.forum.ForumManagerImpl
com.quantumresearch.mycel.app.blog.BlogManagerImpl
com.quantumresearch.mycel.app.privategroup.PrivateGroupManagerImpl
com.quantumresearch.mycel.app.sharing.SharingManagerImpl
com.quantumresearch.mycel.app.introduction.IntroductionManagerImpl
```

#### **Android Application (mycel-android)**
```java
// BEFORE: mycel-android
org.briarproject.briar.android.BriarApplication
org.briarproject.briar.android.AndroidComponent
org.briarproject.briar.android.BriarAndroidModule
org.briarproject.briar.android.activity.BriarActivity
org.briarproject.briar.android.controller.BriarController

// AFTER: mycel-android
com.quantumresearch.mycel.app.android.MycelApplication
com.quantumresearch.mycel.app.android.AndroidComponent
com.quantumresearch.mycel.app.android.MycelAndroidModule
com.quantumresearch.mycel.app.android.activity.MycelActivity
com.quantumresearch.mycel.app.android.controller.MycelController
```

#### **REST API (mycel-headless)**
```java
// BEFORE: mycel-headless
org.briarproject.briar.headless.BriarHeadlessApp
org.briarproject.briar.headless.BriarHeadlessModule
org.briarproject.briar.headless.rest.BriarRestApiController

// AFTER: mycel-headless
com.quantumresearch.mycel.app.headless.MycelHeadlessApp
com.quantumresearch.mycel.app.headless.MycelHeadlessModule
com.quantumresearch.mycel.app.headless.rest.MycelRestApiController
```

---

## 🏗️ **Dagger Module Renaming**

### **Infrastructure Modules (Spore)**
```java
// BEFORE
@Module BrambleApiModule
@Module BrambleCoreModule  
@Module BrambleAndroidModule
@Module BrambleJavaModule

// AFTER
@Module SporeApiModule
@Module SporeCoreModule
@Module SporeAndroidModule  
@Module SporeJavaModule
```

### **Application Modules (Mycel)**
```java
// BEFORE
@Module BriarApiModule
@Module BriarCoreModule
@Module BriarAndroidModule
@Module BriarHeadlessModule

// AFTER
@Module MycelApiModule
@Module MycelCoreModule
@Module MycelAndroidModule
@Module MycelHeadlessModule
```

---

## 🧪 **Test Class Renaming**

### **Spore Tests**
```java
// BEFORE: bramble tests
org.briarproject.bramble.test.BrambleTestCase
org.briarproject.bramble.test.BrambleIntegrationTest
org.briarproject.bramble.crypto.CryptoComponentTest

// AFTER: spore tests
com.quantumresearch.mycel.spore.test.SporeTestCase
com.quantumresearch.mycel.spore.test.SporeIntegrationTest
com.quantumresearch.mycel.spore.crypto.CryptoComponentTest
```

### **Mycel Tests**
```java
// BEFORE: briar tests
org.briarproject.briar.test.BriarTestCase
org.briarproject.briar.test.BriarIntegrationTest
org.briarproject.briar.android.BriarAndroidTest

// AFTER: mycel tests
com.quantumresearch.mycel.app.test.MycelTestCase
com.quantumresearch.mycel.app.test.MycelIntegrationTest
com.quantumresearch.mycel.app.android.MycelAndroidTest
```

---

## 🔧 **Factory and Builder Classes**

### **Spore Factories**
```java
// BEFORE
BrambleComponentFactory
BramblePluginFactory
BrambleTransportFactory

// AFTER
SporeComponentFactory
SporePluginFactory  
SporeTransportFactory
```

### **Mycel Factories**
```java
// BEFORE
BriarComponentFactory
BriarControllerFactory
BriarServiceFactory

// AFTER
MycelComponentFactory
MycelControllerFactory
MycelServiceFactory
```

---

## 📄 **Configuration and Manifest Updates**

### **Android Manifest Classes**
```xml
<!-- BEFORE -->
<application android:name="org.briarproject.briar.android.BriarApplication">
<service android:name="org.briarproject.briar.android.BriarService">
<receiver android:name="org.briarproject.briar.android.BriarBroadcastReceiver">

<!-- AFTER -->
<application android:name="com.quantumresearch.mycel.app.android.MycelApplication">
<service android:name="com.quantumresearch.mycel.app.android.MycelService">
<receiver android:name="com.quantumresearch.mycel.app.android.MycelBroadcastReceiver">
```

### **Test Runner Classes**
```java
// BEFORE
org.briarproject.briar.android.BriarTestRunner

// AFTER  
com.quantumresearch.mycel.app.android.MycelTestRunner
```

---

## 🎯 **Special Naming Considerations**

### **Classes to Keep Generic Names**
Some classes should use descriptive names rather than brand names:

```java
// Database Components - Keep descriptive
DatabaseComponent (not SporeDatabaseComponent)
CryptoComponent (not SporeCryptoComponent)

// Plugin Interfaces - Keep descriptive  
Plugin (not SporePlugin)
TransportPlugin (not SporeTransportPlugin)

// Exception Classes - Keep descriptive
DbException (not SporeDbException)
CryptoException (not SporeCryptoException)
```

### **Classes to Brand with Spore/Mycel**
Brand-specific classes that represent the main components:

```java
// Main Application Classes
SporeApplication, MycelApplication

// Main Service Classes  
SporeService, MycelService

// Main Manager Classes
SporeManager, MycelManager

// Main Controller Classes
SporeController, MycelController
```

---

## 📋 **Implementation Order**

### **Phase 3: Spore Infrastructure (Bramble → Spore)**
1. **spore-api**: Foundation interfaces
2. **spore-core**: Core implementations  
3. **spore-android**: Android platform layer
4. **spore-java**: Java platform layer

### **Phase 4: Mycel Application (Briar → Mycel)**
1. **mycel-api**: Application interfaces
2. **mycel-core**: Application logic
3. **mycel-android**: Android UI and integration
4. **mycel-headless**: REST API service

---

## 🧬 **Biological Metaphor Summary**

The new naming creates a coherent biological system:

```
🍄 MYCEL ECOSYSTEM
├── 🟤 Spore Layer (Infrastructure)
│   ├── Networking protocols
│   ├── Cryptographic functions  
│   ├── Transport mechanisms
│   └── Database operations
└── 🟢 Mycel Layer (Application)
    ├── Messaging features
    ├── User interface
    ├── Social functions
    └── Content sharing
```

**Conceptual Flow:** Spores germinate and create the network infrastructure that enables Mycel applications to connect, communicate, and flourish in a decentralized ecosystem.

This naming convention maintains technical clarity while creating a memorable, cohesive brand identity rooted in natural networking systems.