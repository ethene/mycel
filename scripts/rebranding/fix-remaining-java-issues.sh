#!/bin/bash

# Fix Remaining Java Issues - Comprehensive file renaming and reference fixing
# This addresses the 1217 Java compilation errors

echo "🔧 Fixing remaining Java compilation issues..."

# Create backup directory
BACKUP_DIR="scripts/rebranding/java-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "📁 Backup directory: $BACKUP_DIR"

# Function to rename file and fix references
rename_java_file() {
    local old_file="$1"
    local new_file="$2"
    local old_class="$3" 
    local new_class="$4"
    
    if [[ -f "$old_file" ]]; then
        echo "  📝 Renaming: $old_file -> $new_file"
        
        # Create backup
        cp "$old_file" "$BACKUP_DIR/$(basename "$old_file")"
        
        # Move file to new name
        mv "$old_file" "$new_file"
        
        echo "    ✅ File renamed"
    else
        echo "  ⚠️  File not found: $old_file"
    fi
}

# Function to fix imports across all Java files
fix_imports() {
    local old_class="$1"
    local new_class="$2"
    
    echo "  🔄 Fixing imports: $old_class -> $new_class"
    
    # Find all Java files with imports of the old class
    find . -name "*.java" -exec grep -l "import.*$old_class" {} \; | while read file; do
        echo "    📄 Updating imports in: $file"
        sed -i '' "s/import.*\.$old_class;/import com.quantumresearch.mycel.app.android.activity.$new_class;/g" "$file"
        sed -i '' "s/import.*\.$old_class$/import com.quantumresearch.mycel.app.android.activity.$new_class/g" "$file"
    done
    
    # Fix extends and implements references
    find . -name "*.java" -exec grep -l "extends $old_class\|implements.*$old_class" {} \; | while read file; do
        echo "    📄 Updating extends/implements in: $file"
        sed -i '' "s/extends $old_class/extends $new_class/g" "$file"
        sed -i '' "s/implements $old_class/implements $new_class/g" "$file"
    done
}

echo ""
echo "🎯 Phase 1: Rename critical Activity files"

# Rename the core files that are causing filename/classname mismatches
rename_java_file "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/controller/BriarControllerImpl.java" \
                 "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/controller/MycelControllerImpl.java" \
                 "BriarControllerImpl" "MycelControllerImpl"

rename_java_file "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/view/BriarRecyclerViewScrollListener.java" \
                 "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/view/MycelRecyclerViewScrollListener.java" \
                 "BriarRecyclerViewScrollListener" "MycelRecyclerViewScrollListener"

rename_java_file "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/conversation/glide/BriarModelLoaderFactory.java" \
                 "mycel-android/src/main/java/com/quantumresearch/mycel/app/android/conversation/glide/MycelModelLoaderFactory.java" \
                 "BriarModelLoaderFactory" "MycelModelLoaderFactory"

echo ""
echo "🎯 Phase 2: Fix all import statements"

# Fix BriarService -> MycelService imports  
echo "  🔄 Fixing BriarService imports..."
find . -name "*.java" -exec sed -i '' 's/import.*BriarService;/import com.quantumresearch.mycel.app.android.MycelService;/g' {} \;
find . -name "*.java" -exec sed -i '' 's/BriarService\.EXTRA_/MycelService.EXTRA_/g' {} \;
find . -name "*.java" -exec sed -i '' 's/BriarService\./MycelService./g' {} \;

# Fix BriarActivity -> MycelActivity imports
echo "  🔄 Fixing BriarActivity imports..."
find . -name "*.java" -exec sed -i '' 's/import.*BriarActivity;/import com.quantumresearch.mycel.app.android.activity.MycelActivity;/g' {} \;
find . -name "*.java" -exec sed -i '' 's/extends BriarActivity/extends MycelActivity/g' {} \;

# Fix BriarController -> MycelController imports  
echo "  🔄 Fixing BriarController imports..."
find . -name "*.java" -exec sed -i '' 's/import.*BriarController;/import com.quantumresearch.mycel.app.android.controller.MycelController;/g' {} \;
find . -name "*.java" -exec sed -i '' 's/BriarController\s/MycelController /g' {} \;

echo ""
echo "🎯 Phase 3: Fix remaining class references"

# Fix any remaining standalone class references
find . -name "*.java" -exec sed -i '' 's/\bBriarService\b/MycelService/g' {} \;
find . -name "*.java" -exec sed -i '' 's/\bBriarActivity\b/MycelActivity/g' {} \;
find . -name "*.java" -exec sed -i '' 's/\bBriarController\b/MycelController/g' {} \;
find . -name "*.java" -exec sed -i '' 's/\bBriarControllerImpl\b/MycelControllerImpl/g' {} \;

echo ""
echo "✅ Java Fixes Applied!"
echo "📁 Backups stored in: $BACKUP_DIR"
echo ""
echo "🧪 Fixed issues:"
echo "  • File name mismatches (BriarControllerImpl.java -> MycelControllerImpl.java)"
echo "  • Import statements for renamed classes"
echo "  • extends/implements declarations"
echo "  • Static method references"
echo ""
echo "⚠️  Run 'make clean && make build' to verify compilation works"