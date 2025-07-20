#!/bin/bash
# Mycel Rebranding - Phase Testing Script

echo "🧪 Testing current phase changes..."

# Clean build
echo "1. Running clean build..."
./gradlew clean

# Full build
echo "2. Running full build..."
if ./gradlew build; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
    exit 1
fi

# Run tests
echo "3. Running tests..."
if ./gradlew test; then
    echo "✅ Tests passed"
else
    echo "❌ Tests failed"
    exit 1
fi

# Android APK
echo "4. Building Android APK..."
if ./gradlew :briar-android:assembleDebug; then
    echo "✅ APK build successful"
else
    echo "❌ APK build failed"
    exit 1
fi

# Verify package name (Phase 2+)
echo "5. Verifying package name..."
APK_FILE=$(find briar-android/build/outputs/apk/debug -name "*.apk" | head -1)
if [ -f "$APK_FILE" ]; then
    PACKAGE=$(aapt dump badging "$APK_FILE" | grep "package:" | head -1)
    echo "Package info: $PACKAGE"
    if [[ "$PACKAGE" == *"com.quantumresearch.mycel"* ]]; then
        echo "✅ Package name correct"
    else
        echo "⚠️  Package name verification (expected for Phase 1)"
    fi
fi

echo "🎉 Phase testing completed successfully!"