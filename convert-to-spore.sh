#!/bin/bash

# Convert Infrastructure → Spore and Clean Up Remaining References
# This script completes the naming transition to the biological metaphor

set -e  # Exit on any error

echo "🍄 Converting Infrastructure → Spore & Cleaning Remaining References"
echo "=================================================================="

# Phase 1: Convert existing "infrastructure" to "spore"
echo ""
echo "🔄 Phase 1: Converting infrastructure → spore..."

convert_infrastructure_to_spore() {
    local module_path="$1"
    echo "  Converting $module_path: infrastructure → spore"
    
    # Find all Java files and replace infrastructure with spore
    find "$module_path" -name "*.java" -type f | while read -r file; do
        # Replace package declarations
        sed -i.bak 's|com\.quantumresearch\.mycel\.infrastructure|com.quantumresearch.mycel.spore|g' "$file"
        
        # Remove backup files
        rm -f "${file}.bak"
    done
    
    # Update build.gradle if it exists
    if [ -f "$module_path/build.gradle" ]; then
        sed -i.bak 's|com\.quantumresearch\.mycel\.infrastructure|com.quantumresearch.mycel.spore|g' "$module_path/build.gradle"
        rm -f "$module_path/build.gradle.bak"
    fi
}

# Convert infrastructure to spore in all modules
for module in bramble-api bramble-core bramble-android bramble-java briar-api briar-core briar-android briar-headless; do
    if [ -d "$module" ]; then
        convert_infrastructure_to_spore "$module"
    fi
done

# Phase 2: Update directory structure infrastructure → spore
echo ""
echo "🔄 Phase 2: Updating directory structure..."

restructure_infrastructure_to_spore() {
    local module_path="$1"
    echo "  Restructuring $module_path directories"
    
    for src_dir in "src/main/java" "src/test/java"; do
        local base_path="$module_path/$src_dir"
        local old_path="$base_path/com/quantumresearch/mycel/infrastructure"
        local new_path="$base_path/com/quantumresearch/mycel/spore"
        
        if [ -d "$old_path" ]; then
            # Create new directory structure
            mkdir -p "$new_path"
            
            # Move files to new structure
            if [ "$(ls -A "$old_path" 2>/dev/null)" ]; then
                cp -r "$old_path"/* "$new_path/"
                rm -rf "$old_path"
            fi
        fi
    done
}

# Restructure directories in bramble modules
for module in bramble-api bramble-core bramble-android bramble-java; do
    if [ -d "$module" ]; then
        restructure_infrastructure_to_spore "$module"
    fi
done

# Phase 3: Clean up remaining old package references
echo ""
echo "🔄 Phase 3: Cleaning up remaining old package references..."

cleanup_old_references() {
    local search_path="$1"
    echo "  Cleaning up old references in $search_path"
    
    # Find and update remaining org.briarproject.bramble references
    find "$search_path" -name "*.java" -type f | while read -r file; do
        # Replace old bramble package references
        sed -i.bak 's|org\.briarproject\.bramble|com.quantumresearch.mycel.spore|g' "$file"
        
        # Replace old briar package references  
        sed -i.bak 's|org\.briarproject\.briar|com.quantumresearch.mycel.app|g' "$file"
        
        # Remove backup files
        rm -f "${file}.bak"
    done
}

# Clean up old references in all modules
for module in bramble-api bramble-core bramble-android bramble-java briar-api briar-core briar-android briar-headless mailbox-integration-tests; do
    if [ -d "$module" ]; then
        cleanup_old_references "$module"
    fi
done

# Phase 4: Update remaining directory structures for old packages
echo ""
echo "🔄 Phase 4: Updating remaining directory structures..."

restructure_old_packages() {
    local module_path="$1"
    echo "  Restructuring old packages in $module_path"
    
    for src_dir in "src/main/java" "src/test/java" "src/androidTest/java"; do
        local base_path="$module_path/$src_dir"
        
        # Handle org.briarproject.bramble → com.quantumresearch.mycel.spore
        local old_bramble="$base_path/org/briarproject/bramble"
        local new_spore="$base_path/com/quantumresearch/mycel/spore"
        
        if [ -d "$old_bramble" ]; then
            mkdir -p "$new_spore"
            if [ "$(ls -A "$old_bramble" 2>/dev/null)" ]; then
                cp -r "$old_bramble"/* "$new_spore/"
                rm -rf "$old_bramble"
            fi
        fi
        
        # Handle org.briarproject.briar → com.quantumresearch.mycel.app
        local old_briar="$base_path/org/briarproject/briar"  
        local new_app="$base_path/com/quantumresearch/mycel/app"
        
        if [ -d "$old_briar" ]; then
            mkdir -p "$new_app"
            if [ "$(ls -A "$old_briar" 2>/dev/null)" ]; then
                cp -r "$old_briar"/* "$new_app/"
                rm -rf "$old_briar"
            fi
        fi
    done
}

# Restructure old packages in all modules
for module in bramble-api bramble-core bramble-android bramble-java briar-api briar-core briar-android briar-headless mailbox-integration-tests; do
    if [ -d "$module" ]; then
        restructure_old_packages "$module"
    fi
done

# Phase 5: Clean up empty directories
echo ""
echo "🔄 Phase 5: Cleaning up empty directories..."
find . -path "*/src/*/java/org/briarproject" -type d -empty -delete 2>/dev/null || true
find . -path "*/src/*/java/org" -type d -empty -delete 2>/dev/null || true
find . -path "*/src/*/java/com/quantumresearch/mycel/infrastructure" -type d -empty -delete 2>/dev/null || true

# Phase 6: Update manifest and configuration files
echo ""
echo "🔄 Phase 6: Updating manifest and configuration files..."

# Update Android manifest files
find . -name "AndroidManifest.xml" | while read -r manifest; do
    echo "  Updating $manifest"
    sed -i.bak 's|org\.briarproject\.bramble|com.quantumresearch.mycel.spore|g' "$manifest"
    sed -i.bak 's|org\.briarproject\.briar|com.quantumresearch.mycel.app|g' "$manifest"
    sed -i.bak 's|com\.quantumresearch\.mycel\.infrastructure|com.quantumresearch.mycel.spore|g' "$manifest"
    rm -f "${manifest}.bak"
done

# Update build.gradle files
find . -name "build.gradle" | while read -r gradle; do
    echo "  Updating $gradle"
    sed -i.bak 's|org\.briarproject\.bramble|com.quantumresearch.mycel.spore|g' "$gradle"
    sed -i.bak 's|org\.briarproject\.briar|com.quantumresearch.mycel.app|g' "$gradle"
    sed -i.bak 's|com\.quantumresearch\.mycel\.infrastructure|com.quantumresearch.mycel.spore|g' "$gradle"
    rm -f "${gradle}.bak"
done

echo ""
echo "✅ Conversion Complete!"
echo "======================"
echo "📊 Summary:"
echo "  • infrastructure → spore: Biological naming applied"
echo "  • All old org.briarproject.* references cleaned up"
echo "  • Directory structures updated"
echo "  • Manifest and build files updated"
echo ""
echo "🧬 New Package Structure:"
echo "  • com.quantumresearch.mycel.spore.*     (was bramble/infrastructure)"
echo "  • com.quantumresearch.mycel.app.*       (was briar)"
echo ""
echo "🔧 Next Steps:"
echo "  1. Run build verification"
echo "  2. Run tests"
echo "  3. Commit changes"