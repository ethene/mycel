#!/bin/bash

# Fix XML References - Update all XML files with hardcoded org.briarproject references
# This fixes the runtime error where FragmentContainerView can't find ContactListFragment

echo "🔧 Fixing XML layout and resource files with hardcoded org.briarproject references..."

# Create backup directory
BACKUP_DIR="scripts/rebranding/xml-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

# Find all XML files with org.briarproject references
XML_FILES=$(find mycel-android/src/main/res -name "*.xml" -exec grep -l "org\.briarproject" {} \;)

echo "📁 Found $(echo "$XML_FILES" | wc -l) XML files to update"

# Function to backup and update file
fix_xml_file() {
    local file="$1"
    local backup_file="$BACKUP_DIR/$(basename "$file")"
    
    echo "  📝 Fixing: $file"
    
    # Create backup
    cp "$file" "$backup_file"
    
    # Replace org.briarproject.briar.android with com.quantumresearch.mycel.app.android
    sed -i '' 's/org\.briarproject\.briar\.android/com.quantumresearch.mycel.app.android/g' "$file"
    
    # Replace org.briarproject.bramble with com.quantumresearch.mycel.spore
    sed -i '' 's/org\.briarproject\.bramble/com.quantumresearch.mycel.spore/g' "$file"
}

# Process each XML file
for file in $XML_FILES; do
    fix_xml_file "$file"
done

echo ""
echo "✅ XML References Fixed!"
echo "📁 Backups stored in: $BACKUP_DIR"
echo ""
echo "🧪 Key fixes applied:"
echo "  • ContactListFragment reference in activity_nav_drawer.xml"
echo "  • SettingsFragment reference in activity_settings.xml"
echo "  • All custom view references (BriarRecyclerView, MycelButton, etc.)"
echo "  • Fragment references in hotspot layouts"
echo "  • Preference fragment references in settings XML"
echo ""
echo "⚠️  Run 'make clean && make build' to verify XML inflation works correctly"