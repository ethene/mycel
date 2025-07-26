#!/bin/bash

# Convert Bramble → Spore (single module at a time)
set -e

MODULES=("bramble-api" "bramble-core" "bramble-android" "bramble-java")

for module in "${MODULES[@]}"; do
    if [ -d "$module" ]; then
        echo "🔄 Processing $module..."
        
        # Update package declarations and imports
        find "$module" -name "*.java" -type f -exec sed -i.bak 's|org\.briarproject\.bramble|com.quantumresearch.mycel.spore|g' {} \;
        
        # Update build.gradle if exists
        if [ -f "$module/build.gradle" ]; then
            sed -i.bak 's|org\.briarproject\.bramble|com.quantumresearch.mycel.spore|g' "$module/build.gradle"
            rm -f "$module/build.gradle.bak"
        fi
        
        # Clean up backup files
        find "$module" -name "*.bak" -delete
        
        echo "✅ $module complete"
    fi
done

echo "🍄 Bramble → Spore conversion complete!"