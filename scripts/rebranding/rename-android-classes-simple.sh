#!/bin/bash

# Systematic Android Class Renaming Script (macOS compatible)
# Renames all Android UI classes from Briar* to Mycel*
# Usage: ./scripts/rebranding/rename-android-classes-simple.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 ANDROID CLASS RENAMING${NC}"
echo "========================="
echo

ANDROID_ROOT="mycel-android"

# Function to rename a class
rename_class() {
    local old_name="$1"
    local new_name="$2"
    
    echo -e "${YELLOW}🔄 Renaming $old_name → $new_name${NC}"
    
    # Find files containing the class definition
    found_files=$(find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" | \
        xargs grep -l "class $old_name\|interface $old_name" 2>/dev/null || true)
    
    if [ -n "$found_files" ]; then
        echo "$found_files" | while IFS= read -r file; do
            echo "  📝 Updating: $file"
            
            # Update class/interface definition
            sed -i '' "s/class $old_name/class $new_name/g" "$file"
            sed -i '' "s/interface $old_name/interface $new_name/g" "$file"
            
            # Update constructor references
            sed -i '' "s/public $old_name(/public $new_name(/g" "$file"
            
            # Rename file if it matches the class name
            dir=$(dirname "$file")
            filename=$(basename "$file")
            if [[ "$filename" == "$old_name.java" ]] || [[ "$filename" == "$old_name.kt" ]]; then
                new_filename="$new_name.${filename##*.}"
                echo "  📄 Renaming file: $filename → $new_filename"
                mv "$file" "$dir/$new_filename"
            fi
        done
        echo -e "  ${GREEN}✅ Updated class definition${NC}"
    else
        echo -e "  ${YELLOW}⚠️  No class definition found${NC}"
    fi
    
    # Update all references to the class
    echo "  🔍 Updating references..."
    find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" -o -name "*.xml" | \
        xargs sed -i '' "s/\\b$old_name\\b/$new_name/g" 2>/dev/null || true
    
    echo
}

# Rename all the classes
rename_class "BriarActivity" "MycelActivity"
rename_class "BriarAdapter" "MycelAdapter" 
rename_class "BriarButton" "MycelButton"
rename_class "BriarController" "MycelController"
rename_class "BriarControllerImpl" "MycelControllerImpl"
rename_class "BriarDataFetcherFactory" "MycelDataFetcherFactory"
rename_class "BriarGlideModule" "MycelGlideModule"
rename_class "BriarImageTransformation" "MycelImageTransformation"
rename_class "BriarModelLoader" "MycelModelLoader"
rename_class "BriarNotificationBuilder" "MycelNotificationBuilder"
rename_class "BriarRecyclerView" "MycelRecyclerView"
rename_class "BriarRecyclerViewScrollListener" "MycelRecyclerViewScrollListener"
rename_class "BriarSnackbarBuilder" "MycelSnackbarBuilder"
rename_class "BriarTestComponentApplication" "MycelTestComponentApplication"
rename_class "BriarUiTestComponent" "MycelUiTestComponent"

echo -e "${GREEN}🎉 ANDROID CLASS RENAMING COMPLETE${NC}"
echo "=================================="

echo -e "${BLUE}🔍 Verification${NC}"
echo "=============="
echo "Checking for any remaining Briar class references..."

remaining=$(find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" | \
    xargs grep -l "class.*Briar\|interface.*Briar" 2>/dev/null | wc -l | xargs)

if [ "$remaining" -eq 0 ]; then
    echo -e "${GREEN}✅ No remaining Briar class definitions found${NC}"
else
    echo -e "${RED}⚠️  $remaining files still have Briar class definitions${NC}"
    find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" | \
        xargs grep -l "class.*Briar\|interface.*Briar" 2>/dev/null || true
fi