# Troubleshooting Guide

## Overview

This document provides comprehensive troubleshooting guidance for common issues encountered during development, building, testing, and deployment of the Briar/Mycel application.

## Build Issues

### Gradle Build Problems

#### 1. Gradle Sync Failures

**Symptoms**:
```
> Could not resolve all dependencies for configuration ':app:debugCompileClasspath'
> Could not download [dependency]
```

**Solutions**:
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/
rm -rf .gradle/

# Clean project
./gradlew clean

# Refresh dependencies
./gradlew build --refresh-dependencies

# Check internet connectivity and proxy settings
./gradlew build --debug --stacktrace
```

**Gradle Wrapper Issues**:
```bash
# Re-download Gradle wrapper
./gradlew wrapper --gradle-version=7.6.1

# Fix permissions (Linux/macOS)
chmod +x gradlew

# Verify Gradle installation
./gradlew --version
```

#### 2. Memory Issues

**Symptoms**:
```
> OutOfMemoryError: Java heap space
> OutOfMemoryError: Metaspace
```

**Solutions**:

**gradle.properties**:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+UseParallelGC
org.gradle.daemon=true
org.gradle.parallel=true
```

**Local Environment**:
```bash
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"
export _JAVA_OPTIONS="-Xmx2g"
```

#### 3. Dependency Verification Failures

**Symptoms**:
```
> Dependency verification failed
> Checksum mismatch for [dependency]
```

**Solutions**:
```bash
# Update witness plugin checksums
./gradlew generateWitnessFile

# Bypass witness for specific dependencies (temporary)
./gradlew build -PnoWitness=true

# Check witness.gradle configuration
cat witness.gradle
```

### Android Build Issues

#### 1. SDK/NDK Issues

**Symptoms**:
```
> Android SDK not found
> NDK not configured
> Failed to find target with hash string 'android-34'
```

**Solutions**:
```bash
# Set Android SDK path
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Install missing SDK components
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "cmake;3.22.1"

# Update local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

#### 2. ProGuard/R8 Issues

**Symptoms**:
```
> Missing class references
> ClassNotFoundException at runtime
> Methods removed by minification
```

**Solutions**:

**proguard-rules.txt**:
```proguard
# Keep essential classes
-keep class org.briarproject.briar.** { *; }
-keep class org.briarproject.bramble.** { *; }

# Keep reflection-used classes
-keepclassmembers class * {
    @com.google.inject.Inject <init>(...);
}

# Debug build without minification
-dontobfuscate
-printmapping mapping.txt
```

#### 3. Dagger Dependency Injection Issues

**Symptoms**:
```
> [Dagger/MissingBinding] Cannot provide [Type]
> Dependency cycle detected
```

**Solutions**:
```java
// Check module configurations
@Module
public class TestModule {
    @Provides
    @Singleton
    MissingType provideMissingType() {
        return new MissingTypeImpl();
    }
}

// Verify component includes all necessary modules
@Component(modules = {
    BrambleModule.class,
    BriarModule.class,
    TestModule.class  // Add missing module
})
```

### Java/Desktop Build Issues

#### 1. JDK Version Problems

**Symptoms**:
```
> Unsupported class file major version
> javac: invalid target release
```

**Solutions**:
```bash
# Check Java version
java -version
javac -version

# Set correct JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Use specific JDK for Gradle
./gradlew build -Dorg.gradle.java.home=$JAVA_HOME
```

#### 2. Native Library Issues

**Symptoms**:
```
> UnsatisfiedLinkError: no [library] in java.library.path
> Native library not found
```

**Solutions**:
```bash
# Linux: Install native dependencies
sudo apt install libbluetooth-dev libasound2-dev

# Set library path
export LD_LIBRARY_PATH=/path/to/native/libs:$LD_LIBRARY_PATH

# Verify native libraries
ldd /path/to/library.so  # Linux
otool -L /path/to/library.dylib  # macOS
```

## Runtime Issues

### Database Problems

#### 1. Database Connection Failures

**Symptoms**:
```
DbException: Failed to connect to database
Database is already in use: nio:file
```

**Solutions**:
```bash
# Check database files exist and are accessible
ls -la ~/.local/share/briar/

# Check file permissions
chmod 600 ~/.local/share/briar/db.*

# Stop other instances
pkill -f "briar\|mycel"

# Reset database (development only)
rm -rf ~/.local/share/briar/db*
```

#### 2. Database Migration Issues

**Symptoms**:
```
> Migration failed: incompatible schema version
> Table [name] doesn't exist
```

**Solutions**:
```java
// Check migration scripts
// Location: spore-core/src/main/resources/db/migration/

// Force recreation (data loss)
public void resetDatabase() throws DbException {
    db.removeAll();
    db.close();
    // Delete database files
    // Restart application
}
```

#### 3. Database Corruption

**Symptoms**:
```
> Database corruption detected
> Page checksum mismatch
```

**Solutions**:
```bash
# H2 database repair
java -cp h2.jar org.h2.tools.Recover

# Backup before repair
cp -r ~/.local/share/briar/ ~/.local/share/briar.backup/

# Import from backup
java -cp h2.jar org.h2.tools.RunScript -url jdbc:h2:~/db -script backup.sql
```

### Network and Transport Issues

#### 1. Tor Connection Problems

**Symptoms**:
```
> Tor connection failed
> Circuit creation timeout
> Hidden service unreachable
```

**Solutions**:
```bash
# Check Tor service status
sudo systemctl status tor

# Restart Tor service
sudo systemctl restart tor

# Check Tor configuration
cat /etc/tor/torrc

# Test Tor connectivity
curl --socks5 127.0.0.1:9050 https://check.torproject.org/

# Debug Tor in application
# Set system property: -Dtor.debug=true
```

**Tor Configuration Issues**:
```bash
# /etc/tor/torrc additions for development
ControlPort 9051
CookieAuthentication 1
CookieAuthFileGroupReadable 1

# Restart Tor after configuration change
sudo systemctl restart tor
```

#### 2. Bluetooth Transport Issues

**Symptoms**:
```
> Bluetooth adapter not found
> Permission denied accessing Bluetooth
> Service discovery failed
```

**Solutions**:
```bash
# Linux: Check Bluetooth service
sudo systemctl status bluetooth

# Add user to bluetooth group
sudo usermod -a -G bluetooth $USER

# Check Bluetooth adapter
bluetoothctl list
bluetoothctl show

# Reset Bluetooth stack
sudo systemctl restart bluetooth

# Android: Check permissions in manifest
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

#### 3. LAN/Wi-Fi Transport Issues

**Symptoms**:
```
> Network unreachable
> Connection refused
> Timeout connecting to peer
```

**Solutions**:
```bash
# Check network connectivity
ping 8.8.8.8

# Check firewall rules
sudo ufw status  # Ubuntu
sudo iptables -L  # General Linux

# Test port availability
netstat -tuln | grep 7000

# Android: Check Wi-Fi permissions
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
```

### Android-Specific Issues

#### 1. Permissions Problems

**Symptoms**:
```
> SecurityException: Permission denied
> Camera/Storage access denied
```

**Solutions**:
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

```java
// Runtime permission handling
if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
    != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this, 
        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
}
```

#### 2. Android Version Compatibility

**Symptoms**:
```
> NoSuchMethodError on older Android versions
> API not available on this version
```

**Solutions**:
```java
// Check API level before using newer APIs
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    // Use API 23+ features
} else {
    // Fallback for older versions
}

// Use support library alternatives
import androidx.core.content.ContextCompat;
```

#### 3. Background Processing Issues

**Symptoms**:
```
> Background service killed
> Scheduled jobs not running
> Network requests failing in background
```

**Solutions**:
```xml
<!-- Foreground service declaration -->
<service android:name=".BriarService"
         android:foregroundServiceType="dataSync" />
```

```java
// Start foreground service
startForeground(NOTIFICATION_ID, createNotification());

// Use WorkManager for background tasks
WorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
    .setConstraints(new Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build())
    .build();
WorkManager.getInstance(context).enqueue(syncWork);
```

### Performance Issues

#### 1. Memory Leaks

**Symptoms**:
```
> OutOfMemoryError during normal operation
> App becomes sluggish over time
> High memory usage in profiler
```

**Solutions**:
```java
// Use weak references for listeners
private final WeakReference<Listener> listenerRef;

// Properly close resources
try (InputStream in = new FileInputStream(file)) {
    // Use stream
} // Automatically closed

// Remove listeners in onDestroy
@Override
protected void onDestroy() {
    super.onDestroy();
    eventBus.removeListener(this);
}
```

**Memory Analysis**:
```bash
# Generate heap dump
kill -3 <pid>  # Java
adb shell am dumpheap <package> /sdcard/dump.hprof  # Android

# Analyze with tools
jhat dump.hprof
# Or use Eclipse MAT, Android Studio Profiler
```

#### 2. Slow Database Operations

**Symptoms**:
```
> Database operations taking > 1 second
> UI freezing during database access
> Transaction timeouts
```

**Solutions**:
```java
// Use background threads for database operations
executor.execute(() -> {
    try {
        db.transaction(false, txn -> {
            // Database operations
        });
    } catch (DbException e) {
        LOG.warning("Database operation failed", e);
    }
});

// Add database indexes
CREATE INDEX idx_messages_timestamp ON messages(timestamp);
CREATE INDEX idx_messages_groupId ON messages(groupId);

// Optimize queries
// Use LIMIT for large result sets
// Avoid SELECT * queries
```

#### 3. UI Performance Issues

**Symptoms**:
```
> Choppy scrolling
> ANR (Application Not Responding)
> Frame drops in animations
```

**Solutions**:
```java
// Use RecyclerView with ViewHolder pattern
public class MessageViewHolder extends RecyclerView.ViewHolder {
    // Cache view references
    private final TextView messageText;
    private final TextView timestamp;
    
    public MessageViewHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.message_text);
        timestamp = itemView.findViewById(R.id.timestamp);
    }
}

// Optimize image loading
Glide.with(context)
    .load(imageUrl)
    .placeholder(R.drawable.placeholder)
    .into(imageView);

// Use DiffUtil for list updates
DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
diffResult.dispatchUpdatesTo(adapter);
```

## Testing Issues

### Unit Test Problems

#### 1. Mock Framework Issues

**Symptoms**:
```
> Mock object not behaving as expected
> Stubbed methods not called
> Verification failures
```

**Solutions**:
```java
// JMock: Proper mock setup
@Mock DatabaseComponent mockDb;
@Mock EventBus mockEventBus;

@Before
public void setUp() {
    context.checking(new Expectations() {{
        allowing(mockDb).getContact(with(any(ContactId.class)));
        will(returnValue(testContact));
    }});
}

// Verify mock interactions
context.assertIsSatisfied();
```

#### 2. Test Database Issues

**Symptoms**:
```
> Tests interfering with each other
> Database state not reset between tests
```

**Solutions**:
```java
@Before
public void setUp() throws Exception {
    // Use in-memory database for tests
    DatabaseConfig testConfig = new H2DatabaseConfig(
        "jdbc:h2:mem:test" + System.currentTimeMillis(),
        "test", "test"
    );
    db = new H2Database(testConfig, messageFactory, clock);
}

@After
public void tearDown() throws Exception {
    if (db != null) {
        db.close();
    }
}
```

### Android Test Issues

#### 1. Instrumentation Test Failures

**Symptoms**:
```
> Tests timing out
> UI elements not found
> Flaky test results
```

**Solutions**:
```java
// Use IdlingResource for asynchronous operations
@Before
public void setUp() {
    IdlingRegistry.getInstance().register(loadingIdlingResource);
}

// Wait for UI elements
onView(withId(R.id.button))
    .check(matches(isDisplayed()))
    .perform(click());

// Use TestRule for consistent setup
@Rule
public ActivityTestRule<MainActivity> activityRule = 
    new ActivityTestRule<>(MainActivity.class);
```

#### 2. Emulator Issues

**Symptoms**:
```
> Emulator not starting
> Tests running slowly on emulator
> Hardware features not available
```

**Solutions**:
```bash
# Start emulator with more resources
emulator -avd MycelTest -memory 4096 -cores 4

# Check emulator configuration
emulator -list-avds
emulator -avd MycelTest -verbose

# Use x86_64 images for better performance
android create avd -n MycelTest -t android-34 -k "system-images;android-34;google_apis;x86_64"
```

## Production Issues

### Crash Analysis

#### 1. Native Crashes

**Symptoms**:
```
> SIGSEGV in native code
> Native library crashes
```

**Solutions**:
```bash
# Analyze crash dump with GDB
gdb /path/to/binary core.dump

# Check native library dependencies
ldd /path/to/library.so

# Enable native debugging
-XX:+CreateCoredumpOnCrash
-XX:+ShowCodeDetailsInExceptionMessages
```

#### 2. Memory Issues in Production

**Symptoms**:
```
> OutOfMemoryError in production
> Performance degradation over time
```

**Solutions**:
```java
// Monitor memory usage
Runtime runtime = Runtime.getRuntime();
long maxMemory = runtime.maxMemory();
long totalMemory = runtime.totalMemory();
long freeMemory = runtime.freeMemory();

LOG.info("Memory: max={}, total={}, free={}", 
         maxMemory, totalMemory, freeMemory);

// Implement memory pressure handling
@Override
public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    if (level >= TRIM_MEMORY_MODERATE) {
        // Clear caches
        imageCache.clear();
        messageCache.trim();
    }
}
```

### Configuration Issues

#### 1. Environment-Specific Problems

**Symptoms**:
```
> Configuration not loading
> Different behavior in production vs development
```

**Solutions**:
```java
// Environment-specific configuration
public class ConfigurationManager {
    private final boolean isProduction = !BuildConfig.DEBUG;
    
    public String getDatabaseUrl() {
        if (isProduction) {
            return getProductionDatabaseUrl();
        } else {
            return getDevelopmentDatabaseUrl();
        }
    }
}

// Use build variants for configuration
android {
    buildTypes {
        debug {
            buildConfigField "String", "DATABASE_NAME", "\"briar_debug\""
        }
        release {
            buildConfigField "String", "DATABASE_NAME", "\"briar\""
        }
    }
}
```

## Debugging Tools and Techniques

### Logging and Diagnostics

#### 1. Structured Logging

```java
public class Logger {
    private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger(Logger.class.getName());
    
    public static void info(String message, Object... params) {
        LOG.info(String.format(message, params));
    }
    
    public static void error(String message, Throwable t) {
        LOG.log(Level.SEVERE, message, t);
    }
}
```

#### 2. Debug Builds with Enhanced Logging

```java
if (BuildConfig.DEBUG) {
    // Enable verbose logging
    System.setProperty("briar.debug.database", "true");
    System.setProperty("briar.debug.networking", "true");
    
    // Enable strict mode (Android)
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()
        .penaltyLog()
        .build());
}
```

### Performance Profiling

#### 1. Java Profiling

```bash
# JProfiler
java -agentpath:/path/to/jprofiler/bin/linux-x64/libjprofilerti.so=port=8849 -jar mycel.jar

# JVisualVM
jvisualvm --jdkhome $JAVA_HOME

# Java Flight Recorder
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=mycel.jfr -jar mycel.jar
```

#### 2. Android Profiling

```bash
# Method tracing
Debug.startMethodTracing("mycel-trace");
// ... code to profile
Debug.stopMethodTracing();

# Systrace (deprecated, use perfetto)
python systrace.py -t 10 -o trace.html sched freq idle am wm gfx view binder_driver hal dalvik camera input res

# Android Studio Profiler
# Use built-in CPU, Memory, Network profilers
```

This troubleshooting guide provides comprehensive solutions for the most common issues encountered in Briar/Mycel development and deployment, helping developers quickly identify and resolve problems across all platforms and environments.