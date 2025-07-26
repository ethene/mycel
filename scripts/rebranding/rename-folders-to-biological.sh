#!/bin/bash

# Mycel Biological Folder Renaming Script
# Renames all bramble-* and briar-* folders to spore-* and mycel-* respectively

set -e  # Exit on any error

echo "🍄 Starting Mycel Biological Folder Renaming"
echo "============================================="

# Step 1: Rename Infrastructure Folders (Bramble → Spore)
echo ""
echo "📦 Step 1: Renaming Infrastructure Folders (Bramble → Spore)..."

if [ -d "bramble-api" ]; then
    echo "  Renaming bramble-api → spore-api"
    mv bramble-api spore-api
fi

if [ -d "bramble-core" ]; then
    echo "  Renaming bramble-core → spore-core"
    mv bramble-core spore-core
fi

if [ -d "bramble-android" ]; then
    echo "  Renaming bramble-android → spore-android"
    mv bramble-android spore-android
fi

if [ -d "bramble-java" ]; then
    echo "  Renaming bramble-java → spore-java"
    mv bramble-java spore-java
fi

# Step 2: Rename Application Folders (Briar → Mycel)
echo ""
echo "📱 Step 2: Renaming Application Folders (Briar → Mycel)..."

if [ -d "briar-api" ]; then
    echo "  Renaming briar-api → mycel-api"
    mv briar-api mycel-api
fi

if [ -d "briar-core" ]; then
    echo "  Renaming briar-core → mycel-core"
    mv briar-core mycel-core
fi

if [ -d "briar-android" ]; then
    echo "  Renaming briar-android → mycel-android"
    mv briar-android mycel-android
fi

if [ -d "briar-headless" ]; then
    echo "  Renaming briar-headless → mycel-headless"
    mv briar-headless mycel-headless
fi

if [ -d "briar-mailbox" ]; then
    echo "  Renaming briar-mailbox → mycel-mailbox"
    mv briar-mailbox mycel-mailbox
fi

# Step 3: Update settings.gradle
echo ""
echo "🔧 Step 3: Updating settings.gradle..."
sed -i.bak "s/':bramble-api'/':spore-api'/g" settings.gradle
sed -i.bak "s/':bramble-core'/':spore-core'/g" settings.gradle
sed -i.bak "s/':bramble-android'/':spore-android'/g" settings.gradle
sed -i.bak "s/':bramble-java'/':spore-java'/g" settings.gradle
sed -i.bak "s/':briar-api'/':mycel-api'/g" settings.gradle
sed -i.bak "s/':briar-core'/':mycel-core'/g" settings.gradle
sed -i.bak "s/':briar-android'/':mycel-android'/g" settings.gradle
sed -i.bak "s/':briar-headless'/':mycel-headless'/g" settings.gradle
sed -i.bak "s/briar-mailbox/mycel-mailbox/g" settings.gradle
rm -f settings.gradle.bak

# Step 4: Update all build.gradle files
echo ""
echo "🔧 Step 4: Updating build.gradle files..."

update_build_gradle() {
    local file="$1"
    if [ -f "$file" ]; then
        echo "  Updating $file"
        sed -i.bak "s/':bramble-api'/':spore-api'/g" "$file"
        sed -i.bak "s/':bramble-core'/':spore-core'/g" "$file"
        sed -i.bak "s/':bramble-android'/':spore-android'/g" "$file"
        sed -i.bak "s/':bramble-java'/':spore-java'/g" "$file"
        sed -i.bak "s/':briar-api'/':mycel-api'/g" "$file"
        sed -i.bak "s/':briar-core'/':mycel-core'/g" "$file"
        sed -i.bak "s/':briar-android'/':mycel-android'/g" "$file"
        sed -i.bak "s/':briar-headless'/':mycel-headless'/g" "$file"
        rm -f "${file}.bak"
    fi
}

# Update all build.gradle files
find . -name "build.gradle" | while read -r gradle_file; do
    update_build_gradle "$gradle_file"
done

# Step 5: Update documentation references
echo ""
echo "📚 Step 5: Updating documentation..."

update_docs() {
    local file="$1"
    if [ -f "$file" ]; then
        echo "  Updating $file"
        sed -i.bak "s/bramble-api/spore-api/g" "$file"
        sed -i.bak "s/bramble-core/spore-core/g" "$file"
        sed -i.bak "s/bramble-android/spore-android/g" "$file"
        sed -i.bak "s/bramble-java/spore-java/g" "$file"
        sed -i.bak "s/briar-api/mycel-api/g" "$file"
        sed -i.bak "s/briar-core/mycel-core/g" "$file"
        sed -i.bak "s/briar-android/mycel-android/g" "$file"
        sed -i.bak "s/briar-headless/mycel-headless/g" "$file"
        sed -i.bak "s/briar-mailbox/mycel-mailbox/g" "$file"
        rm -f "${file}.bak"
    fi
}

# Update documentation files
find docs/ -name "*.md" | while read -r doc_file; do
    update_docs "$doc_file"
done

# Update main documentation files
update_docs "README.md"
update_docs "CLAUDE.md"
update_docs "CONTRIBUTING.md"

echo ""
echo "✅ Folder Renaming Complete!"
echo "============================="
echo "📊 Summary:"
echo "  🟤 Infrastructure (Spore):"
echo "    • bramble-api → spore-api"
echo "    • bramble-core → spore-core"
echo "    • bramble-android → spore-android"
echo "    • bramble-java → spore-java"
echo ""
echo "  🟢 Application (Mycel):"
echo "    • briar-api → mycel-api"
echo "    • briar-core → mycel-core"
echo "    • briar-android → mycel-android"
echo "    • briar-headless → mycel-headless"
echo "    • briar-mailbox → mycel-mailbox"
echo ""
echo "🔧 Next Steps:"
echo "  1. Test build with new folder structure"
echo "  2. Verify all references are updated"
echo "  3. Update any remaining documentation"
echo "  4. Run tests to ensure everything works"