#!/bin/bash

# Convert Briar → Mycel (single module at a time)
set -e

MODULES=("briar-api" "briar-core" "briar-android" "briar-headless")

for module in "${MODULES[@]}"; do
    if [ -d "$module" ]; then
        echo "🔄 Processing $module..."
        
        # Update package declarations and imports
        find "$module" -name "*.java" -type f -exec sed -i.bak 's|org\.briarproject\.briar|com.quantumresearch.mycel.app|g' {} \;
        
        # Update build.gradle if exists
        if [ -f "$module/build.gradle" ]; then
            sed -i.bak 's|org\.briarproject\.briar|com.quantumresearch.mycel.app|g' "$module/build.gradle"
            rm -f "$module/build.gradle.bak"
        fi
        
        # Clean up backup files
        find "$module" -name "*.bak" -delete
        
        echo "✅ $module complete"
    fi
done

echo "📱 Briar → Mycel conversion complete!"