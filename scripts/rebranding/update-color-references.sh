#!/bin/bash

# Update Color References from briar_ to mycel_ using proper Mycel color scheme
echo "🎨 UPDATING COLOR REFERENCES TO MYCEL SCHEME"
echo "============================================="
echo ""

# Create backup directory
BACKUP_DIR="scripts/rebranding/color-update-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"
echo "📁 Backup directory: $BACKUP_DIR"

echo ""
echo "📋 CURRENT MYCEL COLOR MAPPINGS:"
echo "================================"
echo "  briar_accent        → mycel_accent (green #A8C8B3)"
echo "  briar_primary       → mycel_primary (blue #A3BEEA)"
echo "  briar_lime_*        → mycel_green family"
echo "  briar_blue_*        → mycel_blue family"
echo "  briar_red_*         → mycel_coral family"
echo "  briar_orange_*      → mycel_coral family"
echo "  briar_gray_*        → mycel neutral family"

echo ""
echo "🔧 UPDATING LAYOUT FILES"
echo "========================"

# Find all layout files with briar color references
LAYOUT_FILES=$(find mycel-android/src/main/res/layout -name "*.xml" -exec grep -l "@color/briar_" {} \;)

if [[ -n "$LAYOUT_FILES" ]]; then
    echo "$LAYOUT_FILES" | while read file; do
        echo "  📄 Updating: $(basename "$file")"
        cp "$file" "$BACKUP_DIR/$(basename "$file")"
        
        # Update color references to use the already-mapped legacy colors
        # These are safe since the color.xml already maps briar_* to mycel_* colors
        sed -i '' 's/@color\/briar_accent/@color\/mycel_accent/g' "$file"
        sed -i '' 's/@color\/briar_primary/@color\/mycel_primary/g' "$file"
        sed -i '' 's/@color\/briar_lime_950/@color\/mycel_green_dark/g' "$file"
        sed -i '' 's/@color\/briar_lime_600/@color\/mycel_green/g' "$file"
        sed -i '' 's/@color\/briar_lime_500/@color\/mycel_primary/g' "$file"
        sed -i '' 's/@color\/briar_lime_400/@color\/mycel_secondary/g' "$file"
        sed -i '' 's/@color\/briar_blue_900/@color\/mycel_blue_dark/g' "$file"
        sed -i '' 's/@color\/briar_blue_800/@color\/mycel_blue_dark/g' "$file"
        sed -i '' 's/@color\/briar_blue_600/@color\/mycel_primary/g' "$file"
        sed -i '' 's/@color\/briar_blue_400/@color\/mycel_accent/g' "$file"
        sed -i '' 's/@color\/briar_blue_300/@color\/mycel_blue_soft/g' "$file"
        sed -i '' 's/@color\/briar_blue_100/@color\/mycel_blue_soft/g' "$file"
        sed -i '' 's/@color\/briar_orange_500/@color\/mycel_warning/g' "$file"
        sed -i '' 's/@color\/briar_orange_400/@color\/mycel_coral/g' "$file"
        sed -i '' 's/@color\/briar_orange_200/@color\/mycel_coral_soft/g' "$file"
        sed -i '' 's/@color\/briar_red_500/@color\/mycel_error/g' "$file"
        sed -i '' 's/@color\/briar_red_400/@color\/mycel_coral/g' "$file"
        sed -i '' 's/@color\/briar_red_600/@color\/mycel_error/g' "$file"
        sed -i '' 's/@color\/briar_gray_900/@color\/mycel_text_main/g' "$file"
        sed -i '' 's/@color\/briar_gray_700/@color\/mycel_ui_muted/g' "$file"
        sed -i '' 's/@color\/briar_gray_500/@color\/mycel_ui_muted/g' "$file"
        sed -i '' 's/@color\/briar_gray_300/@color\/mycel_ui_border/g' "$file"
        sed -i '' 's/@color\/briar_gray_200/@color\/mycel_ui_border/g' "$file"
        sed -i '' 's/@color\/briar_gray_100/@color\/mycel_bg_light/g' "$file"
    done
else
    echo "  ✅ No layout files need color updates"
fi

echo ""
echo "🔧 UPDATING STRING REFERENCES"
echo "============================="

# Find files with briar string references
STRING_FILES=$(find mycel-android/src/main/res -name "*.xml" -exec grep -l "@string/briar" {} \;)

if [[ -n "$STRING_FILES" ]]; then
    echo "$STRING_FILES" | while read file; do
        echo "  📄 Updating: $(basename "$file")"
        cp "$file" "$BACKUP_DIR/strings-$(basename "$file")" 2>/dev/null
        
        # Update string references
        sed -i '' 's/@string\/briar_version/@string\/mycel_version/g' "$file"
        sed -i '' 's/@string\/briar_website/@string\/mycel_website/g' "$file"
        sed -i '' 's/@string\/briar_crashed/@string\/mycel_crashed/g' "$file"
        sed -i '' 's/@string\/briar/@string\/mycel/g' "$file"
    done
else
    echo "  ✅ No files need string reference updates"
fi

echo ""
echo "🔧 UPDATING STRINGS.XML DEFINITIONS"
echo "==================================="

# Update the actual string definitions in strings.xml
STRINGS_FILE="mycel-android/src/main/res/values/strings.xml"
if [[ -f "$STRINGS_FILE" ]]; then
    echo "  📄 Updating string definitions in strings.xml"
    cp "$STRINGS_FILE" "$BACKUP_DIR/strings-definitions.xml"
    
    # Update string names and values
    sed -i '' 's/name="briar_version"/name="mycel_version"/g' "$STRINGS_FILE"
    sed -i '' 's/name="briar_website"/name="mycel_website"/g' "$STRINGS_FILE"
    sed -i '' 's/name="briar_crashed"/name="mycel_crashed"/g' "$STRINGS_FILE"
    sed -i '' 's/name="briar"/name="mycel"/g' "$STRINGS_FILE"
    
    # Update string content to reference Mycel instead of Briar
    sed -i '' 's/Briar version/Mycel version/g' "$STRINGS_FILE"
    sed -i '' 's/briarproject\.org/quantumresearch.com.au/g' "$STRINGS_FILE"
    sed -i '' 's/Briar crashed/Mycel crashed/g' "$STRINGS_FILE"
    sed -i '' 's/Briar/Mycel/g' "$STRINGS_FILE"
else
    echo "  ⚠️  strings.xml not found"
fi

echo ""
echo "📊 VERIFICATION"
echo "==============="

# Count remaining references
REMAINING_COLOR=$(find mycel-android/src/main/res -name "*.xml" -exec grep -c "@color/briar_" {} \; | awk '{sum+=$1} END {print sum+0}')
REMAINING_STRING=$(find mycel-android/src/main/res -name "*.xml" -exec grep -c "@string/briar" {} \; | awk '{sum+=$1} END {print sum+0}')

echo "📈 RESULTS:"
echo "  Color references updated: Layout files processed"
echo "  String references updated: Resource files processed"
echo "  Remaining @color/briar_ refs: $REMAINING_COLOR"
echo "  Remaining @string/briar refs: $REMAINING_STRING"

echo ""
if [[ $REMAINING_COLOR -eq 0 && $REMAINING_STRING -eq 0 ]]; then
    echo "✅ SUCCESS: All color and string references updated to Mycel scheme!"
    echo "🎨 Colors now use proper Mycel palette:"
    echo "   • Green accents: #A8C8B3 (mycel_accent)"
    echo "   • Blue primary: #A3BEEA (mycel_primary)" 
    echo "   • Coral errors: #E8A6A1 (mycel_error)"
    echo "   • Neutral tones: Warm off-white and soft black"
else
    echo "⚠️  Some references may still need manual review"
fi

echo ""
echo "📁 All backups saved to: $BACKUP_DIR"
echo "🔧 Run compilation test to verify changes"