#!/bin/bash

# Script to check for remaining Briar/Bramble references in the codebase
# This helps ensure the rebranding to Mycel/Spore is complete

echo "================================================"
echo "Checking for remaining Briar/Bramble references"
echo "================================================"
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Directories to exclude from search
EXCLUDE_DIRS=(
    ".git"
    ".gradle"
    "build"
    "bin"
    ".idea"
    "out"
    "tmp"
    "libs"
    "gradle/wrapper"
)

# Files to exclude from search
EXCLUDE_FILES=(
    "*.jar"
    "*.class"
    "*.png"
    "*.jpg"
    "*.jpeg"
    "*.gif"
    "*.ico"
    "*.pdf"
    "*.zip"
    "*.tar"
    "*.gz"
    "*.lock"
    "gradle-wrapper.properties"
    "gradlew"
    "gradlew.bat"
)

# Build exclude arguments for grep
GREP_EXCLUDE_DIRS=""
for dir in "${EXCLUDE_DIRS[@]}"; do
    GREP_EXCLUDE_DIRS="$GREP_EXCLUDE_DIRS --exclude-dir=$dir"
done

GREP_EXCLUDE_FILES=""
for file in "${EXCLUDE_FILES[@]}"; do
    GREP_EXCLUDE_FILES="$GREP_EXCLUDE_FILES --exclude=$file"
done

# Function to search for a pattern and report results
search_pattern() {
    local pattern=$1
    local description=$2
    local case_sensitive=${3:-true}
    
    echo -e "${YELLOW}Checking for: $description${NC}"
    echo "Pattern: $pattern"
    echo "----------------------------------------"
    
    if [ "$case_sensitive" = true ]; then
        results=$(grep -r "$pattern" . $GREP_EXCLUDE_DIRS $GREP_EXCLUDE_FILES 2>/dev/null | grep -v "check-rebranding.sh")
    else
        results=$(grep -ri "$pattern" . $GREP_EXCLUDE_DIRS $GREP_EXCLUDE_FILES 2>/dev/null | grep -v "check-rebranding.sh")
    fi
    
    if [ -z "$results" ]; then
        echo -e "${GREEN}✓ No matches found${NC}"
    else
        echo -e "${RED}✗ Found matches:${NC}"
        echo "$results" | head -20
        match_count=$(echo "$results" | wc -l)
        if [ $match_count -gt 20 ]; then
            echo -e "${YELLOW}... and $((match_count - 20)) more matches${NC}"
        fi
    fi
    echo
}

# Main checks
echo "1. Checking for 'briar' references (case-insensitive)..."
search_pattern "briar" "References to 'briar' (excluding URLs and expected references)" false | \
    grep -v "briarproject.org" | \
    grep -v "briar://" | \
    grep -v "Briar Mailbox" | \
    grep -v "docs/" | \
    grep -v "README" | \
    grep -v "CHANGELOG" | \
    grep -v "verification-metadata.xml"

echo "2. Checking for 'bramble' references (case-insensitive)..."
search_pattern "bramble" "References to 'bramble'" false | \
    grep -v "docs/" | \
    grep -v "README" | \
    grep -v "CHANGELOG" | \
    grep -v "verification-metadata.xml"

echo "3. Checking for package declarations with old naming..."
search_pattern "^package org\\.briarproject" "Old package declarations" true

echo "4. Checking for imports with old naming..."
search_pattern "^import org\\.briarproject" "Old import statements" true

echo "5. Checking for old class references in strings..."
search_pattern "org\\.briarproject\\.briar" "Old Briar class references in strings" true
search_pattern "org\\.briarproject\\.bramble" "Old Bramble class references in strings" true

echo "6. Checking for old application IDs..."
search_pattern "org\\.briarproject\\.briar\\.android" "Old Android application ID" true

echo "7. Checking for old maven/gradle dependencies..."
search_pattern "org\\.briarproject:" "Old Maven group ID in dependencies" true

echo "8. Checking AndroidManifest files for old references..."
find . -name "AndroidManifest.xml" -not -path "*/build/*" -not -path "*/.gradle/*" | while read -r manifest; do
    echo -e "${YELLOW}Checking: $manifest${NC}"
    grep -H "org\\.briarproject" "$manifest" 2>/dev/null || echo -e "${GREEN}✓ Clean${NC}"
done
echo

echo "9. Checking for hardcoded 'briar://' URLs..."
search_pattern "briar://" "Old Briar URL scheme" true | grep -v "test"

echo "10. Checking for Briar in user-visible strings..."
find . -name "strings.xml" -not -path "*/build/*" -not -path "*/.gradle/*" | while read -r stringsfile; do
    echo -e "${YELLOW}Checking: $stringsfile${NC}"
    grep -H -i ">.*briar.*<" "$stringsfile" 2>/dev/null | grep -v "mycel" || echo -e "${GREEN}✓ Clean${NC}"
done

echo
echo "================================================"
echo "Summary of checks complete"
echo "================================================"
echo
echo "Note: Some references might be intentional (e.g., in documentation, changelogs, or external dependencies)."
echo "Please review any findings to determine if they need to be updated."