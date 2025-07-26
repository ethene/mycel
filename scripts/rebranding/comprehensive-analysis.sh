#!/bin/bash

# Comprehensive Briar/Bramble Reference Analysis Script
# Systematically identifies ALL remaining references that need fixing
# Usage: ./scripts/rebranding/comprehensive-analysis.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 COMPREHENSIVE BRIAR/BRAMBLE REFERENCE ANALYSIS${NC}"
echo "=================================================="
echo

# Create results directory
RESULTS_DIR="scripts/rebranding/analysis-results"
mkdir -p "$RESULTS_DIR"
cd "$RESULTS_DIR"

echo -e "${YELLOW}📊 PHASE 1: Code Reference Analysis${NC}"
echo "------------------------------------"

# 1. CRITICAL: Package declarations that should be changed
echo -e "${RED}🚨 CRITICAL: Old package declarations:${NC}"
find ../../.. -name "*.java" -o -name "*.kt" | \
    grep -v ".git" | grep -v "build/" | grep -v "search-results" | \
    xargs grep -l "^package.*org\.briarproject" 2>/dev/null | \
    tee critical-package-declarations.txt | wc -l
echo " files found"

# 2. CRITICAL: Import statements that should be changed
echo -e "${RED}🚨 CRITICAL: Old import statements:${NC}"
find ../../.. -name "*.java" -o -name "*.kt" | \
    grep -v ".git" | grep -v "build/" | grep -v "search-results" | \
    xargs grep -l "^import.*org\.briarproject\.(briar|bramble)" 2>/dev/null | \
    tee critical-import-statements.txt | wc -l
echo " files found"

# 3. CRITICAL: Class names with Briar/Bramble
echo -e "${RED}🚨 CRITICAL: Class names to rename:${NC}"
find ../../.. -name "*.java" -o -name "*.kt" | \
    grep -v ".git" | grep -v "build/" | grep -v "search-results" | \
    xargs grep -l "class.*\(Briar\|Bramble\)" 2>/dev/null | \
    tee critical-class-names.txt | wc -l
echo " files found"

# 4. CRITICAL: File names with briar/bramble
echo -e "${RED}🚨 CRITICAL: Files to rename:${NC}"
find ../../.. -name "*briar*" -o -name "*bramble*" | \
    grep -v ".git" | grep -v "build/" | grep -v "search-results" | \
    grep -v "\.jar$" | grep -v "libs/" | \
    tee critical-file-names.txt | wc -l
echo " files found"

echo
echo -e "${YELLOW}📊 PHASE 2: Configuration Reference Analysis${NC}"
echo "----------------------------------------------"

# 5. AndroidManifest references
echo -e "${YELLOW}📱 AndroidManifest.xml references:${NC}"
find ../../.. -name "AndroidManifest.xml" | \
    xargs grep -l "briar\|bramble" 2>/dev/null | \
    tee config-android-manifest.txt | wc -l
echo " files found"

# 6. Build script references
echo -e "${YELLOW}🔧 Build script references:${NC}"
find ../../.. -name "*.gradle" -o -name "*.properties" | \
    grep -v ".git" | grep -v "build/" | \
    xargs grep -l "briar\|bramble" 2>/dev/null | \
    tee config-build-scripts.txt | wc -l
echo " files found"

# 7. Fastlane and deployment references
echo -e "${YELLOW}🚀 Deployment references:${NC}"
find ../../.. -path "*/fastlane/*" -type f | \
    xargs grep -l "briar\|bramble" 2>/dev/null | \
    tee config-deployment.txt | wc -l
echo " files found"

echo
echo -e "${YELLOW}📊 PHASE 3: Acceptable Reference Analysis${NC}"
echo "-----------------------------------------"

# 8. External dependencies (should NOT be changed)
echo -e "${GREEN}✅ External dependencies (OK to keep):${NC}"
find ../../.. -name "*.java" -o -name "*.kt" | \
    xargs grep -l "org\.briarproject\.nullsafety\|org\.briarproject\.android\.dontkillmelib" 2>/dev/null | \
    tee acceptable-external-deps.txt | wc -l
echo " files found"

# 9. JAR files (should NOT be changed)
echo -e "${GREEN}✅ JAR files (OK to keep):${NC}"
find ../../.. -name "*briar*.jar" -o -name "*bramble*.jar" | \
    tee acceptable-jar-files.txt | wc -l
echo " files found"

# 10. Documentation files (should NOT be changed)
echo -e "${GREEN}✅ Documentation files (OK to keep):${NC}"
find ../../.. -path "*/docs/*" -name "*.md" -o -path "*/docs/*" -name "*.txt" | \
    xargs grep -l "briar\|bramble" 2>/dev/null | \
    tee acceptable-documentation.txt | wc -l
echo " files found"

echo
echo -e "${BLUE}📊 SUMMARY OF CRITICAL ISSUES${NC}"
echo "=============================="

critical_issues=0

# Count critical issues
package_count=$(wc -l < critical-package-declarations.txt 2>/dev/null || echo "0")
import_count=$(wc -l < critical-import-statements.txt 2>/dev/null || echo "0")
class_count=$(wc -l < critical-class-names.txt 2>/dev/null || echo "0")
file_count=$(wc -l < critical-file-names.txt 2>/dev/null || echo "0")
manifest_count=$(wc -l < config-android-manifest.txt 2>/dev/null || echo "0")
build_count=$(wc -l < config-build-scripts.txt 2>/dev/null || echo "0")
deployment_count=$(wc -l < config-deployment.txt 2>/dev/null || echo "0")

critical_issues=$((package_count + import_count + class_count + file_count + manifest_count + build_count + deployment_count))

echo -e "${RED}🚨 CRITICAL ISSUES REQUIRING IMMEDIATE FIX:${NC}"
echo "  Package declarations: $package_count"
echo "  Import statements: $import_count"
echo "  Class names: $class_count"
echo "  File names: $file_count"
echo "  AndroidManifest: $manifest_count"
echo "  Build scripts: $build_count"
echo "  Deployment configs: $deployment_count"
echo "  --------------------------------"
echo -e "  ${RED}TOTAL CRITICAL: $critical_issues${NC}"

echo
if [ $critical_issues -eq 0 ]; then
    echo -e "${GREEN}✅ SUCCESS: No critical rebranding issues found!${NC}"
else
    echo -e "${RED}❌ INCOMPLETE: $critical_issues critical issues need fixing${NC}"
    echo
    echo -e "${YELLOW}📋 DETAILED RESULTS:${NC}"
    echo "All results saved in: $(pwd)"
    echo "Review these files for specific items to fix:"
    echo "  - critical-*.txt: Items that MUST be changed"
    echo "  - config-*.txt: Configuration items that MUST be changed"
    echo "  - acceptable-*.txt: Items that should NOT be changed"
fi

echo
echo -e "${BLUE}🏁 Analysis complete!${NC}"

# Return to original directory
cd ../../..