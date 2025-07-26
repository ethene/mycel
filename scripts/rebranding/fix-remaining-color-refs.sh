#!/bin/bash

# Fix Remaining Color References - Including Text Colors and Drawables
echo "🎨 FIXING REMAINING COLOR REFERENCES"
echo "==================================="
echo ""

# Create backup directory
BACKUP_DIR="scripts/rebranding/remaining-colors-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"
echo "📁 Backup directory: $BACKUP_DIR"

echo ""
echo "🔧 FIXING TEXT COLOR REFERENCES"
echo "==============================="

# Find all files with briar text color references
find mycel-android/src/main/res -name "*.xml" -exec grep -l "briar_text_" {} \; | while read file; do
    echo "  📄 Updating text colors in: $(basename "$file")"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-text" 2>/dev/null
    
    # Update text color references
    sed -i '' 's/@color\/briar_text_link/@color/mycel_accent/g' "$file"
    sed -i '' 's/@color\/briar_text_link_inverse/@color/mycel_text_invert/g' "$file"
    sed -i '' 's/@color\/briar_text_primary/@color/mycel_text_main/g' "$file"
    sed -i '' 's/@color\/briar_text_primary_inverse/@color/mycel_text_invert/g' "$file"
    sed -i '' 's/@color\/briar_text_secondary_inverse/@color/mycel_text_invert/g' "$file"
    sed -i '' 's/@color\/briar_text_tertiary_inverse/@color/mycel_text_invert/g' "$file"
done

echo ""
echo "🔧 FIXING DRAWABLE COLOR REFERENCES"
echo "==================================="

# Find drawable files with briar color references
find mycel-android/src/main/res/drawable* -name "*.xml" -exec grep -l "@color/briar_" {} \; | while read file; do
    echo "  🎨 Updating drawable colors in: $(basename "$file")"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-drawable" 2>/dev/null
    
    # Update drawable color references
    sed -i '' 's/@color\/briar_accent/@color/mycel_accent/g' "$file"
    sed -i '' 's/@color\/briar_primary/@color/mycel_primary/g' "$file"
    sed -i '' 's/@color\/briar_text_primary/@color/mycel_text_main/g' "$file"
    sed -i '' 's/@color\/briar_text_primary_inverse/@color/mycel_text_invert/g' "$file"
    sed -i '' 's/@color\/briar_text_link/@color/mycel_accent/g' "$file"
    sed -i '' 's/@color\/briar_text_link_inverse/@color/mycel_text_invert/g' "$file"
    
    # Update other briar color variants
    sed -i '' 's/@color\/briar_lime_950/@color/mycel_green_dark/g' "$file"
    sed -i '' 's/@color\/briar_lime_600/@color/mycel_green/g' "$file"
    sed -i '' 's/@color\/briar_lime_500/@color/mycel_primary/g' "$file"
    sed -i '' 's/@color\/briar_lime_400/@color/mycel_secondary/g' "$file"
    sed -i '' 's/@color\/briar_blue_900/@color/mycel_blue_dark/g' "$file"
    sed -i '' 's/@color\/briar_blue_800/@color/mycel_blue_dark/g' "$file"
    sed -i '' 's/@color\/briar_blue_600/@color/mycel_primary/g' "$file"
    sed -i '' 's/@color\/briar_blue_400/@color/mycel_accent/g' "$file"
    sed -i '' 's/@color\/briar_blue_300/@color/mycel_blue_soft/g' "$file"
    sed -i '' 's/@color\/briar_blue_100/@color/mycel_blue_soft/g' "$file"
    sed -i '' 's/@color\/briar_orange_500/@color/mycel_warning/g' "$file"
    sed -i '' 's/@color\/briar_orange_400/@color/mycel_coral/g' "$file"
    sed -i '' 's/@color\/briar_orange_200/@color/mycel_coral_soft/g' "$file"
    sed -i '' 's/@color\/briar_red_500/@color/mycel_error/g' "$file"
    sed -i '' 's/@color\/briar_red_400/@color/mycel_coral/g' "$file"
    sed -i '' 's/@color\/briar_red_600/@color/mycel_error/g' "$file"
    sed -i '' 's/@color\/briar_gray_900/@color/mycel_text_main/g' "$file"
    sed -i '' 's/@color\/briar_gray_700/@color/mycel_ui_muted/g' "$file"
    sed -i '' 's/@color\/briar_gray_500/@color/mycel_ui_muted/g' "$file"
    sed -i '' 's/@color\/briar_gray_300/@color/mycel_ui_border/g' "$file"
    sed -i '' 's/@color\/briar_gray_200/@color/mycel_ui_border/g' "$file"
    sed -i '' 's/@color\/briar_gray_100/@color/mycel_bg_light/g' "$file"
done

echo ""
echo "🔧 FIXING REMAINING LAYOUT REFERENCES"
echo "====================================="

# Handle any remaining layout references
find mycel-android/src/main/res/layout -name "*.xml" -exec grep -l "@color/briar_" {} \; | while read file; do
    echo "  📄 Final layout update: $(basename "$file")"
    cp "$file" "$BACKUP_DIR/$(basename "$file")-final" 2>/dev/null
    
    # Update any remaining color references
    sed -i '' 's/@color\/briar_text_link/@color/mycel_accent/g' "$file"
    sed -i '' 's/@color\/briar_text_link_inverse/@color/mycel_text_invert/g' "$file"
    sed -i '' 's/@color\/briar_text_primary/@color/mycel_text_main/g' "$file"
    sed -i '' 's/@color\/briar_text_primary_inverse/@color/mycel_text_invert/g' "$file"
    sed -i '' 's/@color\/briar_text_secondary_inverse/@color/mycel_text_invert/g' "$file"
    sed -i '' 's/@color\/briar_text_tertiary_inverse/@color/mycel_text_invert/g' "$file"
done

echo ""
echo "📊 FINAL VERIFICATION"
echo "===================="

# Count remaining references
REMAINING_TOTAL=$(find mycel-android/src/main/res -name "*.xml" -exec grep -c "@color/briar_" {} \; | awk '{sum+=$1} END {print sum+0}')

echo "📈 FINAL RESULTS:"
echo "  Total remaining @color/briar_ refs: $REMAINING_TOTAL"

if [[ $REMAINING_TOTAL -eq 0 ]]; then
    echo ""
    echo "🎉 COMPLETE SUCCESS!"
    echo "✅ All briar color references updated to Mycel scheme"
    echo "🎨 Now using proper Mycel color palette:"
    echo "   • Primary: Spore Blue (#A3BEEA)"
    echo "   • Accent: Signal Moss Green (#A8C8B3)"
    echo "   • Error: Sporeset Coral (#E8A6A1)"
    echo "   • Text: Pure black (#000000) and white (#FFFFFF)"
    echo "   • Backgrounds: Warm off-white (#F5F4F1) and soft black (#1D1D1B)"
else
    echo ""
    echo "📋 Some references still remain - checking details:"
    find mycel-android/src/main/res -name "*.xml" -exec grep -Hn "@color/briar_" {} \; | head -5
fi

echo ""
echo "📁 All backups saved to: $BACKUP_DIR"