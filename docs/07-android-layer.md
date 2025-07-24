# Android Layer - UI and Platform Integration

## Overview

The Android layer provides the primary user interface and platform-specific integration for the Briar/Mycel messaging application. It implements a modern Android UI using Material Design components, MVVM architecture, and comprehensive accessibility support.

## Android Architecture

### Layer Structure

```
┌─────────────────────────────────────────┐
│            Android UI Layer            │  ← THIS LAYER
├─────────────────────────────────────────┤
│         Briar Application Layer         │  ← Application Logic
├─────────────────────────────────────────┤
│          Bramble Infrastructure         │  ← Core Services
├─────────────────────────────────────────┤
│         Android Platform APIs          │  ← Android System
└─────────────────────────────────────────┘
```

### Module Structure

**Main Module**: `mycel-android/`
- **Package**: `org.briarproject.briar.android.*`
- **Target**: Android API 21-34 (Android 5.0 - Android 14)
- **Architecture**: MVVM with LiveData and ViewModels

## UI Architecture Patterns

### MVVM Implementation

**ViewModel Pattern**:
```java
public class ConversationViewModel extends AndroidViewModel {
    private final ConversationManager conversationManager;
    private final EventBus eventBus;
    private final MutableLiveData<List<ConversationMessageHeader>> messages;
    private final MutableLiveData<Boolean> loading;
    
    @Inject
    public ConversationViewModel(@NonNull Application application,
            ConversationManager conversationManager, EventBus eventBus) {
        super(application);
        this.conversationManager = conversationManager;
        this.eventBus = eventBus;
        this.messages = new MutableLiveData<>();
        this.loading = new MutableLiveData<>();
        
        // Register for message events
        eventBus.addListener(this::handleEvent);
    }
    
    public LiveData<List<ConversationMessageHeader>> getMessages() {
        return messages;
    }
    
    public void loadMessages(ContactId contactId) {
        loading.setValue(true);
        ioExecutor.execute(() -> {
            try {
                Collection<ConversationMessageHeader> messageHeaders = 
                    conversationManager.getMessageHeaders(contactId);
                messages.postValue(new ArrayList<>(messageHeaders));
            } catch (DbException e) {
                handleException(e);
            } finally {
                loading.postValue(false);
            }
        });
    }
}
```

### Fragment-Based Navigation

**Activity Structure**:
```java
@ActivityScoped
public class MainActivity extends BriarActivity {
    @Inject ViewModelProvider.Factory viewModelFactory;
    
    private MainViewModel viewModel;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        viewModel = new ViewModelProvider(this, viewModelFactory)
            .get(MainViewModel.class);
        
        setupNavigationDrawer();
        setupFragmentNavigation();
        observeViewModel();
    }
    
    private void setupFragmentNavigation() {
        NavController navController = Navigation.findNavController(
            this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
}
```

## Core UI Components

### Location: `mycel-android/src/main/java/org/briarproject/briar/android/`

### Main Navigation (`navdrawer/`)

**NavDrawerFragment.java**:
```java
public class NavDrawerFragment extends BaseFragment {
    @Inject ContactManager contactManager;
    @Inject TransportPropertyManager transportPropertyManager;
    
    private NavDrawerViewModel viewModel;
    private RecyclerView contactList;
    private TextView transportStatus;
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        viewModel = new ViewModelProvider(this, viewModelFactory)
            .get(NavDrawerViewModel.class);
        
        setupContactList();
        setupTransportStatus();
        observeViewModel();
    }
    
    private void setupTransportStatus() {
        viewModel.getTransportStates().observe(this, states -> {
            updateTransportIndicators(states);
        });
    }
}
```

**Transport Status Indicators**:
```java
public class TransportStateIndicator extends View {
    private final Paint torPaint;
    private final Paint bluetoothPaint;
    private final Paint lanPaint;
    
    public void updateStates(Map<TransportId, TransportState> states) {
        torState = states.get(TorConstants.ID);
        bluetoothState = states.get(BluetoothConstants.ID);
        lanState = states.get(LanTcpConstants.ID);
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw transport status indicators
        drawTransportIndicator(canvas, torPaint, torState, 0);
        drawTransportIndicator(canvas, bluetoothPaint, bluetoothState, 1);
        drawTransportIndicator(canvas, lanPaint, lanState, 2);
    }
}
```

### Conversation UI (`conversation/`)

**ConversationActivity.java**:
```java
@ActivityScoped
public class ConversationActivity extends BriarActivity {
    private static final String CONTACT_ID = "contactId";
    
    @Inject ViewModelProvider.Factory viewModelFactory;
    private ConversationViewModel viewModel;
    private RecyclerView messageList;
    private EditText messageInput;
    private ImageButton sendButton;
    
    public static void show(Context context, ContactId contactId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(CONTACT_ID, contactId.getBytes());
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        
        ContactId contactId = getContactIdFromIntent();
        viewModel = new ViewModelProvider(this, viewModelFactory)
            .get(ConversationViewModel.class);
        
        setupMessageList(contactId);
        setupMessageInput();
        loadMessages(contactId);
    }
    
    private void setupMessageList(ContactId contactId) {
        ConversationAdapter adapter = new ConversationAdapter(contactId);
        messageList.setAdapter(adapter);
        
        viewModel.getMessages().observe(this, messages -> {
            adapter.submitList(messages);
            scrollToBottom();
        });
    }
}
```

**Message Adapter**:
```java
public class ConversationAdapter extends 
        RecyclerView.Adapter<ConversationAdapter.MessageViewHolder> {
    
    private final List<ConversationMessageHeader> messages;
    private final ContactId contactId;
    
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(getLayoutForViewType(viewType), parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        ConversationMessageHeader message = messages.get(position);
        holder.bind(message);
    }
    
    private int getLayoutForViewType(int viewType) {
        switch (viewType) {
            case TYPE_INCOMING_MESSAGE:
                return R.layout.list_item_conversation_msg_in;
            case TYPE_OUTGOING_MESSAGE:
                return R.layout.list_item_conversation_msg_out;
            case TYPE_NOTICE:
                return R.layout.list_item_conversation_notice;
            default:
                throw new IllegalArgumentException();
        }
    }
}
```

### Contact Management (`contact/`)

**ContactListFragment.java**:
```java
public class ContactListFragment extends BaseFragment {
    @Inject ContactManager contactManager;
    private ContactListViewModel viewModel;
    private RecyclerView contactList;
    private SwipeRefreshLayout swipeRefresh;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, 
            container, false);
        
        setupContactList(view);
        setupSwipeRefresh(view);
        setupFab(view);
        
        return view;
    }
    
    private void setupContactList(View view) {
        contactList = view.findViewById(R.id.contact_list);
        ContactListAdapter adapter = new ContactListAdapter();
        contactList.setAdapter(adapter);
        
        viewModel.getContacts().observe(this, contacts -> {
            adapter.submitList(contacts);
            updateEmptyState(contacts.isEmpty());
        });
    }
}
```

**AddContactActivity.java**:
```java
@ActivityScoped
public class AddContactActivity extends BriarActivity {
    private static final int REQUEST_QR_CODE = 1;
    
    @Inject ContactExchangeManager contactExchangeManager;
    @Inject QrCodeDecoder qrCodeDecoder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        
        setupQrCodeScanner();
        setupRemoteAddContact();
        setupNearbyAddContact();
    }
    
    private void setupQrCodeScanner() {
        findViewById(R.id.scan_qr_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, QrCodeScannerActivity.class);
            startActivityForResult(intent, REQUEST_QR_CODE);
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QR_CODE && resultCode == RESULT_OK) {
            String qrCode = data.getStringExtra(QrCodeScannerActivity.RESULT);
            handleQrCodeResult(qrCode);
        }
    }
}
```

### Settings UI (`settings/`)

**SettingsActivity.java**:
```java
public class SettingsActivity extends BriarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
        }
    }
}

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        
        setupNotificationSettings();
        setupScreenLockSettings();
        setupTransportSettings();
        setupPrivacySettings();
    }
    
    private void setupNotificationSettings() {
        SwitchPreference notificationPref = findPreference("pref_notifications");
        notificationPref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (Boolean) newValue;
            updateNotificationSettings(enabled);
            return true;
        });
    }
}
```

### Forum UI (`forum/`)

**ForumListActivity.java**:
```java
@ActivityScoped
public class ForumListActivity extends BriarActivity {
    @Inject ForumManager forumManager;
    private ForumListViewModel viewModel;
    private RecyclerView forumList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_list);
        
        setupForumList();
        setupCreateForumFab();
        loadForums();
    }
    
    private void setupCreateForumFab() {
        FloatingActionButton fab = findViewById(R.id.create_forum_fab);
        fab.setOnClickListener(v -> showCreateForumDialog());
    }
    
    private void showCreateForumDialog() {
        CreateForumDialogFragment dialog = CreateForumDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), CreateForumDialogFragment.TAG);
    }
}

public class ForumActivity extends BriarActivity {
    private ForumViewModel viewModel;
    private RecyclerView postList;
    private EditText replyInput;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        
        GroupId forumId = getForumIdFromIntent();
        setupPostList(forumId);
        setupReplyInput(forumId);
        loadPosts(forumId);
    }
}
```

## Material Design Implementation

### Themes and Styles

**Location**: `mycel-android/src/main/res/values/`

**styles.xml**:
```xml
<resources>
    <!-- Base application theme -->
    <style name="BriarTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/briar_primary</item>
        <item name="colorPrimaryDark">@color/briar_primary_dark</item>
        <item name="colorAccent">@color/briar_accent</item>
        <item name="colorSurface">@color/briar_surface</item>
        <item name="colorOnSurface">@color/briar_on_surface</item>
        
        <!-- Material Design 3 attributes -->
        <item name="colorPrimaryContainer">@color/briar_primary_container</item>
        <item name="colorOnPrimaryContainer">@color/briar_on_primary_container</item>
    </style>
    
    <!-- Splash screen theme -->
    <style name="SplashScreenTheme" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/briar_primary</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/logo_circle</item>
    </style>
    
    <!-- Message bubble styles -->
    <style name="MessageBubbleIncoming">
        <item name="android:background">@drawable/msg_bubble_incoming</item>
        <item name="android:padding">@dimen/message_bubble_padding</item>
    </style>
    
    <style name="MessageBubbleOutgoing">
        <item name="android:background">@drawable/msg_bubble_outgoing</item>
        <item name="android:padding">@dimen/message_bubble_padding</item>
    </style>
</resources>
```

**colors.xml**:
```xml
<resources>
    <!-- Primary brand colors -->
    <color name="briar_primary">#2E7D32</color>
    <color name="briar_primary_dark">#1B5E20</color>
    <color name="briar_accent">#4CAF50</color>
    
    <!-- Material Design 3 color tokens -->
    <color name="briar_primary_container">#A8DADC</color>
    <color name="briar_on_primary_container">#001E1F</color>
    <color name="briar_surface">#FEFBFF</color>
    <color name="briar_on_surface">#1C1B1F</color>
    
    <!-- Message colors -->
    <color name="message_bubble_incoming">#E8F5E8</color>
    <color name="message_bubble_outgoing">#DCF8C6</color>
    
    <!-- Transport status colors -->
    <color name="transport_tor">#7E57C2</color>
    <color name="transport_bluetooth">#2196F3</color>
    <color name="transport_lan">#FF9800</color>
</resources>
```

### Custom UI Components

**TrustIndicatorView.java**:
```java
public class TrustIndicatorView extends View {
    private final Paint trustedPaint;
    private final Paint untrustedPaint;
    private boolean trusted = false;
    
    public TrustIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        trustedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trustedPaint.setColor(ContextCompat.getColor(context, R.color.trust_verified));
        
        untrustedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        untrustedPaint.setColor(ContextCompat.getColor(context, R.color.trust_unverified));
    }
    
    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = trusted ? trustedPaint : untrustedPaint;
        float radius = Math.min(getWidth(), getHeight()) / 2f;
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, paint);
    }
}
```

**ConversationMessageView.java**:
```java
public class ConversationMessageView extends LinearLayout {
    private TextView messageText;
    private TextView timestamp;
    private ImageView deliveryStatus;
    private TrustIndicatorView trustIndicator;
    
    public ConversationMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_conversation_message, 
            this, true);
        
        messageText = findViewById(R.id.message_text);
        timestamp = findViewById(R.id.timestamp);
        deliveryStatus = findViewById(R.id.delivery_status);
        trustIndicator = findViewById(R.id.trust_indicator);
    }
    
    public void bind(ConversationMessageHeader message) {
        messageText.setText(message.getText());
        timestamp.setText(DateUtils.formatDateTime(getContext(), 
            message.getTimestamp()));
        
        updateDeliveryStatus(message.getDeliveryState());
        trustIndicator.setTrusted(message.isVerified());
    }
}
```

## Accessibility Support

### Accessibility Implementation

**AccessibilityHelper.java**:
```java
public class AccessibilityHelper {
    public static void setContentDescription(View view, String description) {
        view.setContentDescription(description);
    }
    
    public static void announceForAccessibility(View view, String announcement) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.announceForAccessibility(announcement);
        }
    }
    
    public static void setAccessibilityDelegate(RecyclerView recyclerView) {
        recyclerView.setAccessibilityDelegateCompat(
            new RecyclerViewAccessibilityDelegate(recyclerView) {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, 
                        AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    
                    // Add custom accessibility information
                    if (host instanceof ConversationMessageView) {
                        ConversationMessageView messageView = 
                            (ConversationMessageView) host;
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
                        info.setContentDescription(
                            messageView.getAccessibilityDescription());
                    }
                }
            });
    }
}
```

### Voice Control Integration

```xml
<!-- Example accessibility labels in strings.xml -->
<string name="accessibility_send_message">Send message</string>
<string name="accessibility_message_from">Message from %1$s</string>
<string name="accessibility_message_timestamp">Sent at %1$s</string>
<string name="accessibility_contact_verified">Contact is verified</string>
<string name="accessibility_transport_connected">%1$s transport connected</string>
```

## Internationalization (i18n)

### Multi-Language Support

**Supported Languages** (30+ locales):
- `values/` - English (default)
- `values-ar/` - Arabic
- `values-de/` - German  
- `values-es/` - Spanish
- `values-fr/` - French
- `values-it/` - Italian
- `values-ja/` - Japanese
- `values-ru/` - Russian
- `values-zh-rCN/` - Chinese (Simplified)
- And 20+ additional languages

**String Resource Management**:
```xml
<!-- values/strings.xml -->
<resources>
    <string name="app_name">Briar</string>
    <string name="action_send">Send</string>
    <string name="conversation_title">%1$s</string>
    <string name="message_delivery_pending">Sending…</string>
    <string name="message_delivery_delivered">Delivered</string>
    
    <!-- Plurals support -->
    <plurals name="contact_list_selection">
        <item quantity="one">%1$d contact selected</item>
        <item quantity="other">%1$d contacts selected</item>
    </plurals>
</resources>
```

## Notification System

### Notification Implementation

**NotificationManagerImpl.java**:
```java
@Singleton
public class NotificationManagerImpl implements NotificationManager {
    private final Context context;
    private final AndroidNotificationManager androidNotificationManager;
    private final Settings settings;
    
    @Override
    public void showMessageNotification(ContactId contactId, String text) {
        if (!areNotificationsEnabled()) return;
        
        Contact contact = getContact(contactId);
        
        NotificationCompat.Builder builder = 
            new NotificationCompat.Builder(context, CHANNEL_MESSAGES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(contact.getDisplayName())
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true);
        
        // Add reply action
        addReplyAction(builder, contactId);
        
        // Add mark as read action
        addMarkAsReadAction(builder, contactId);
        
        androidNotificationManager.notify(contactId.hashCode(), 
            builder.build());
    }
    
    private void addReplyAction(NotificationCompat.Builder builder, 
            ContactId contactId) {
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(getString(R.string.reply_hint))
            .build();
        
        Intent replyIntent = new Intent(context, ReplyReceiver.class);
        replyIntent.putExtra(EXTRA_CONTACT_ID, contactId.getBytes());
        
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(
            context, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Action replyAction = 
            new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                getString(R.string.reply), replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();
        
        builder.addAction(replyAction);
    }
}
```

## Security and Privacy UI

### Screen Lock Integration

**ScreenLockManager.java**:
```java
public class ScreenLockManager {
    private final KeyguardManager keyguardManager;
    private final Settings settings;
    
    public boolean isScreenLockEnabled() {
        return settings.getBoolean(PREF_SCREEN_LOCK, true);
    }
    
    public void enableScreenLock() {
        settings.putBoolean(PREF_SCREEN_LOCK, true);
    }
    
    public boolean shouldLockScreen() {
        return isScreenLockEnabled() && 
               keyguardManager.isDeviceSecure() &&
               hasBeenInBackground();
    }
    
    public void showUnlockPrompt(FragmentActivity activity, 
            Callback<Boolean> callback) {
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
            ContextCompat.getMainExecutor(activity),
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(
                        BiometricPrompt.AuthenticationResult result) {
                    callback.onResult(true);
                }
                
                @Override
                public void onAuthenticationError(int errorCode, 
                        CharSequence errString) {
                    callback.onResult(false);
                }
            });
        
        BiometricPrompt.PromptInfo promptInfo = 
            new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.unlock_briar))
                .setSubtitle(getString(R.string.unlock_to_continue))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
        
        biometricPrompt.authenticate(promptInfo);
    }
}
```

## Background Services

### BriarService.java

```java
public class BriarService extends LifecycleService {
    private final IBinder binder = new BriarBinder();
    
    @Inject BriarController briarController;
    @Inject NotificationManager notificationManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidComponent.from(this).inject(this);
        
        // Start in foreground to prevent killing
        startForeground(ONGOING_NOTIFICATION_ID, 
            createOngoingNotification());
        
        // Initialize Briar controller
        briarController.start();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
        // Handle intent actions (reply, mark as read, etc.)
        if (intent != null) {
            handleIntentAction(intent);
        }
        
        return START_STICKY; // Restart if killed
    }
    
    public class BriarBinder extends Binder {
        public BriarService getService() {
            return BriarService.this;
        }
    }
}
```

## Rebranding Implications for Android Layer

### High Impact Changes (Visual/User-Facing)

1. **App Name and Branding**:
   ```xml
   <!-- strings.xml -->
   <string name="app_name">Mycel</string>
   <string name="app_name_formatted">Mycel</string>
   ```

2. **Visual Assets**:
   - `artwork/` - All logo SVG files
   - `res/mipmap-*/` - App launcher icons
   - `res/drawable/` - UI icons and graphics

3. **Package References**:
   - AndroidManifest.xml package declaration
   - All Java class package names
   - Deep link scheme changes (`briar://` → `mycel://`)

### Moderate Impact Changes

1. **Notification Content**:
   - Notification channel names
   - Foreground service descriptions
   - System notification text

2. **Settings and Preferences**:
   - Default preference values
   - About screen information
   - Version information display

3. **Error Messages and Dialogs**:
   - User-facing error messages
   - Dialog titles and content
   - Help text and instructions

### Implementation Strategy for Mycel

1. **Package Renaming**:
   ```
   org.briarproject.briar.android.* 
   → com.quantumresearch.mycel.app.android.*
   ```

2. **Application ID Update**:
   ```gradle
   android {
       defaultConfig {
           applicationId "com.quantumresearch.mycel"
       }
   }
   ```

3. **Resource Updates**:
   - Update all string resources (30+ languages)
   - Replace visual assets with Mycel branding
   - Update themes and color schemes if needed

4. **Configuration Updates**:
   - Update deep link schemes
   - Update notification channels
   - Update service declarations

The Android layer provides a polished, accessible user interface that will seamlessly transition to Mycel branding while maintaining all functionality, accessibility features, and Material Design compliance.