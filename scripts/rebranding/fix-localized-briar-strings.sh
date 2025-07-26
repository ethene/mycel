#!/bin/bash

# Fix Localized Briar String Names
# Systematically replace briar_* string names with mycel_* equivalents
# across all localized Android string files

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
LOG_FILE="$SCRIPT_DIR/localized-string-replacement.log"

# Initialize log
echo "$(date): Starting localized briar string replacement" > "$LOG_FILE"

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 SYSTEMATIC BRIAR → MYCEL STRING REPLACEMENT${NC}"
echo -e "${BLUE}===============================================${NC}"

# Define the string mappings (using arrays for macOS bash compatibility)
OLD_STRINGS=("briar_changelog" "briar_crashed" "briar_privacy_policy" "briar_source_code" "briar_version" "briar_website")
NEW_STRINGS=("mycel_changelog" "mycel_crashed" "mycel_privacy_policy" "mycel_source_code" "mycel_version" "mycel_website")

# Find all localized string files
echo -e "${YELLOW}📂 Finding localized string files...${NC}"
STRING_FILES=$(find "$PROJECT_ROOT/mycel-android/src/main/res" -name "strings.xml" -path "*/values-*/strings.xml")
MAIN_STRING_FILE="$PROJECT_ROOT/mycel-android/src/main/res/values/strings.xml"

# Count files
TOTAL_FILES=$(echo "$STRING_FILES" | wc -l)
echo -e "${GREEN}Found $TOTAL_FILES localized string files${NC}"

# Function to backup a file
backup_file() {
    local file="$1"
    local backup="${file}.backup-$(date +%Y%m%d-%H%M%S)"
    cp "$file" "$backup"
    echo "Backed up: $backup" >> "$LOG_FILE"
}

# Function to replace strings in a file
replace_strings_in_file() {
    local file="$1"
    local changes_made=0
    
    echo -e "${BLUE}Processing: ${file#$PROJECT_ROOT/}${NC}"
    
    # Create backup
    backup_file "$file"
    
    # Apply replacements using array indices
    for i in "${!OLD_STRINGS[@]}"; do
        old_string="${OLD_STRINGS[$i]}"
        new_string="${NEW_STRINGS[$i]}"
        
        # Count occurrences before replacement
        before_count=$(grep -c "name=\"$old_string\"" "$file" 2>/dev/null || echo "0")
        
        if [ "$before_count" -gt 0 ]; then
            echo -e "  ${YELLOW}→${NC} Replacing $before_count occurrence(s) of '$old_string' with '$new_string'"
            
            # Perform replacement
            sed -i '' "s/name=\"$old_string\"/name=\"$new_string\"/g" "$file"
            
            # Verify replacement
            after_count=$(grep -c "name=\"$new_string\"" "$file" 2>/dev/null || echo "0")
            remaining_old=$(grep -c "name=\"$old_string\"" "$file" 2>/dev/null || echo "0")
            
            if [ "$remaining_old" -eq 0 ] && [ "$after_count" -eq "$before_count" ]; then
                echo -e "    ${GREEN}✅ Success: $before_count → $after_count${NC}"
                echo "$(date): $file - Replaced $before_count occurrences of $old_string with $new_string" >> "$LOG_FILE"
                changes_made=$((changes_made + before_count))
            else
                echo -e "    ${RED}❌ Error: Expected $before_count, got $after_count new, $remaining_old old remaining${NC}"
                echo "$(date): ERROR in $file - Replacement failed for $old_string" >> "$LOG_FILE"
            fi
        fi
    done
    
    if [ "$changes_made" -eq 0 ]; then
        echo -e "  ${GREEN}No briar_ strings found in this file${NC}"
    else
        echo -e "  ${GREEN}✅ Total changes: $changes_made${NC}"
    fi
    
    echo
    return $changes_made
}

# Process all localized string files
echo -e "${YELLOW}🔄 Processing localized string files...${NC}"
echo

total_changes=0
processed_files=0

for file in $STRING_FILES; do
    if [ -f "$file" ]; then
        replace_strings_in_file "$file"
        file_changes=$?
        total_changes=$((total_changes + file_changes))
        processed_files=$((processed_files + 1))
    else
        echo -e "${RED}❌ File not found: $file${NC}"
    fi
done

# Summary
echo -e "${BLUE}===============================================${NC}"
echo -e "${GREEN}📊 REPLACEMENT SUMMARY${NC}"
echo -e "${GREEN}Files processed: $processed_files${NC}"
echo -e "${GREEN}Total string replacements: $total_changes${NC}"

# Log summary
echo "$(date): Completed - $processed_files files processed, $total_changes total replacements" >> "$LOG_FILE"

if [ "$total_changes" -gt 0 ]; then
    echo -e "${YELLOW}📝 Backups created for all modified files${NC}"
    echo -e "${YELLOW}📋 Detailed log: $LOG_FILE${NC}"
    echo
    echo -e "${GREEN}🚀 Ready to test build with updated localized strings!${NC}"
else
    echo -e "${YELLOW}⚠️  No briar_ strings found to replace${NC}"
fi

echo -e "${BLUE}===============================================${NC}"