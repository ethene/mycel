#!/bin/bash

# Fix Final XML RecyclerView References
echo "🔧 FIXING FINAL XML RECYCLERVIEW REFERENCES"
echo "============================================"

# Create backup directory
BACKUP_DIR="scripts/rebranding/final-xml-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"
echo "📁 Backup directory: $BACKUP_DIR"

# Find and fix all XML files with BriarRecyclerView references
find mycel-android/src/main/res -name "*.xml" -exec grep -l "BriarRecyclerView" {} \; | while read file; do
    echo "  📄 Fixing: $file"
    cp "$file" "$BACKUP_DIR/$(basename "$file")"
    
    # Replace BriarRecyclerView with MycelRecyclerView in XML files
    sed -i '' 's/BriarRecyclerView/MycelRecyclerView/g' "$file"
done

echo ""
echo "✅ All XML RecyclerView references updated!"
echo "📁 Backups saved to: $BACKUP_DIR"