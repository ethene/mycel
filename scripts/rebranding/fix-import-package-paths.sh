#!/bin/bash

# Fix Import Package Paths - Correct the package paths for the renamed view classes
# The classes exist but imports are using wrong package paths

echo "🔧 FIXING IMPORT PACKAGE PATHS"
echo "=============================="
echo ""

# Create backup directory
BACKUP_DIR="scripts/rebranding/import-path-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "📁 Backup directory: $BACKUP_DIR"
echo ""

echo "🎯 Phase 1: Fix MycelSnackbarBuilder Import Paths"
echo "------------------------------------------------"
echo "  📦 Changing from view package to util package..."

# MycelSnackbarBuilder is in util/ package, not view/ package
find mycel-android -name "*.java" -exec grep -l "import.*MycelSnackbarBuilder" {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-snackbar" 2>/dev/null || true
    sed -i '' 's/import com\.quantumresearch\.mycel\.app\.android\.view\.MycelSnackbarBuilder;/import com.quantumresearch.mycel.app.android.util.MycelSnackbarBuilder;/g' "$file"
done

echo ""
echo "🎯 Phase 2: Fix MycelAdapter Import Paths"
echo "----------------------------------------"
echo "  📦 Changing from view package to util package..."

# MycelAdapter is in util/ package, not view/ package
find mycel-android -name "*.java" -exec grep -l "import.*MycelAdapter" {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-adapter" 2>/dev/null || true
    sed -i '' 's/import com\.quantumresearch\.mycel\.app\.android\.view\.MycelAdapter;/import com.quantumresearch.mycel.app.android.util.MycelAdapter;/g' "$file"
done

echo ""
echo "🎯 Phase 3: Fix Remaining BriarAdapter → MycelAdapter References"
echo "---------------------------------------------------------------"
echo "  🔗 Fixing extends and variable declarations..."

# Fix extends BriarAdapter → extends MycelAdapter
find mycel-android -name "*.java" -exec grep -l "extends BriarAdapter" {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-extends-adapter" 2>/dev/null || true
    sed -i '' 's/extends BriarAdapter/extends MycelAdapter/g' "$file"
done

echo ""
echo "🎯 Phase 4: Fix Remaining BriarRecyclerView References"
echo "-----------------------------------------------------"
echo "  🔗 Fixing variable declarations and field types..."

# Fix BriarRecyclerView variable declarations
find mycel-android -name "*.java" -exec grep -l "BriarRecyclerView" {} \; | while read file; do
    # Skip files that have import statements (should be handled already)
    if ! grep -q "import.*BriarRecyclerView\|import.*MycelRecyclerView" "$file" 2>/dev/null; then
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-recycler-vars" 2>/dev/null || true
        sed -i '' 's/BriarRecyclerView/MycelRecyclerView/g' "$file"
    fi
done

echo ""
echo "🎯 Phase 5: Fix ActivityModule Controller Interface"
echo "--------------------------------------------------"
echo "  🔧 Fixing method return type in ActivityModule..."

file="mycel-android/src/main/java/com/quantumresearch/mycel/app/android/activity/ActivityModule.java"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")-controller-return"
    # Check current content and fix if needed
    if grep -q "BriarController" "$file"; then
        echo "    📄 Fixing BriarController references in $file"
        sed -i '' 's/BriarController/MycelController/g' "$file"
    fi
    echo "    ✅ ActivityModule checked"
fi

echo ""
echo "🎯 Phase 6: Fix MycelActivity Field Declarations"
echo "-----------------------------------------------"
echo "  🔧 Fixing field types in MycelActivity..."

file="mycel-android/src/main/java/com/quantumresearch/mycel/app/android/activity/MycelActivity.java"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")-activity-fields"
    # Fix field declarations that might still reference old types
    if grep -q "BriarController" "$file"; then
        echo "    📄 Fixing BriarController field type in $file"
        sed -i '' 's/BriarController/MycelController/g' "$file"
    fi
    echo "    ✅ MycelActivity checked"
fi

echo ""
echo "🎯 Phase 7: Final Verification"
echo "------------------------------"

echo "  🔍 Checking for remaining import issues..."

SNACKBAR_WRONG_IMPORTS=$(find mycel-android -name "*.java" -exec grep -l "import.*view.*MycelSnackbarBuilder" {} \; 2>/dev/null | wc -l)
echo "    MycelSnackbarBuilder wrong imports: $SNACKBAR_WRONG_IMPORTS files"

ADAPTER_WRONG_IMPORTS=$(find mycel-android -name "*.java" -exec grep -l "import.*view.*MycelAdapter" {} \; 2>/dev/null | wc -l)  
echo "    MycelAdapter wrong imports: $ADAPTER_WRONG_IMPORTS files"

BRIAR_ADAPTER_REFS=$(find mycel-android -name "*.java" -exec grep -l "extends BriarAdapter\|BriarAdapter<" {} \; 2>/dev/null | wc -l)
echo "    BriarAdapter references: $BRIAR_ADAPTER_REFS files"

BRIAR_RECYCLER_REFS=$(find mycel-android -name "*.java" -exec grep -l "BriarRecyclerView" {} \; 2>/dev/null | wc -l)
echo "    BriarRecyclerView references: $BRIAR_RECYCLER_REFS files"

TOTAL_REMAINING=$((SNACKBAR_WRONG_IMPORTS + ADAPTER_WRONG_IMPORTS + BRIAR_ADAPTER_REFS + BRIAR_RECYCLER_REFS))

echo ""
echo "📊 IMPORT PATH FIX SUMMARY"
echo "=========================="

echo "✅ COMPLETED FIXES:"
echo "  ✓ MycelSnackbarBuilder imports → util package"
echo "  ✓ MycelAdapter imports → util package"  
echo "  ✓ BriarAdapter extends → MycelAdapter extends"
echo "  ✓ BriarRecyclerView variables → MycelRecyclerView"
echo "  ✓ ActivityModule controller interface"
echo "  ✓ MycelActivity field declarations"
echo ""
echo "🎯 REMAINING PATH ISSUES: $TOTAL_REMAINING"
echo ""

if [[ $TOTAL_REMAINING -eq 0 ]]; then
    echo "🎉 SUCCESS: All import package paths should be correct!"
    echo "🧪 NEXT: Test compilation with './gradlew :mycel-android:compileOfficialDebugJavaWithJavac'"
else
    echo "⚠️  Some import path issues may remain"
    echo "📋 Check the files listed above for manual review"
fi

echo ""
echo "📁 Backups saved to: $BACKUP_DIR"
echo "🔧 This fixes the specific package path mismatches found"