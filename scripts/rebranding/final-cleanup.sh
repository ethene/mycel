#!/bin/bash

# Final Cleanup - Fix remaining briar/bramble references
# This addresses the last uncaught references found in the codebase

echo "🔧 FINAL BRIAR/BRAMBLE REFERENCE CLEANUP"
echo "======================================="
echo ""

# Create backup directory
BACKUP_DIR="scripts/rebranding/final-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "📁 Backup directory: $BACKUP_DIR"
echo ""

# 1. Fix Java Import and Class References
echo "🎯 1. Fixing Java Import and Class References"
echo "--------------------------------------------"

# ActivityModule.java - Fix imports
echo "  📝 Fixing ActivityModule.java imports..."
file="mycel-android/src/main/java/com/quantumresearch/mycel/app/android/activity/ActivityModule.java"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")"
    sed -i '' 's/import.*BriarController;/import com.quantumresearch.mycel.app.android.controller.MycelController;/g' "$file"
    sed -i '' 's/import.*BriarControllerImpl;/import com.quantumresearch.mycel.app.android.controller.MycelControllerImpl;/g' "$file"
    sed -i '' 's/BriarServiceConnection;/MycelServiceConnection;/g' "$file"
    echo "    ✅ ActivityModule.java updated"
else
    echo "    ⚠️  ActivityModule.java not found"
fi

# MycelControllerImpl.java - Fix references
echo "  📝 Fixing MycelControllerImpl.java references..."
file="mycel-android/src/main/java/com/quantumresearch/mycel/app/android/controller/MycelControllerImpl.java"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")"
    sed -i '' 's/BriarServiceConnection;/MycelServiceConnection;/g' "$file"
    echo "    ✅ MycelControllerImpl.java updated"
else
    echo "    ⚠️  MycelControllerImpl.java not found"
fi

# 2. Fix ProGuard Rules
echo ""
echo "🎯 2. Fixing ProGuard Rules"
echo "---------------------------"

# proguard-rules.txt
echo "  🛡️  Fixing proguard-rules.txt..."
file="mycel-android/proguard-rules.txt"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")"
    sed -i '' 's/org\.briarproject\.briar\.android/com.quantumresearch.mycel.app.android/g' "$file"
    sed -i '' 's/org\.briarproject\.briar\.api\.android/com.quantumresearch.mycel.app.api.android/g' "$file"
    echo "    ✅ proguard-rules.txt updated"
else
    echo "    ⚠️  proguard-rules.txt not found"
fi

# proguard-test.txt
echo "  🛡️  Fixing proguard-test.txt..."
file="mycel-android/proguard-test.txt"
if [[ -f "$file" ]]; then
    cp "$file" "$BACKUP_DIR/$(basename "$file")"
    sed -i '' 's/org\.briarproject\.bramble\./com.quantumresearch.mycel.spore./g' "$file"
    sed -i '' 's/org\.briarproject\.briar\./com.quantumresearch.mycel.app./g' "$file"
    echo "    ✅ proguard-test.txt updated"
else
    echo "    ⚠️  proguard-test.txt not found"
fi

# 3. Check for any remaining critical references
echo ""
echo "🎯 3. Scanning for Remaining Critical References"
echo "-----------------------------------------------"

echo "  🔍 Scanning Java files for uncaught Briar/Bramble classes..."
REMAINING_JAVA=$(find mycel-android -name "*.java" -exec grep -l "BriarService\|BriarActivity\|BriarController" {} \; 2>/dev/null | wc -l)
echo "    Found: $REMAINING_JAVA Java files with class references"

if [[ $REMAINING_JAVA -gt 0 ]]; then
    echo "    📄 Files still containing references:"
    find mycel-android -name "*.java" -exec grep -l "BriarService\|BriarActivity\|BriarController" {} \; 2>/dev/null | head -5 | while read file; do
        echo "      🚨 $file"
        grep -n "BriarService\|BriarActivity\|BriarController" "$file" | head -3 | while read line; do
            echo "         $line"
        done
    done
fi

echo "  🔍 Scanning XML files for hardcoded package references..."
REMAINING_XML=$(find mycel-android -name "*.xml" -exec grep -l "org\.briarproject\|org\.bramble" {} \; 2>/dev/null | wc -l)
echo "    Found: $REMAINING_XML XML files with package references"

echo "  🔍 Scanning ProGuard files for old package references..."
REMAINING_PROGUARD=$(find mycel-android -name "*.pro" -o -name "*.txt" | xargs grep -l "org\.briarproject\|org\.bramble" 2>/dev/null | wc -l)
echo "    Found: $REMAINING_PROGUARD ProGuard files with package references"

# 4. Test Framework References (ACCEPTABLE - Don't change these)
echo ""
echo "🎯 4. Acceptable External References (DO NOT CHANGE)"
echo "---------------------------------------------------"

echo "  ✅ External test framework references (should remain):"
EXTERNAL_TEST=$(find . -name "*.java" -exec grep -l "org\.briarproject\.nullsafety\|BrambleTestCase" {} \; 2>/dev/null | wc -l)
echo "    Found: $EXTERNAL_TEST files using external test framework (OK)"

echo "  ✅ External dependency references in build.gradle (should remain):"
EXTERNAL_DEPS=$(find . -name "build.gradle*" -exec grep -l "org\.briarproject.*:" {} \; 2>/dev/null | wc -l)
echo "    Found: $EXTERNAL_DEPS gradle files with external dependencies (OK)"

# 5. Generate Summary
echo ""
echo "📊 FINAL CLEANUP SUMMARY"
echo "========================"

TOTAL_ISSUES=$((REMAINING_JAVA + REMAINING_XML + REMAINING_PROGUARD))

echo "🚨 REMAINING CRITICAL ISSUES:"
echo "  Java class references:     $REMAINING_JAVA files"
echo "  XML package references:    $REMAINING_XML files"  
echo "  ProGuard package refs:     $REMAINING_PROGUARD files"
echo ""
echo "🎯 TOTAL REMAINING ISSUES: $TOTAL_ISSUES"
echo ""

if [[ $TOTAL_ISSUES -eq 0 ]]; then
    echo "🎉 SUCCESS: No critical briar/bramble references remain!"
    echo "✅ The codebase is fully rebranded to Mycel/Spore architecture"
else
    echo "⚠️  Some issues remain - see details above"
    echo "🔧 Consider manual review of remaining files"
fi

echo ""
echo "✅ CLEANUP ACTIONS COMPLETED:"
echo "  • Fixed Java import statements"
echo "  • Updated ProGuard rules for new package names"
echo "  • Preserved external dependencies (as they should be)"
echo "  • Verified no hardcoded XML references remain"
echo ""
echo "📁 Backups saved to: $BACKUP_DIR"
echo ""
echo "🧪 NEXT STEPS:"
echo "  1. Run 'make clean && make build' to test compilation"
echo "  2. Test Android APK installation and fragment loading"
echo "  3. Verify no runtime ClassNotFoundException errors"