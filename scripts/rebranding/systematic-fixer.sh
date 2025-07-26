#!/bin/bash

# Systematic Fixer - Fix ALL identified Briar/Bramble issues systematically
# This addresses the specific issues found by the systematic identifier

echo "🔧 SYSTEMATIC BRIAR/BRAMBLE ISSUE FIXER"
echo "======================================="
echo ""

# Create backup directory
BACKUP_DIR="scripts/rebranding/systematic-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "📁 Backup directory: $BACKUP_DIR"
echo ""

echo "🎯 Phase 1: Fix 'extends BriarActivity' Declarations (29 files)"
echo "--------------------------------------------------------------"

# List of files that extend BriarActivity (from systematic analysis)
BRIAR_ACTIVITY_FILES=(
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/settings/SettingsActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contact/add/nearby/AddNearbyContactActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contact/add/remote/AddContactActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contact/add/remote/PendingContactListActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contact/connect/ConnectViaBluetoothActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/sharing/SharingStatusActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/sharing/InvitationActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/test/TestDataActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contactselection/ContactSelectorActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/introduction/IntroductionActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/mailbox/MailboxActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/hotspot/HotspotActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/blog/WriteBlogPostActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/blog/BlogActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/blog/RssFeedActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/blog/ReblogActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/forum/CreateForumActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/panic/PanicPreferencesActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/panic/PanicResponderActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/removabledrive/RemovableDriveActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/navdrawer/NavDrawerActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/navdrawer/IntentRouter.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/navdrawer/TransportsActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/login/ChangePasswordActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/threaded/ThreadListActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/conversation/ConversationActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/conversation/ImageActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/privategroup/creation/CreateGroupActivity.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/privategroup/memberlist/GroupMemberListActivity.java"
)

# Fix extends BriarActivity
FIXED_EXTENDS=0
for file in "${BRIAR_ACTIVITY_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        echo "  📝 Fixing: $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-extends"
        sed -i '' 's/extends BriarActivity/extends MycelActivity/g' "$file"
        sed -i '' 's/Class<? extends BriarActivity>/Class<? extends MycelActivity>/g' "$file"
        FIXED_EXTENDS=$((FIXED_EXTENDS + 1))
    else
        echo "  ⚠️  File not found: $file"
    fi
done
echo "    ✅ Fixed $FIXED_EXTENDS files extending BriarActivity"

echo ""
echo "🎯 Phase 2: Fix Import Statements"
echo "---------------------------------"

# Fix all import statements in one pass across all Java files
echo "  📦 Fixing BriarActivity imports..."
find mycel-android -name "*.java" -exec grep -l "import.*BriarActivity" {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-import" 2>/dev/null || true
    sed -i '' 's/import.*BriarActivity;/import com.quantumresearch.mycel.app.android.activity.MycelActivity;/g' "$file"
done

echo "  📦 Fixing BriarService imports..."
find mycel-android -name "*.java" -exec grep -l "import.*BriarService" {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-import-service" 2>/dev/null || true
    sed -i '' 's/import.*BriarService;/import com.quantumresearch.mycel.app.android.MycelService;/g' "$file"
done

echo "  📦 Fixing BriarController imports..."
find mycel-android -name "*.java" -exec grep -l "import.*BriarController" {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-import-controller" 2>/dev/null || true
    sed -i '' 's/import.*BriarController;/import com.quantumresearch.mycel.app.android.controller.MycelController;/g' "$file"
done

echo ""
echo "🎯 Phase 3: Fix Variable and Method References"
echo "----------------------------------------------"

# Fix variable declarations, method parameters, and return types
echo "  🔗 Fixing BriarActivity variable/method references..."
find mycel-android -name "*.java" -exec grep -l "BriarActivity" {} \; | while read file; do
    if ! grep -q "import.*BriarActivity\|extends BriarActivity" "$file"; then
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-vars" 2>/dev/null || true
        sed -i '' 's/\bBriarActivity\b/MycelActivity/g' "$file"
    fi
done

echo "  🔗 Fixing BriarService variable/method references..."
find mycel-android -name "*.java" -exec grep -l "BriarService" {} \; | while read file; do
    if ! grep -q "import.*BriarService" "$file"; then
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-service-vars" 2>/dev/null || true
        sed -i '' 's/\bBriarService\b/MycelService/g' "$file"
    fi
done

echo "  🔗 Fixing BriarController variable/method references..."
find mycel-android -name "*.java" -exec grep -l "BriarController" {} \; | while read file; do
    if ! grep -q "import.*BriarController\|implements.*BriarController" "$file"; then
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-controller-vars" 2>/dev/null || true
        sed -i '' 's/\bBriarController\b/MycelController/g' "$file"
    fi
done

echo ""
echo "🎯 Phase 4: Fix Static Method Calls"
echo "-----------------------------------"

echo "  🔗 Fixing BriarService.EXTRA_* static calls..."
find mycel-android -name "*.java" -exec grep -l "BriarService\." {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-static" 2>/dev/null || true
    sed -i '' 's/BriarService\./MycelService./g' "$file"
done

echo ""
echo "🎯 Phase 5: Fix Remaining Constructor and Class Names"
echo "----------------------------------------------------"

# Fix any remaining mismatched constructor/class names
echo "  🏗️  Fixing constructor name mismatches..."

# Fix specific cases we know about
files_to_check=(
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/activity/ActivityModule.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/controller/MycelControllerImpl.java"
)

for file in "${files_to_check[@]}"; do
    if [[ -f "$file" ]]; then
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-constructors" 2>/dev/null || true
        
        # Fix method names and parameters
        sed -i '' 's/provideBriarController/provideMycelController/g' "$file"
        sed -i '' 's/BriarControllerImpl briarController/MycelControllerImpl mycelController/g' "$file"
        sed -i '' 's/provideBriarServiceConnection/provideMycelServiceConnection/g' "$file"
        sed -i '' 's/BriarServiceConnection();/MycelServiceConnection();/g' "$file"
        
        # Fix Logger class name references  
        sed -i '' 's/BriarControllerImpl\.class\.getName()/MycelControllerImpl.class.getName()/g' "$file"
        
        # Fix service binding references
        sed -i '' 's/BriarServiceConnection serviceConnection/MycelServiceConnection serviceConnection/g' "$file"
        sed -i '' 's/BriarBinder/MycelBinder/g' "$file"
    fi
done

echo ""
echo "🎯 Phase 6: Verification"
echo "------------------------"

echo "  🔍 Checking for remaining issues..."

REMAINING_EXTENDS=$(find mycel-android -name "*.java" -exec grep -l "extends BriarActivity" {} \; 2>/dev/null | wc -l)
echo "    Remaining 'extends BriarActivity': $REMAINING_EXTENDS files"

REMAINING_IMPORTS=$(find mycel-android -name "*.java" -exec grep -l "import.*Briar" {} \; 2>/dev/null | wc -l)
echo "    Remaining Briar imports: $REMAINING_IMPORTS files"

REMAINING_VARS=$(find mycel-android -name "*.java" -exec grep -l "BriarActivity\|BriarService\|BriarController" {} \; 2>/dev/null | wc -l)
echo "    Remaining Briar references: $REMAINING_VARS files"

TOTAL_REMAINING=$((REMAINING_EXTENDS + REMAINING_IMPORTS + REMAINING_VARS))

echo ""
echo "📊 SYSTEMATIC FIXER SUMMARY"
echo "==========================="

echo "✅ COMPLETED PHASES:"
echo "  ✓ Fixed extends BriarActivity declarations"
echo "  ✓ Fixed import statements for Briar classes"
echo "  ✓ Fixed variable and method references"
echo "  ✓ Fixed static method calls"
echo "  ✓ Fixed constructor name mismatches"
echo ""
echo "🎯 REMAINING ISSUES: $TOTAL_REMAINING"
echo ""

if [[ $TOTAL_REMAINING -eq 0 ]]; then
    echo "🎉 SUCCESS: All systematic Briar/Bramble references fixed!"
    echo "🧪 NEXT: Run 'make clean && make build' to test compilation"
else
    echo "⚠️  Some issues may remain - check individual files if build fails"
fi

echo ""
echo "📁 Backups saved to: $BACKUP_DIR"
echo "🔧 To verify: Run systematic-issue-identifier.sh again"