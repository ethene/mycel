#!/bin/bash

# Quick Critical Search - Focus on Runtime Issues Only
# Excludes rebranding scripts/reports and focuses on most critical areas

echo "🔍 QUICK CRITICAL SEARCH - RUNTIME ISSUES ONLY"
echo "=============================================="
echo ""

# Create results directory
RESULTS_DIR="scripts/rebranding/critical-search-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"

# Define exclusion paths
EXCLUDE_PATHS="./scripts/rebranding ./search-results ./docs/archive ./build ./*/build"

echo "📋 EXCLUDING FROM SEARCH:"
echo "  - scripts/rebranding/"
echo "  - search-results/"
echo "  - docs/archive/"
echo "  - All build/ directories"
echo ""

echo "🎯 1. XML LAYOUTS - Most Critical (Fragment References)"
echo "======================================================="

# Search XML layouts for critical references (case-insensitive)
find mycel-android/src/main/res/layout -name "*.xml" \
    -exec grep -i -Hn "briar\|bramble\|org\.briarproject\|org\.bramble" {} \; \
    > "$RESULTS_DIR/layout-critical.txt" 2>/dev/null

LAYOUT_COUNT=$(wc -l < "$RESULTS_DIR/layout-critical.txt" | tr -d ' ')
echo "  Found: $LAYOUT_COUNT layout file references"

if [[ $LAYOUT_COUNT -gt 0 ]]; then
    echo "  🚨 CRITICAL LAYOUT ISSUES:"
    head -5 "$RESULTS_DIR/layout-critical.txt"
    if [[ $LAYOUT_COUNT -gt 5 ]]; then
        echo "    ... see $RESULTS_DIR/layout-critical.txt for all $LAYOUT_COUNT issues"
    fi
fi

echo ""
echo "🎯 2. ANDROID MANIFESTS - Critical (Component References)"
echo "========================================================="

# Search manifests for critical references
find . -name "AndroidManifest.xml" \
    -not -path "./scripts/*" -not -path "./build/*" -not -path "./*/build/*" \
    -exec grep -i -Hn "briar\|bramble\|org\.briarproject\|org\.bramble" {} \; \
    > "$RESULTS_DIR/manifest-critical.txt" 2>/dev/null

MANIFEST_COUNT=$(wc -l < "$RESULTS_DIR/manifest-critical.txt" | tr -d ' ')
echo "  Found: $MANIFEST_COUNT manifest references"

if [[ $MANIFEST_COUNT -gt 0 ]]; then
    echo "  🚨 CRITICAL MANIFEST ISSUES:"
    cat "$RESULTS_DIR/manifest-critical.txt"
fi

echo ""
echo "🎯 3. XML PREFERENCES - Critical (Settings References)"
echo "====================================================="

# Search XML preferences for critical references
find mycel-android/src/main/res/xml -name "*.xml" \
    -exec grep -i -Hn "briar\|bramble\|org\.briarproject\|org\.bramble" {} \; \
    > "$RESULTS_DIR/preferences-critical.txt" 2>/dev/null

PREF_COUNT=$(wc -l < "$RESULTS_DIR/preferences-critical.txt" | tr -d ' ')
echo "  Found: $PREF_COUNT preference file references"

if [[ $PREF_COUNT -gt 0 ]]; then
    echo "  🚨 CRITICAL PREFERENCE ISSUES:"
    cat "$RESULTS_DIR/preferences-critical.txt"
fi

echo ""
echo "📊 CRITICAL RUNTIME SUMMARY"
echo "==========================="

TOTAL_CRITICAL=$((LAYOUT_COUNT + MANIFEST_COUNT + PREF_COUNT))

echo "🚨 CRITICAL RUNTIME ISSUES (will cause crashes):"
echo "  XML Layouts:          $LAYOUT_COUNT references"
echo "  Android Manifests:    $MANIFEST_COUNT references"
echo "  XML Preferences:      $PREF_COUNT references"
echo "  ═══════════════════════════════════════"
echo "  🎯 TOTAL CRITICAL:    $TOTAL_CRITICAL issues"

echo ""
if [[ $TOTAL_CRITICAL -eq 0 ]]; then
    echo "✅ EXCELLENT: No critical runtime issues found!"
    echo "✅ App should run without ClassNotFoundException crashes"
else
    echo "🚨 CRITICAL: $TOTAL_CRITICAL runtime issues need immediate fixing!"
    echo "🚨 These WILL cause app crashes and ClassNotFoundException errors"
    echo ""
    echo "🔧 NEXT STEPS:"
    echo "  1. Fix XML layout references first (highest priority)"
    echo "  2. Fix Android manifest references"
    echo "  3. Fix XML preference references"
    echo "  4. Test compilation after each fix"
fi

echo ""
echo "📁 Results saved to: $RESULTS_DIR"