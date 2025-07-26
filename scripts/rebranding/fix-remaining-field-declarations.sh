#!/bin/bash

# Fix Remaining Field Declarations - Fix the specific remaining compilation errors
# These are field declarations that still use old class names

echo "🔧 FIXING REMAINING FIELD DECLARATIONS"
echo "======================================"
echo ""

# Create backup directory
BACKUP_DIR="scripts/rebranding/field-declarations-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "📁 Backup directory: $BACKUP_DIR"
echo ""

echo "🎯 Phase 1: Fix BriarRecyclerView Field Declarations"
echo "---------------------------------------------------"

# List of files with BriarRecyclerView field declarations (from compilation errors)
RECYCLER_VIEW_FILES=(
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/blog/RssFeedManageFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/introduction/ContactChooserFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/blog/FeedFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/forum/ForumListFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contact/ContactListFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/blog/BlogFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contactselection/BaseContactSelectorFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/privategroup/list/GroupListFragment.java"
)

echo "  🔗 Fixing BriarRecyclerView → MycelRecyclerView in field declarations..."
for file in "${RECYCLER_VIEW_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-recycler"
        # Fix field declarations specifically
        sed -i '' 's/private BriarRecyclerView/private MycelRecyclerView/g' "$file"
        sed -i '' 's/protected BriarRecyclerView/protected MycelRecyclerView/g' "$file"
        sed -i '' 's/public BriarRecyclerView/public MycelRecyclerView/g' "$file"
        sed -i '' 's/BriarRecyclerView list/MycelRecyclerView list/g' "$file"
    else
        echo "    ⚠️  File not found: $file"
    fi
done

echo ""
echo "🎯 Phase 2: Fix BriarButton Field Declarations"
echo "---------------------------------------------"

# Files with BriarButton field declarations
BUTTON_FILES=(
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/contact/add/remote/NicknameFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/mailbox/MailboxStatusFragment.java"
    "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/mailbox/ErrorWizardFragment.java"
)

echo "  🔗 Fixing BriarButton → MycelButton in field declarations..."
for file in "${BUTTON_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-button"
        # Fix field declarations specifically
        sed -i '' 's/private BriarButton/private MycelButton/g' "$file"
        sed -i '' 's/protected BriarButton/protected MycelButton/g' "$file"
        sed -i '' 's/public BriarButton/public MycelButton/g' "$file"
    else
        echo "    ⚠️  File not found: $file"
    fi
done

echo ""
echo "🎯 Phase 3: Fix Any Remaining BriarAdapter References"
echo "----------------------------------------------------"

echo "  🔗 Searching for remaining BriarAdapter references..."
find mycel-android -name "*.java" -exec grep -l "BriarAdapter" {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-adapter-remaining" 2>/dev/null || true
    # Fix any remaining BriarAdapter references
    sed -i '' 's/BriarAdapter/MycelAdapter/g' "$file"
done

echo ""
echo "🎯 Phase 4: Fix Any Other Remaining Field Type Issues"
echo "----------------------------------------------------"

# Check for any other field declaration issues that might exist
echo "  🔍 Checking for other potential field type issues..."

# Fix BriarFragment if any exist
FRAGMENT_REFS=$(find mycel-android -name "*.java" -exec grep -l "BriarFragment" {} \; 2>/dev/null | wc -l)
if [[ $FRAGMENT_REFS -gt 0 ]]; then
    echo "  🔗 Fixing BriarFragment references..."
    find mycel-android -name "*.java" -exec grep -l "BriarFragment" {} \; | while read file; do
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-fragment"
        sed -i '' 's/BriarFragment/MycelFragment/g' "$file"
    done
fi

echo ""
echo "🎯 Phase 5: Final Compilation Test"
echo "----------------------------------"

echo "  🧪 Running quick compilation test to check remaining errors..."
export JAVA_HOME=$(/usr/libexec/java_home -v17)
ERROR_COUNT=$(./gradlew :mycel-android:compileOfficialDebugJavaWithJavac --warning-mode=none 2>&1 | grep -c "error:" || echo "0")
echo "    Compilation errors found: $ERROR_COUNT"

if [[ $ERROR_COUNT -eq 0 ]]; then
    echo "    🎉 COMPILATION SUCCESSFUL!"
elif [[ $ERROR_COUNT -lt 10 ]]; then
    echo "    ✅ Major progress - very few errors remaining"
else
    echo "    ⚠️  Still some errors to resolve"
fi

echo ""
echo "📊 FIELD DECLARATION FIX SUMMARY"
echo "================================"

echo "✅ COMPLETED FIXES:"
echo "  ✓ BriarRecyclerView field declarations → MycelRecyclerView"
echo "  ✓ BriarButton field declarations → MycelButton"
echo "  ✓ Any remaining BriarAdapter references → MycelAdapter"
echo "  ✓ BriarFragment references → MycelFragment (if any)"
echo ""
echo "🎯 COMPILATION ERRORS: $ERROR_COUNT"
echo ""

if [[ $ERROR_COUNT -eq 0 ]]; then
    echo "🎉 SUCCESS: All field declaration issues resolved!"
    echo "✅ The Android module should now compile successfully"
    echo "🧪 NEXT: Test full build with 'make build'"
else
    echo "📋 If errors remain, they are likely edge cases requiring manual review"
fi

echo ""
echo "📁 Backups saved to: $BACKUP_DIR"
echo "🔧 This targeted the specific field declaration compilation errors"