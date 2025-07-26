#!/bin/bash

# Find Runtime Issues - Comprehensive script to catch XML and resource-related problems
# This script would have caught the ContactListFragment XML reference issue

echo "🔍 COMPREHENSIVE RUNTIME ISSUE DETECTION"
echo "========================================"
echo ""

# Create results directory
RESULTS_DIR="scripts/rebranding/runtime-analysis-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"

echo "📁 Results will be saved to: $RESULTS_DIR"
echo ""

# 1. XML LAYOUT FILES - Fragment and Class References
echo "🎯 1. XML Layout Files - Fragment & Class References"
echo "------------------------------------------------"

echo "  📱 Checking XML layouts for hardcoded class references..."
find . -name "*.xml" -path "*/res/layout/*" -exec grep -l "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/xml-layout-references.txt"
XML_LAYOUT_COUNT=$(cat "$RESULTS_DIR/xml-layout-references.txt" | wc -l)
echo "    Found: $XML_LAYOUT_COUNT layout files with hardcoded references"

if [[ $XML_LAYOUT_COUNT -gt 0 ]]; then
    echo "    📄 Detailed references:"
    find . -name "*.xml" -path "*/res/layout/*" -exec grep -Hn "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/xml-layout-details.txt"
    head -10 "$RESULTS_DIR/xml-layout-details.txt" | while read line; do
        echo "      🚨 $line"
    done
    if [[ $XML_LAYOUT_COUNT -gt 10 ]]; then
        echo "      ... and $(($XML_LAYOUT_COUNT - 10)) more (see $RESULTS_DIR/xml-layout-details.txt)"
    fi
fi

# 2. XML PREFERENCE FILES - Settings and Configuration  
echo ""
echo "  ⚙️  Checking XML preferences for class references..."
find . -name "*.xml" -path "*/res/xml/*" -exec grep -l "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/xml-preference-references.txt"
XML_PREF_COUNT=$(cat "$RESULTS_DIR/xml-preference-references.txt" | wc -l)
echo "    Found: $XML_PREF_COUNT preference files with hardcoded references"

if [[ $XML_PREF_COUNT -gt 0 ]]; then
    find . -name "*.xml" -path "*/res/xml/*" -exec grep -Hn "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/xml-preference-details.txt"
    head -5 "$RESULTS_DIR/xml-preference-details.txt" | while read line; do
        echo "      🚨 $line"
    done
fi

# 3. ANDROID MANIFEST FILES - Services, Activities, Receivers
echo ""
echo "🎯 2. Android Manifest Files"
echo "----------------------------"

echo "  📋 Checking AndroidManifest.xml files for component references..."
find . -name "AndroidManifest.xml" -exec grep -l "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/manifest-references.txt"
MANIFEST_COUNT=$(cat "$RESULTS_DIR/manifest-references.txt" | wc -l)
echo "    Found: $MANIFEST_COUNT manifest files with old references"

if [[ $MANIFEST_COUNT -gt 0 ]]; then
    find . -name "AndroidManifest.xml" -exec grep -Hn "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/manifest-details.txt"
    cat "$RESULTS_DIR/manifest-details.txt" | while read line; do
        echo "      🚨 $line"
    done
fi

# 4. RESOURCE VALUES - Strings, Arrays, etc.
echo ""
echo "🎯 3. Resource Value Files"
echo "-------------------------"

echo "  📝 Checking resource values for hardcoded references..."
find . -name "*.xml" -path "*/res/values*/*" -exec grep -l "org\.briarproject\|org\.bramble\|briar\|bramble" {} \; > "$RESULTS_DIR/resource-value-references.txt"
RESOURCE_COUNT=$(cat "$RESULTS_DIR/resource-value-references.txt" | wc -l)
echo "    Found: $RESOURCE_COUNT resource files with potential references"

if [[ $RESOURCE_COUNT -gt 0 ]]; then
    # Filter out acceptable string references (app name changes are expected)
    find . -name "*.xml" -path "*/res/values*/*" -exec grep -Hn "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/resource-value-details.txt"
    RESOURCE_DETAIL_COUNT=$(cat "$RESULTS_DIR/resource-value-details.txt" | wc -l)
    if [[ $RESOURCE_DETAIL_COUNT -gt 0 ]]; then
        echo "    📄 Package references in resources:"
        head -5 "$RESULTS_DIR/resource-value-details.txt" | while read line; do
            echo "      🚨 $line"
        done
    fi
fi

# 5. GRADLE BUILD FILES - Package references, dependencies
echo ""
echo "🎯 4. Build Configuration Files"
echo "-------------------------------"

echo "  🔧 Checking build.gradle files for package references..."
find . -name "build.gradle*" -exec grep -l "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/gradle-references.txt"
GRADLE_COUNT=$(cat "$RESULTS_DIR/gradle-references.txt" | wc -l)
echo "    Found: $GRADLE_COUNT gradle files with old references"

if [[ $GRADLE_COUNT -gt 0 ]]; then
    find . -name "build.gradle*" -exec grep -Hn "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/gradle-details.txt"
    cat "$RESULTS_DIR/gradle-details.txt" | while read line; do
        echo "      🚨 $line"
    done
fi

# 6. PROGUARD/R8 CONFIGURATION - Obfuscation rules
echo ""
echo "🎯 5. ProGuard/R8 Configuration"
echo "------------------------------"

echo "  🛡️  Checking ProGuard rules for package references..."
find . -name "proguard*.pro" -o -name "proguard*.txt" -o -name "consumer-rules.pro" | xargs grep -l "org\.briarproject\|org\.bramble" 2>/dev/null > "$RESULTS_DIR/proguard-references.txt"
PROGUARD_COUNT=$(cat "$RESULTS_DIR/proguard-references.txt" | wc -l)
echo "    Found: $PROGUARD_COUNT ProGuard files with old references"

if [[ $PROGUARD_COUNT -gt 0 ]]; then
    find . -name "proguard*.pro" -o -name "proguard*.txt" -o -name "consumer-rules.pro" | xargs grep -Hn "org\.briarproject\|org\.bramble" 2>/dev/null > "$RESULTS_DIR/proguard-details.txt"
    cat "$RESULTS_DIR/proguard-details.txt" | while read line; do
        echo "      🚨 $line"
    done
fi

# 7. KOTLIN FILES - Import and usage issues
echo ""
echo "🎯 6. Kotlin Files"
echo "-----------------"

echo "  🅺 Checking Kotlin files for old references..."
find . -name "*.kt" -exec grep -l "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/kotlin-references.txt"
KOTLIN_COUNT=$(cat "$RESULTS_DIR/kotlin-references.txt" | wc -l)
echo "    Found: $KOTLIN_COUNT Kotlin files with old references"

if [[ $KOTLIN_COUNT -gt 0 ]]; then
    find . -name "*.kt" -exec grep -Hn "org\.briarproject\|org\.bramble" {} \; > "$RESULTS_DIR/kotlin-details.txt"
    echo "    📄 Sample Kotlin references:"
    head -5 "$RESULTS_DIR/kotlin-details.txt" | while read line; do
        echo "      🚨 $line"
    done
fi

# 8. SPECIAL RUNTIME FILES - JNI, Native, Resources
echo ""
echo "🎯 7. Special Runtime Files"
echo "--------------------------"

echo "  🔍 Checking for JNI/Native library references..."
find . -name "*.so" -o -name "*.dll" -o -name "*.jnilib" 2>/dev/null | wc -l | xargs echo "    Native libraries found:"

echo "  🔍 Checking assets directory for hardcoded references..."
if [[ -d "assets" ]] || find . -path "*/assets/*" -type f 2>/dev/null | head -1 | grep -q .; then
    find . -path "*/assets/*" -type f -exec grep -l "org\.briarproject\|org\.bramble\|briar\|bramble" {} \; 2>/dev/null > "$RESULTS_DIR/assets-references.txt"
    ASSETS_COUNT=$(cat "$RESULTS_DIR/assets-references.txt" | wc -l)
    echo "    Found: $ASSETS_COUNT asset files with potential references"
else
    echo "    No assets directory found"
fi

# 9. DOCUMENTATION AND CONFIG FILES
echo ""
echo "🎯 8. Documentation & Config Files"
echo "----------------------------------"

echo "  📚 Checking documentation files for outdated references..."
find . -name "*.md" -o -name "*.txt" -o -name "*.rst" | xargs grep -l "org\.briarproject\|org\.bramble" 2>/dev/null > "$RESULTS_DIR/docs-references.txt"
DOCS_COUNT=$(cat "$RESULTS_DIR/docs-references.txt" | wc -l)
echo "    Found: $DOCS_COUNT documentation files with old references"

# 10. GENERATE SUMMARY REPORT
echo ""
echo "📊 RUNTIME ISSUE SUMMARY"
echo "========================"

TOTAL_ISSUES=$((XML_LAYOUT_COUNT + XML_PREF_COUNT + MANIFEST_COUNT + GRADLE_COUNT + PROGUARD_COUNT + KOTLIN_COUNT))

echo "🚨 CRITICAL RUNTIME ISSUES (will cause crashes):"
echo "  XML Layouts:           $XML_LAYOUT_COUNT files"
echo "  XML Preferences:       $XML_PREF_COUNT files"
echo "  Android Manifests:     $MANIFEST_COUNT files"
echo "  ProGuard Rules:        $PROGUARD_COUNT files"
echo ""
echo "⚠️  BUILD-TIME ISSUES (will cause compilation failures):"
echo "  Kotlin Files:          $KOTLIN_COUNT files"
echo "  Gradle Files:          $GRADLE_COUNT files"
echo ""
echo "📋 INFORMATIONAL:"
echo "  Resource Values:       $RESOURCE_COUNT files"
echo "  Documentation:         $DOCS_COUNT files"
echo ""
echo "🎯 TOTAL CRITICAL ISSUES: $TOTAL_ISSUES"

# 11. SPECIFIC DETECTION FOR THE CONTACTLISTFRAGMENT ISSUE
echo ""
echo "🔬 SPECIFIC FRAGMENT REFERENCE DETECTION"
echo "========================================="

echo "  🧩 Searching for Fragment references in XML layouts..."
find . -name "*.xml" -path "*/layout/*" -exec grep -Hn 'android:name.*Fragment' {} \; > "$RESULTS_DIR/fragment-references.txt"
FRAGMENT_REF_COUNT=$(cat "$RESULTS_DIR/fragment-references.txt" | wc -l)
echo "    Found: $FRAGMENT_REF_COUNT fragment references in layouts"

if [[ $FRAGMENT_REF_COUNT -gt 0 ]]; then
    echo "    📱 Fragment references found:"
    cat "$RESULTS_DIR/fragment-references.txt" | while read line; do
        if echo "$line" | grep -q "org\.briarproject\|org\.bramble"; then
            echo "      🚨 OLD PACKAGE: $line"
        else
            echo "      ✅ UPDATED: $line"
        fi
    done
fi

echo ""
echo "📁 DETAILED RESULTS SAVED TO: $RESULTS_DIR"
echo ""
echo "🔧 RECOMMENDED ACTIONS:"
echo "  1. Fix XML layout fragment references (HIGHEST PRIORITY)"
echo "  2. Update AndroidManifest.xml component declarations"
echo "  3. Fix ProGuard rules for new package names"
echo "  4. Update build.gradle package references"
echo "  5. Verify no hardcoded strings in resource files"
echo ""
echo "✅ This script would have caught the ContactListFragment XML issue!"