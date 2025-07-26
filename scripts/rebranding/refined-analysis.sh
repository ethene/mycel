#!/bin/bash

# Refined Briar/Bramble Reference Analysis Script
# Focuses on actual code issues that need fixing, excluding acceptable references
# Usage: ./scripts/rebranding/refined-analysis.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 REFINED BRIAR/BRAMBLE REFERENCE ANALYSIS${NC}"
echo "=============================================="
echo

# Create results directory
RESULTS_DIR="scripts/rebranding/refined-results"
mkdir -p "$RESULTS_DIR"
cd "$RESULTS_DIR"

echo -e "${YELLOW}🎯 FOCUSING ON ACTUAL CODE ISSUES${NC}"
echo "-----------------------------------"

# 1. Actual class definitions (not test classes extending external frameworks)
echo -e "${RED}🚨 Class definitions to rename:${NC}"
find ../../.. -name "*.java" -o -name "*.kt" | \
    grep -v ".git" | grep -v "build/" | grep -v "search-results" | grep -v "test/" | \
    xargs grep -l "^public.*class.*\(Briar\|Bramble\)" 2>/dev/null | \
    tee actual-class-definitions.txt
count=$(wc -l < actual-class-definitions.txt 2>/dev/null || echo "0")
echo "  Found: $count classes"

# 2. Interface definitions
echo -e "${RED}🚨 Interface definitions to rename:${NC}"
find ../../.. -name "*.java" -o -name "*.kt" | \
    grep -v ".git" | grep -v "build/" | grep -v "search-results" | grep -v "test/" | \
    xargs grep -l "^public.*interface.*\(Briar\|Bramble\)" 2>/dev/null | \
    tee actual-interface-definitions.txt
count=$(wc -l < actual-interface-definitions.txt 2>/dev/null || echo "0")
echo "  Found: $count interfaces"

# 3. References to renamed classes (excluding external frameworks)
echo -e "${RED}🚨 References to classes we renamed:${NC}"
find ../../.. -name "*.java" -o -name "*.kt" | \
    grep -v ".git" | grep -v "build/" | grep -v "search-results" | \
    xargs grep -l "\(BriarService\|BrambleService\|BriarApplication\|BrambleApplication\)" 2>/dev/null | \
    grep -v "TestCase" | \
    tee actual-class-references.txt
count=$(wc -l < actual-class-references.txt 2>/dev/null || echo "0")
echo "  Found: $count files with references"

# 4. Main source files (non-test) with briar/bramble in filename
echo -e "${RED}🚨 Main source files to rename:${NC}"
find ../../.. -path "*/src/main/*" -name "*briar*" -o -path "*/src/main/*" -name "*bramble*" | \
    grep -v ".git" | grep -v "build/" | grep -v "libs/" | grep -v "\.jar$" | \
    tee actual-main-files.txt
count=$(wc -l < actual-main-files.txt 2>/dev/null || echo "0")
echo "  Found: $count main source files"

# 5. Configuration files needing updates
echo -e "${YELLOW}⚙️  Configuration files needing updates:${NC}"

# AndroidManifest
find ../../.. -name "AndroidManifest.xml" | \
    xargs grep -l "BriarService\|BrambleService" 2>/dev/null | \
    tee config-manifests.txt
count=$(wc -l < config-manifests.txt 2>/dev/null || echo "0")
echo "  AndroidManifest files: $count"

# Build scripts with applicationId
find ../../.. -name "build.gradle" | \
    xargs grep -l "org\.briarproject\.briar\.android\|applicationId.*briar" 2>/dev/null | \
    tee config-app-ids.txt
count=$(wc -l < config-app-ids.txt 2>/dev/null || echo "0")
echo "  Build files with app IDs: $count"

# Fastlane configs
find ../../.. -path "*/fastlane/*" -type f | \
    xargs grep -l "org\.briarproject\.briar\.android" 2>/dev/null | \
    tee config-fastlane.txt
count=$(wc -l < config-fastlane.txt 2>/dev/null || echo "0")
echo "  Fastlane config files: $count"

echo
echo -e "${GREEN}✅ ACCEPTABLE REFERENCES (should NOT change):${NC}"
echo "----------------------------------------------------"

# External test framework usage (acceptable)
echo -e "${GREEN}  External test frameworks:${NC}"
find ../../.. -name "*.java" | \
    xargs grep -l "extends.*BrambleTestCase\|extends.*BrambleMockTestCase" 2>/dev/null | \
    wc -l | sed 's/^/    /'
echo "    files (using external test framework - OK)"

# External nullsafety annotations (acceptable)
echo -e "${GREEN}  External annotations:${NC}"
find ../../.. -name "*.java" | \
    xargs grep -l "org\.briarproject\.nullsafety" 2>/dev/null | \
    wc -l | sed 's/^/    /'
echo "    files (using external annotations - OK)"

# JAR files (acceptable)
echo -e "${GREEN}  JAR dependencies:${NC}"
find ../../.. -name "*briar*.jar" -o -name "*bramble*.jar" | \
    wc -l | sed 's/^/    /'
echo "    JAR files (external dependencies - OK)"

echo
echo -e "${BLUE}📋 SUMMARY OF ACTUAL ISSUES${NC}"
echo "=========================="

# Count real issues
class_defs=$(wc -l < actual-class-definitions.txt 2>/dev/null || echo "0")
interface_defs=$(wc -l < actual-interface-definitions.txt 2>/dev/null || echo "0")
class_refs=$(wc -l < actual-class-references.txt 2>/dev/null || echo "0")
main_files=$(wc -l < actual-main-files.txt 2>/dev/null || echo "0")
manifests=$(wc -l < config-manifests.txt 2>/dev/null || echo "0")
app_ids=$(wc -l < config-app-ids.txt 2>/dev/null || echo "0")
fastlane=$(wc -l < config-fastlane.txt 2>/dev/null || echo "0")

total_issues=$((class_defs + interface_defs + class_refs + main_files + manifests + app_ids + fastlane))

echo -e "${RED}🎯 REAL ISSUES NEEDING FIXES:${NC}"
echo "  Class definitions: $class_defs"
echo "  Interface definitions: $interface_defs"
echo "  Class references: $class_refs"
echo "  Main source files: $main_files"
echo "  AndroidManifest files: $manifests"
echo "  Build app IDs: $app_ids"
echo "  Fastlane configs: $fastlane"
echo "  -------------------------"
echo -e "  ${RED}TOTAL REAL ISSUES: $total_issues${NC}"

echo
if [ $total_issues -eq 0 ]; then
    echo -e "${GREEN}🎉 SUCCESS: All real rebranding issues are fixed!${NC}"
else
    echo -e "${YELLOW}📝 ACTION ITEMS:${NC}"
    echo "  1. Review files in: $(pwd)"
    echo "  2. Fix the $total_issues identified issues"
    echo "  3. Re-run this script to verify fixes"
fi

echo
echo -e "${BLUE}🏁 Refined analysis complete!${NC}"

# Return to original directory
cd ../../..