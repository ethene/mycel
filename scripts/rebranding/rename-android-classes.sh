#!/bin/bash

# Systematic Android Class Renaming Script
# Renames all Android UI classes from Briar* to Mycel*
# Usage: ./scripts/rebranding/rename-android-classes.sh

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

# Android classes to rename (Briar* -> Mycel*)
declare -A RENAMES=(
    ["BriarActivity"]="MycelActivity"
    ["BriarAdapter"]="MycelAdapter" 
    ["BriarButton"]="MycelButton"
    ["BriarController"]="MycelController"
    ["BriarControllerImpl"]="MycelControllerImpl"
    ["BriarDataFetcherFactory"]="MycelDataFetcherFactory"
    ["BriarGlideModule"]="MycelGlideModule"
    ["BriarImageTransformation"]="MycelImageTransformation"
    ["BriarModelLoader"]="MycelModelLoader"
    ["BriarNotificationBuilder"]="MycelNotificationBuilder"
    ["BriarRecyclerView"]="MycelRecyclerView"
    ["BriarRecyclerViewScrollListener"]="MycelRecyclerViewScrollListener"
    ["BriarSnackbarBuilder"]="MycelSnackbarBuilder"
    ["BriarTestComponentApplication"]="MycelTestComponentApplication"
    ["BriarUiTestComponent"]="MycelUiTestComponent"
)

ANDROID_ROOT="mycel-android"
total_renamed=0

for old_name in "${!RENAMES[@]}"; do
    new_name="${RENAMES[$old_name]}"
    
    echo -e "${YELLOW}🔄 Renaming $old_name → $new_name${NC}"
    
    # Find files containing the class definition
    found_files=$(find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" | \
        xargs grep -l "class $old_name\|interface $old_name" 2>/dev/null || true)
    
    if [ -n "$found_files" ]; then
        while IFS= read -r file; do
            echo "  📝 Updating: $file"
            
            # Create backup
            cp "$file" "$file.backup"
            
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
                git mv "$file" "$dir/$new_filename" 2>/dev/null || mv "$file" "$dir/$new_filename"
                rm "$file.backup" 2>/dev/null || true
            else
                rm "$file.backup"
            fi
            
        done <<< "$found_files"
        
        echo -e "  ${GREEN}✅ Updated class definition${NC}"
    else
        echo -e "  ${YELLOW}⚠️  No class definition found${NC}"
    fi
    
    # Update all references to the class
    echo "  🔍 Updating references..."
    find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" -o -name "*.xml" | \
        xargs sed -i '' "s/$old_name/$new_name/g" 2>/dev/null || true
    
    total_renamed=$((total_renamed + 1))
    echo
done

echo -e "${GREEN}🎉 ANDROID CLASS RENAMING COMPLETE${NC}"
echo "=================================="
echo "  Processed: $total_renamed class names"
echo "  Next steps:"
echo "    1. Run tests to verify changes"
echo "    2. Check for any remaining references"
echo "    3. Update imports in other modules if needed"
echo

echo -e "${BLUE}🔍 Verification${NC}"
echo "=============="
echo "Checking for any remaining Briar class references..."

remaining=$(find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" | \
    xargs grep -l "class.*Briar\|interface.*Briar" 2>/dev/null | wc -l)

if [ "$remaining" -eq 0 ]; then
    echo -e "${GREEN}✅ No remaining Briar class definitions found${NC}"
else
    echo -e "${RED}⚠️  $remaining files still have Briar class definitions${NC}"
    find "$ANDROID_ROOT" -name "*.java" -o -name "*.kt" | \
        xargs grep -l "class.*Briar\|interface.*Briar" 2>/dev/null || true
fi