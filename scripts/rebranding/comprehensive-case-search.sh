#!/bin/bash

# Comprehensive Case-Sensitive Search for Briar/Bramble References
# Searches for: briar, Briar, BRIAR, bramble, Bramble, BRAMBLE

echo "🔍 COMPREHENSIVE CASE-SENSITIVE BRIAR/BRAMBLE SEARCH"
echo "==================================================="
echo ""

# Create results directory
RESULTS_DIR="scripts/rebranding/case-search-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"
echo "📁 Results will be saved to: $RESULTS_DIR"
echo ""

# Define search patterns for all case variations
PATTERNS=(
    "briar"
    "Briar" 
    "BRIAR"
    "bramble"
    "Bramble"
    "BRAMBLE"
    "org\.briarproject"
    "org\.bramble"
    "BriarActivity"
    "BriarRecyclerView"
    "BriarService"
    "BriarController"
    "BrambleService"
    "BrambleController"
)

echo "🎯 1. CRITICAL RUNTIME FILES - XML LAYOUTS & MANIFESTS"
echo "======================================================"

echo "  📱 Searching XML layout files for ALL case variations..."
LAYOUT_RESULTS="$RESULTS_DIR/layout-references.txt"
> "$LAYOUT_RESULTS"

for pattern in "${PATTERNS[@]}"; do
    find mycel-android/src/main/res/layout -name "*.xml" -exec grep -Hn "$pattern" {} \; >> "$LAYOUT_RESULTS" 2>/dev/null
done

LAYOUT_COUNT=$(wc -l < "$LAYOUT_RESULTS" | tr -d ' ')
echo "    Found: $LAYOUT_COUNT layout file references"
if [[ $LAYOUT_COUNT -gt 0 ]]; then
    echo "    🚨 CRITICAL ISSUES FOUND:"
    head -10 "$LAYOUT_RESULTS"
    if [[ $LAYOUT_COUNT -gt 10 ]]; then
        echo "    ... ($(($LAYOUT_COUNT - 10)) more entries in $LAYOUT_RESULTS)"
    fi
fi

echo ""
echo "  📋 Searching AndroidManifest.xml files for ALL case variations..."
MANIFEST_RESULTS="$RESULTS_DIR/manifest-references.txt"
> "$MANIFEST_RESULTS"

for pattern in "${PATTERNS[@]}"; do
    find . -name "AndroidManifest.xml" -not -path "./scripts/*" -exec grep -Hn "$pattern" {} \; >> "$MANIFEST_RESULTS" 2>/dev/null
done

MANIFEST_COUNT=$(wc -l < "$MANIFEST_RESULTS" | tr -d ' ')
echo "    Found: $MANIFEST_COUNT manifest references"
if [[ $MANIFEST_COUNT -gt 0 ]]; then
    echo "    🚨 CRITICAL ISSUES FOUND:"
    head -10 "$MANIFEST_RESULTS"
    if [[ $MANIFEST_COUNT -gt 10 ]]; then
        echo "    ... ($(($MANIFEST_COUNT - 10)) more entries in $MANIFEST_RESULTS)"
    fi
fi

echo ""
echo "🎯 2. XML PREFERENCES & RESOURCE FILES" 
echo "====================================="

echo "  ⚙️  Searching XML preference files..."
PREF_RESULTS="$RESULTS_DIR/preference-references.txt"
> "$PREF_RESULTS"

for pattern in "${PATTERNS[@]}"; do
    find mycel-android/src/main/res/xml -name "*.xml" -exec grep -Hn "$pattern" {} \; >> "$PREF_RESULTS" 2>/dev/null
done

PREF_COUNT=$(wc -l < "$PREF_RESULTS" | tr -d ' ')
echo "    Found: $PREF_COUNT preference file references"

echo ""
echo "  📝 Searching all resource value files..."
RESOURCE_RESULTS="$RESULTS_DIR/resource-references.txt"
> "$RESOURCE_RESULTS"

for pattern in "${PATTERNS[@]}"; do
    find mycel-android/src/main/res -name "*.xml" -not -path "*/layout/*" -exec grep -Hn "$pattern" {} \; >> "$RESOURCE_RESULTS" 2>/dev/null
done

RESOURCE_COUNT=$(wc -l < "$RESOURCE_RESULTS" | tr -d ' ')
echo "    Found: $RESOURCE_COUNT resource file references"

echo ""
echo "🎯 3. JAVA/KOTLIN SOURCE CODE"
echo "============================"

echo "  ☕ Searching Java source files..."
JAVA_RESULTS="$RESULTS_DIR/java-references.txt"
> "$JAVA_RESULTS"

for pattern in "${PATTERNS[@]}"; do
    find . -name "*.java" -not -path "./scripts/*" -not -path "./build/*" -not -path "./*/build/*" -exec grep -Hn "$pattern" {} \; >> "$JAVA_RESULTS" 2>/dev/null
done

JAVA_COUNT=$(wc -l < "$JAVA_RESULTS" | tr -d ' ')
echo "    Found: $JAVA_COUNT Java file references"

echo ""
echo "  🅺 Searching Kotlin source files..."
KOTLIN_RESULTS="$RESULTS_DIR/kotlin-references.txt"
> "$KOTLIN_RESULTS"

for pattern in "${PATTERNS[@]}"; do
    find . -name "*.kt" -not -path "./scripts/*" -not -path "./build/*" -not -path "./*/build/*" -exec grep -Hn "$pattern" {} \; >> "$KOTLIN_RESULTS" 2>/dev/null
done

KOTLIN_COUNT=$(wc -l < "$KOTLIN_RESULTS" | tr -d ' ')
echo "    Found: $KOTLIN_COUNT Kotlin file references"

echo ""
echo "🎯 4. BUILD & CONFIGURATION FILES"
echo "================================="

echo "  🔧 Searching build.gradle files (excluding external dependencies)..."
GRADLE_RESULTS="$RESULTS_DIR/gradle-internal-references.txt"
> "$GRADLE_RESULTS"

# Search for internal package references, not external dependencies
find . -name "build.gradle" -not -path "./scripts/*" -exec grep -Hn "com\.quantumresearch\.mycel.*briar\|com\.quantumresearch\.mycel.*bramble\|applicationId.*briar\|applicationId.*bramble" {} \; >> "$GRADLE_RESULTS" 2>/dev/null

GRADLE_COUNT=$(wc -l < "$GRADLE_RESULTS" | tr -d ' ')
echo "    Found: $GRADLE_COUNT internal build configuration issues"

echo ""
echo "  🛡️  Searching ProGuard rules..."
PROGUARD_RESULTS="$RESULTS_DIR/proguard-references.txt"
> "$PROGUARD_RESULTS"

for pattern in "${PATTERNS[@]}"; do
    find . -name "proguard*.txt" -not -path "./scripts/*" -exec grep -Hn "$pattern" {} \; >> "$PROGUARD_RESULTS" 2>/dev/null
done

PROGUARD_COUNT=$(wc -l < "$PROGUARD_RESULTS" | tr -d ' ')
echo "    Found: $PROGUARD_COUNT ProGuard rule references"

echo ""
echo "📊 COMPREHENSIVE CASE-SENSITIVE SUMMARY"
echo "======================================="

TOTAL_CRITICAL=$((LAYOUT_COUNT + MANIFEST_COUNT + PREF_COUNT))
TOTAL_ISSUES=$((TOTAL_CRITICAL + JAVA_COUNT + KOTLIN_COUNT + GRADLE_COUNT + PROGUARD_COUNT))

echo "🚨 CRITICAL RUNTIME ISSUES (will cause crashes):"
echo "  XML Layouts:                  $LAYOUT_COUNT files"
echo "  Android Manifests:            $MANIFEST_COUNT files"  
echo "  XML Preferences:              $PREF_COUNT files"
echo "  📈 CRITICAL TOTAL:            $TOTAL_CRITICAL issues"

echo ""
echo "⚠️  BUILD-TIME & CODE ISSUES:"
echo "  Java Source Files:            $JAVA_COUNT references"
echo "  Kotlin Source Files:          $KOTLIN_COUNT references"
echo "  Build Configuration:          $GRADLE_COUNT issues"
echo "  ProGuard Rules:               $PROGUARD_COUNT references"

echo ""
echo "📋 INFORMATIONAL:"
echo "  Resource Values:              $RESOURCE_COUNT references"

echo ""
echo "🎯 GRAND TOTAL ALL CASES:       $TOTAL_ISSUES issues"

echo ""
if [[ $TOTAL_CRITICAL -eq 0 ]]; then
    echo "✅ EXCELLENT: No critical runtime issues found!"
    echo "✅ App should run without ClassNotFoundException crashes"
else
    echo "🚨 CRITICAL: $TOTAL_CRITICAL runtime issues need immediate fixing!"
    echo "🚨 These WILL cause app crashes and ClassNotFoundException errors"
fi

echo ""
echo "📁 All detailed results saved to: $RESULTS_DIR"
echo "🔧 Use the individual result files to fix specific issues"

# Show most critical issues first
if [[ $LAYOUT_COUNT -gt 0 ]]; then
    echo ""
    echo "🚨 MOST CRITICAL - XML LAYOUT ISSUES:"
    echo "===================================="
    head -5 "$LAYOUT_RESULTS"
fi

if [[ $MANIFEST_COUNT -gt 0 ]]; then
    echo ""
    echo "🚨 CRITICAL - ANDROID MANIFEST ISSUES:"
    echo "======================================"
    head -5 "$MANIFEST_RESULTS"
fi