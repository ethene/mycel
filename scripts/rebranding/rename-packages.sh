#!/bin/bash

# Mycel Package Renaming Script
# Phase 3: Bramble → Spore Infrastructure
# Phase 4: Briar → Mycel Application

set -e  # Exit on any error

echo "🍄 Starting Mycel Package Renaming Process"
echo "============================================"

# Phase 3: Bramble → Spore Infrastructure
echo ""
echo "📦 Phase 3: Renaming Bramble → Spore Infrastructure..."

# Function to rename package in Java files
rename_package_in_files() {
    local old_package="$1"
    local new_package="$2"
    local module_path="$3"
    
    echo "  Renaming $old_package → $new_package in $module_path"
    
    # Find all Java files and replace package declarations and imports
    find "$module_path" -name "*.java" -type f | while read -r file; do
        # Replace package declarations
        sed -i.bak "s|package $old_package|package $new_package|g" "$file"
        
        # Replace import statements
        sed -i.bak "s|import $old_package|import $new_package|g" "$file"
        
        # Remove backup files
        rm -f "${file}.bak"
    done
    
    # Also update build.gradle files if they exist
    if [ -f "$module_path/build.gradle" ]; then
        sed -i.bak "s|$old_package|$new_package|g" "$module_path/build.gradle"
        rm -f "$module_path/build.gradle.bak"
    fi
}

# Function to create new directory structure and move files
restructure_module() {
    local module_path="$1"
    local old_package_path="$2"
    local new_package_path="$3"
    
    echo "  Restructuring directories in $module_path"
    
    # Create new package directory structure
    local src_main="$module_path/src/main/java"
    local src_test="$module_path/src/test/java"
    
    if [ -d "$src_main/$old_package_path" ]; then
        # Create new directory structure
        mkdir -p "$src_main/$new_package_path"
        
        # Move files to new structure
        if [ "$(ls -A "$src_main/$old_package_path" 2>/dev/null)" ]; then
            cp -r "$src_main/$old_package_path"/* "$src_main/$new_package_path/"
            rm -rf "$src_main/$old_package_path"
        fi
    fi
    
    if [ -d "$src_test/$old_package_path" ]; then
        # Create new directory structure
        mkdir -p "$src_test/$new_package_path"
        
        # Move files to new structure
        if [ "$(ls -A "$src_test/$old_package_path" 2>/dev/null)" ]; then
            cp -r "$src_test/$old_package_path"/* "$src_test/$new_package_path/"
            rm -rf "$src_test/$old_package_path"
        fi
    fi
}

# Phase 3.1: bramble-api → spore-api
echo ""
echo "🔄 Step 3.1: bramble-api → spore-api"
rename_package_in_files "org.briarproject.bramble" "com.quantumresearch.mycel.spore" "bramble-api"
restructure_module "bramble-api" "org/briarproject/bramble" "com/quantumresearch/mycel/spore"

# Phase 3.2: bramble-core → spore-core  
echo ""
echo "🔄 Step 3.2: bramble-core → spore-core"
rename_package_in_files "org.briarproject.bramble" "com.quantumresearch.mycel.spore" "bramble-core"
restructure_module "bramble-core" "org/briarproject/bramble" "com/quantumresearch/mycel/spore"

# Phase 3.3: bramble-android → spore-android
echo ""
echo "🔄 Step 3.3: bramble-android → spore-android"
rename_package_in_files "org.briarproject.bramble" "com.quantumresearch.mycel.spore" "bramble-android"
restructure_module "bramble-android" "org/briarproject/bramble" "com/quantumresearch/mycel/spore"

# Phase 3.4: bramble-java → spore-java
echo ""
echo "🔄 Step 3.4: bramble-java → spore-java"
rename_package_in_files "org.briarproject.bramble" "com.quantumresearch.mycel.spore" "bramble-java"
restructure_module "bramble-java" "org/briarproject/bramble" "com/quantumresearch/mycel/spore"

echo ""
echo "✅ Phase 3 Complete: Bramble → Spore Infrastructure"

# Phase 4: Briar → Mycel Application
echo ""
echo "📱 Phase 4: Renaming Briar → Mycel Application..."

# Phase 4.1: briar-api → mycel-api
echo ""
echo "🔄 Step 4.1: briar-api → mycel-api"
rename_package_in_files "org.briarproject.briar" "com.quantumresearch.mycel.app" "briar-api"
restructure_module "briar-api" "org/briarproject/briar" "com/quantumresearch/mycel/app"

# Phase 4.2: briar-core → mycel-core
echo ""
echo "🔄 Step 4.2: briar-core → mycel-core"
rename_package_in_files "org.briarproject.briar" "com.quantumresearch.mycel.app" "briar-core"
restructure_module "briar-core" "org/briarproject/briar" "com/quantumresearch/mycel/app"

# Phase 4.3: briar-android → mycel-android  
echo ""
echo "🔄 Step 4.3: briar-android → mycel-android"
rename_package_in_files "org.briarproject.briar" "com.quantumresearch.mycel.app" "briar-android"
restructure_module "briar-android" "org/briarproject/briar" "com/quantumresearch/mycel/app"

# Phase 4.4: briar-headless → mycel-headless
echo ""
echo "🔄 Step 4.4: briar-headless → mycel-headless"
rename_package_in_files "org.briarproject.briar" "com.quantumresearch.mycel.app" "briar-headless"
restructure_module "briar-headless" "org/briarproject/briar" "com/quantumresearch/mycel/app"

echo ""
echo "✅ Phase 4 Complete: Briar → Mycel Application"

# Clean up any remaining org/briarproject directories
echo ""
echo "🧹 Cleaning up old package directories..."
find . -path "*/src/*/java/org/briarproject" -type d -empty -delete 2>/dev/null || true
find . -path "*/src/*/java/org" -type d -empty -delete 2>/dev/null || true

echo ""
echo "🎉 Package Renaming Complete!"
echo "============================================"
echo "📊 Summary:"
echo "  • Bramble → Spore: Infrastructure layer renamed"
echo "  • Briar → Mycel: Application layer renamed"
echo "  • All package declarations updated"
echo "  • All import statements updated"
echo "  • Directory structure restructured"
echo ""
echo "🔧 Next Steps:"
echo "  1. Update Android manifest files"
echo "  2. Update build configuration"
echo "  3. Run build verification"
echo "  4. Run tests"