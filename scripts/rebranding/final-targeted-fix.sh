#!/bin/bash

# Final Targeted Fix - Fix the specific remaining compilation errors
# Based on the exact errors from the compilation attempt

echo "🔧 FINAL TARGETED FIX FOR COMPILATION ERRORS"
echo "============================================"
echo ""

# Create backup directory
BACKUP_DIR="scripts/rebranding/final-targeted-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "📁 Backup directory: $BACKUP_DIR"
echo ""

echo "🎯 Phase 1: Fix Import Statements for Renamed View Classes"
echo "--------------------------------------------------------"

# List of specific classes that need import fixes
VIEW_CLASS_FIXES=(
    "BriarSnackbarBuilder:MycelSnackbarBuilder"
    "BriarRecyclerView:MycelRecyclerView" 
    "BriarAdapter:MycelAdapter"
    "BriarButton:MycelButton"
    "BriarFragment:MycelFragment"
)

for fix in "${VIEW_CLASS_FIXES[@]}"; do
    OLD_CLASS=$(echo "$fix" | cut -d: -f1)
    NEW_CLASS=$(echo "$fix" | cut -d: -f2)
    
    echo "  📦 Fixing imports: $OLD_CLASS → $NEW_CLASS"
    
    # Find and fix import statements
    find mycel-android -name "*.java" -exec grep -l "import.*$OLD_CLASS" {} \; | while read file; do
        echo "    📄 $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file")-$OLD_CLASS" 2>/dev/null || true
        sed -i '' "s/import.*\.$OLD_CLASS;/import com.quantumresearch.mycel.app.android.view.$NEW_CLASS;/g" "$file"
        sed -i '' "s/import.*\.$OLD_CLASS$/import com.quantumresearch.mycel.app.android.view.$NEW_CLASS/g" "$file"
    done
done

echo ""
echo "🎯 Phase 2: Fix Static Import Statements"
echo "----------------------------------------"

echo "  📦 Fixing static imports with BriarActivity references..."
find mycel-android -name "*.java" -exec grep -l "import static.*BriarActivity\." {} \; | while read file; do
    echo "    📄 $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-static" 2>/dev/null || true
    sed -i '' 's/import static.*BriarActivity\./import static com.quantumresearch.mycel.app.android.activity.MycelActivity./g' "$file"
done

echo ""
echo "🎯 Phase 3: Fix Variable and Method Declarations"
echo "------------------------------------------------"

# Fix extends and variable declarations for the view classes
for fix in "${VIEW_CLASS_FIXES[@]}"; do
    OLD_CLASS=$(echo "$fix" | cut -d: -f1)
    NEW_CLASS=$(echo "$fix" | cut -d: -f2)
    
    echo "  🔗 Fixing declarations: $OLD_CLASS → $NEW_CLASS"
    
    # Find files that use these classes in extends/implements/variables
    find mycel-android -name "*.java" -exec grep -l "\b$OLD_CLASS\b" {} \; | while read file; do
        # Skip files that have import statements (already handled)
        if ! grep -q "import.*$OLD_CLASS" "$file" 2>/dev/null; then
            echo "    📄 $file"
            cp "$file" "$BACKUP_DIR/$(basename "$file")-vars-$OLD_CLASS" 2>/dev/null || true
            sed -i '' "s/\b$OLD_CLASS\b/$NEW_CLASS/g" "$file"
        fi
    done
done

echo ""
echo "🎯 Phase 4: Fix Specific Interface and Implementation Issues"
echo "----------------------------------------------------------"

# Fix the ActivityModule remaining issues
echo "  🔧 Fixing ActivityModule.java specific issues..."
file="mycel-android/src/main/java/com/quantumresearch/mycel/app/android/activity/ActivityModule.java"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")-specific"
    # Fix the method parameter and variable name mismatch
    sed -i '' 's/protected BriarController provideMycelController(/protected MycelController provideMycelController(/g' "$file"
    sed -i '' 's/briarController/mycelController/g' "$file"
    sed -i '' 's/BriarServiceConnection provideMycelServiceConnection/MycelServiceConnection provideMycelServiceConnection/g' "$file"
    echo "    ✅ Fixed ActivityModule.java"
fi

# Fix the MycelControllerImpl remaining issues
echo "  🔧 Fixing MycelControllerImpl.java specific issues..."
file="mycel-android/src/main/java/com/quantumresearch/mycel/app/android/controller/MycelControllerImpl.java"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")-controller-specific"
    # Fix the interface implementation and variable types
    sed -i '' 's/implements BriarController/implements MycelController/g' "$file"
    sed -i '' 's/BriarService service =/MycelService service =/g' "$file"
    sed -i '' 's/MycelService\.MycelBinder/MycelService.MycelBinder/g' "$file"
    echo "    ✅ Fixed MycelControllerImpl.java"
fi

echo ""
echo "🎯 Phase 5: Final Verification and Summary"
echo "------------------------------------------"

echo "  🔍 Checking for remaining compilation issues..."

# Check for specific error patterns we just fixed
BRIAR_SNACKBAR=$(find mycel-android -name "*.java" -exec grep -l "BriarSnackbarBuilder" {} \; 2>/dev/null | wc -l)
echo "    BriarSnackbarBuilder references: $BRIAR_SNACKBAR files"

BRIAR_RECYCLER=$(find mycel-android -name "*.java" -exec grep -l "BriarRecyclerView" {} \; 2>/dev/null | wc -l)
echo "    BriarRecyclerView references: $BRIAR_RECYCLER files"

BRIAR_ADAPTER=$(find mycel-android -name "*.java" -exec grep -l "BriarAdapter" {} \; 2>/dev/null | wc -l)
echo "    BriarAdapter references: $BRIAR_ADAPTER files"

STATIC_IMPORTS=$(find mycel-android -name "*.java" -exec grep -l "import static.*BriarActivity\." {} \; 2>/dev/null | wc -l)
echo "    Static BriarActivity imports: $STATIC_IMPORTS files"

TOTAL_TARGETED=$((BRIAR_SNACKBAR + BRIAR_RECYCLER + BRIAR_ADAPTER + STATIC_IMPORTS))

echo ""
echo "📊 FINAL TARGETED FIX SUMMARY"
echo "============================="

echo "✅ COMPLETED TARGETED FIXES:"
echo "  ✓ BriarSnackbarBuilder → MycelSnackbarBuilder imports"
echo "  ✓ BriarRecyclerView → MycelRecyclerView imports"
echo "  ✓ BriarAdapter → MycelAdapter imports"
echo "  ✓ Static import statements for BriarActivity"
echo "  ✓ ActivityModule interface and variable fixes"
echo "  ✓ MycelControllerImpl interface implementation fix"
echo ""
echo "🎯 REMAINING TARGETED ISSUES: $TOTAL_TARGETED"
echo ""

if [[ $TOTAL_TARGETED -eq 0 ]]; then
    echo "🎉 SUCCESS: All targeted compilation errors should be resolved!"
    echo "🧪 NEXT: Run 'export JAVA_HOME=\$(/usr/libexec/java_home -v17) && ./gradlew :mycel-android:compileOfficialDebugJavaWithJavac' to test"
else
    echo "⚠️  Some targeted issues may remain"
    echo "📋 Files that might still need manual review:"
    if [[ $BRIAR_SNACKBAR -gt 0 ]]; then
        find mycel-android -name "*.java" -exec grep -l "BriarSnackbarBuilder" {} \; 2>/dev/null | head -3
    fi
    if [[ $BRIAR_RECYCLER -gt 0 ]]; then
        find mycel-android -name "*.java" -exec grep -l "BriarRecyclerView" {} \; 2>/dev/null | head -3
    fi
fi

echo ""
echo "📁 Backups saved to: $BACKUP_DIR"
echo "🔧 This fix targets the specific compilation errors from the build output"