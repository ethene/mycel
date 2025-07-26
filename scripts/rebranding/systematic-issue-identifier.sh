#!/bin/bash

# Systematic Issue Identifier - Find ALL remaining briar/bramble references
# This script provides complete visibility into what needs to be fixed

echo "🔍 SYSTEMATIC BRIAR/BRAMBLE ISSUE IDENTIFICATION"
echo "==============================================="
echo ""

# Create results directory
RESULTS_DIR="scripts/rebranding/systematic-results-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"

echo "📁 Results directory: $RESULTS_DIR"
echo ""

# 1. JAVA CLASS INHERITANCE ISSUES
echo "🎯 1. Java Class Inheritance Issues"
echo "-----------------------------------"

echo "  📝 Finding 'extends BriarActivity' declarations..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "extends BriarActivity" {} \; > "$RESULTS_DIR/extends-briar-activity.txt"
EXTENDS_BRIAR=$(cat "$RESULTS_DIR/extends-briar-activity.txt" | wc -l)
echo "    Found: $EXTENDS_BRIAR files extending BriarActivity"

echo "  📝 Finding 'extends BriarFragment' declarations..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "extends BriarFragment" {} \; > "$RESULTS_DIR/extends-briar-fragment.txt"
EXTENDS_FRAGMENT=$(cat "$RESULTS_DIR/extends-briar-fragment.txt" | wc -l)
echo "    Found: $EXTENDS_FRAGMENT files extending BriarFragment"

echo "  📝 Finding 'implements BriarController' declarations..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "implements.*BriarController" {} \; > "$RESULTS_DIR/implements-briar-controller.txt"
IMPLEMENTS_CONTROLLER=$(cat "$RESULTS_DIR/implements-briar-controller.txt" | wc -l)
echo "    Found: $IMPLEMENTS_CONTROLLER files implementing BriarController"

# 2. JAVA IMPORT STATEMENT ISSUES
echo ""
echo "🎯 2. Java Import Statement Issues"
echo "----------------------------------"

echo "  📦 Finding 'import.*BriarActivity' statements..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "import.*BriarActivity" {} \; > "$RESULTS_DIR/import-briar-activity.txt"
IMPORT_ACTIVITY=$(cat "$RESULTS_DIR/import-briar-activity.txt" | wc -l)
echo "    Found: $IMPORT_ACTIVITY import statements for BriarActivity"

echo "  📦 Finding 'import.*BriarService' statements..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "import.*BriarService" {} \; > "$RESULTS_DIR/import-briar-service.txt"
IMPORT_SERVICE=$(cat "$RESULTS_DIR/import-briar-service.txt" | wc -l)
echo "    Found: $IMPORT_SERVICE import statements for BriarService"

echo "  📦 Finding 'import.*BriarController' statements..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "import.*BriarController" {} \; > "$RESULTS_DIR/import-briar-controller.txt"
IMPORT_CONTROLLER=$(cat "$RESULTS_DIR/import-briar-controller.txt" | wc -l)
echo "    Found: $IMPORT_CONTROLLER import statements for BriarController"

echo "  📦 Finding all 'import.*Briar*' statements..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "import.*\.Briar" {} \; > "$RESULTS_DIR/import-all-briar.txt"
IMPORT_ALL_BRIAR=$(cat "$RESULTS_DIR/import-all-briar.txt" | wc -l)
echo "    Found: $IMPORT_ALL_BRIAR total Briar* import statements"

# 3. JAVA VARIABLE AND METHOD REFERENCES
echo ""
echo "🎯 3. Java Variable and Method References"
echo "----------------------------------------"

echo "  🔗 Finding 'BriarService.' static references..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "BriarService\." {} \; > "$RESULTS_DIR/briar-service-static.txt"
SERVICE_STATIC=$(cat "$RESULTS_DIR/briar-service-static.txt" | wc -l)
echo "    Found: $SERVICE_STATIC BriarService static references"

echo "  🔗 Finding variable declarations with Briar types..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "BriarActivity\|BriarService\|BriarController" {} \; | grep -v "import\|extends\|implements" > "$RESULTS_DIR/briar-variables.txt"
BRIAR_VARS=$(cat "$RESULTS_DIR/briar-variables.txt" | wc -l)
echo "    Found: $BRIAR_VARS variable/method references to Briar types"

# 4. JAVA CONSTRUCTOR AND METHOD NAMES
echo ""
echo "🎯 4. Java Constructor and Method Names"
echo "--------------------------------------"

echo "  🏗️  Finding constructor names that don't match class names..."
find . -name "*.java" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "BriarControllerImpl(\|BriarServiceConnection(\|BriarModelLoader(" {} \; > "$RESULTS_DIR/constructor-mismatches.txt"
CONSTRUCTOR_MISMATCH=$(cat "$RESULTS_DIR/constructor-mismatches.txt" | wc -l)
echo "    Found: $CONSTRUCTOR_MISMATCH constructor name mismatches"

# 5. KOTLIN FILE REFERENCES
echo ""
echo "🎯 5. Kotlin File References"
echo "----------------------------"

echo "  🅺 Finding Kotlin files with Briar references..."
find . -name "*.kt" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "BriarService\|BriarActivity\|BriarController" {} \; > "$RESULTS_DIR/kotlin-briar-refs.txt"
KOTLIN_REFS=$(cat "$RESULTS_DIR/kotlin-briar-refs.txt" | wc -l)
echo "    Found: $KOTLIN_REFS Kotlin references to Briar classes"

# 6. XML RESOURCE REFERENCES  
echo ""
echo "🎯 6. XML Resource References"
echo "----------------------------"

echo "  📱 Finding XML files with hardcoded org.briarproject references..."
find . -name "*.xml" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "org\.briarproject" {} \; > "$RESULTS_DIR/xml-org-briarproject.txt"
XML_ORG_BRIAR=$(cat "$RESULTS_DIR/xml-org-briarproject.txt" | wc -l)
echo "    Found: $XML_ORG_BRIAR XML references to org.briarproject"

echo "  📱 Finding XML files with Briar class references..."
find . -name "*.xml" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "BriarActivity\|BriarService\|BriarFragment" {} \; > "$RESULTS_DIR/xml-briar-classes.txt"
XML_BRIAR_CLASSES=$(cat "$RESULTS_DIR/xml-briar-classes.txt" | wc -l)
echo "    Found: $XML_BRIAR_CLASSES XML references to Briar classes"

# 7. GRADLE BUILD FILE REFERENCES
echo ""
echo "🎯 7. Gradle Build File References"
echo "----------------------------------"

echo "  🔧 Finding build.gradle references to briarproject (non-external)..."
find . -name "build.gradle*" -not -path "./scripts/*" -not -path "./docs/*" -not -path "./search-results/*" \
    -exec grep -Hn "org\.briarproject" {} \; | grep -v "dont-kill-me-lib\|onionwrapper\|tor-\|lyrebird\|null-safety\|socks-socket" > "$RESULTS_DIR/gradle-internal-refs.txt"
GRADLE_INTERNAL=$(cat "$RESULTS_DIR/gradle-internal-refs.txt" | wc -l)
echo "    Found: $GRADLE_INTERNAL internal briarproject references in gradle files"

# 8. GENERATE DETAILED ANALYSIS FILES
echo ""
echo "🎯 8. Generating Detailed Analysis Files"
echo "----------------------------------------"

# Generate fix scripts for each category
echo "  📋 Generating systematic fix commands..."

# Category 1: extends BriarActivity fixes
if [[ $EXTENDS_BRIAR -gt 0 ]]; then
    echo "# Fix extends BriarActivity declarations" > "$RESULTS_DIR/fix-extends-briar-activity.sh"
    echo "#!/bin/bash" >> "$RESULTS_DIR/fix-extends-briar-activity.sh"
    cat "$RESULTS_DIR/extends-briar-activity.txt" | while read line; do
        file=$(echo "$line" | cut -d: -f1)
        echo "sed -i '' 's/extends BriarActivity/extends MycelActivity/g' \"$file\"" >> "$RESULTS_DIR/fix-extends-briar-activity.sh"
    done
    chmod +x "$RESULTS_DIR/fix-extends-briar-activity.sh"
    echo "    ✅ Created fix-extends-briar-activity.sh"
fi

# Category 2: import statement fixes
if [[ $IMPORT_ALL_BRIAR -gt 0 ]]; then
    echo "# Fix Briar import statements" > "$RESULTS_DIR/fix-import-statements.sh"
    echo "#!/bin/bash" >> "$RESULTS_DIR/fix-import-statements.sh"
    cat "$RESULTS_DIR/import-all-briar.txt" | cut -d: -f1 | sort | uniq | while read file; do
        echo "sed -i '' 's/import.*BriarActivity;/import com.quantumresearch.mycel.app.android.activity.MycelActivity;/g' \"$file\"" >> "$RESULTS_DIR/fix-import-statements.sh"
        echo "sed -i '' 's/import.*BriarService;/import com.quantumresearch.mycel.app.android.MycelService;/g' \"$file\"" >> "$RESULTS_DIR/fix-import-statements.sh"
        echo "sed -i '' 's/import.*BriarController;/import com.quantumresearch.mycel.app.android.controller.MycelController;/g' \"$file\"" >> "$RESULTS_DIR/fix-import-statements.sh"
    done
    chmod +x "$RESULTS_DIR/fix-import-statements.sh"
    echo "    ✅ Created fix-import-statements.sh"
fi

# Category 3: Variable and method reference fixes
if [[ $BRIAR_VARS -gt 0 ]]; then
    echo "# Fix variable and method references" > "$RESULTS_DIR/fix-variable-references.sh"
    echo "#!/bin/bash" >> "$RESULTS_DIR/fix-variable-references.sh"
    cat "$RESULTS_DIR/briar-variables.txt" | cut -d: -f1 | sort | uniq | while read file; do
        echo "sed -i '' 's/BriarActivity/MycelActivity/g' \"$file\"" >> "$RESULTS_DIR/fix-variable-references.sh"
        echo "sed -i '' 's/BriarService/MycelService/g' \"$file\"" >> "$RESULTS_DIR/fix-variable-references.sh"
        echo "sed -i '' 's/BriarController/MycelController/g' \"$file\"" >> "$RESULTS_DIR/fix-variable-references.sh"
    done
    chmod +x "$RESULTS_DIR/fix-variable-references.sh"
    echo "    ✅ Created fix-variable-references.sh"
fi

# 9. GENERATE MASTER SUMMARY
echo ""
echo "📊 SYSTEMATIC ANALYSIS SUMMARY"
echo "=============================="

TOTAL_CRITICAL_ISSUES=$((EXTENDS_BRIAR + EXTENDS_FRAGMENT + IMPLEMENTS_CONTROLLER + IMPORT_ACTIVITY + IMPORT_SERVICE + IMPORT_CONTROLLER + SERVICE_STATIC + CONSTRUCTOR_MISMATCH + KOTLIN_REFS + XML_ORG_BRIAR + XML_BRIAR_CLASSES + GRADLE_INTERNAL))

echo "🚨 CRITICAL ISSUES REQUIRING FIXES:"
echo "  Java Class Inheritance:"
echo "    - extends BriarActivity:      $EXTENDS_BRIAR files"
echo "    - extends BriarFragment:      $EXTENDS_FRAGMENT files"
echo "    - implements BriarController: $IMPLEMENTS_CONTROLLER files"
echo ""
echo "  Java Import Statements:"
echo "    - import BriarActivity:       $IMPORT_ACTIVITY files"
echo "    - import BriarService:        $IMPORT_SERVICE files"  
echo "    - import BriarController:     $IMPORT_CONTROLLER files"
echo "    - Total Briar imports:        $IMPORT_ALL_BRIAR files"
echo ""
echo "  Java References:"
echo "    - BriarService static calls:  $SERVICE_STATIC references"
echo "    - Variable/method references: $BRIAR_VARS references"
echo "    - Constructor mismatches:     $CONSTRUCTOR_MISMATCH files"
echo ""
echo "  Other Languages:"
echo "    - Kotlin Briar references:    $KOTLIN_REFS references"
echo ""
echo "  Configuration Files:"
echo "    - XML org.briarproject refs:  $XML_ORG_BRIAR references"
echo "    - XML Briar class refs:       $XML_BRIAR_CLASSES references"
echo "    - Gradle internal refs:       $GRADLE_INTERNAL references"
echo ""
echo "🎯 TOTAL CRITICAL ISSUES: $TOTAL_CRITICAL_ISSUES"

# 10. GENERATE EXECUTION PLAN
echo ""
echo "🔧 SYSTEMATIC EXECUTION PLAN"
echo "============================"

echo "📋 Generated fix scripts in order of execution:"
echo "  1. $RESULTS_DIR/fix-import-statements.sh"
echo "  2. $RESULTS_DIR/fix-extends-briar-activity.sh"  
echo "  3. $RESULTS_DIR/fix-variable-references.sh"
echo ""
echo "📁 Detailed issue files:"
echo "  • extends-briar-activity.txt ($EXTENDS_BRIAR issues)"
echo "  • import-all-briar.txt ($IMPORT_ALL_BRIAR issues)"
echo "  • briar-variables.txt ($BRIAR_VARS issues)"
echo "  • kotlin-briar-refs.txt ($KOTLIN_REFS issues)"
echo "  • xml-org-briarproject.txt ($XML_ORG_BRIAR issues)"
echo ""
echo "✅ VERIFICATION COMMAND:"
echo "  Run this script again after fixes to verify count reduction"
echo ""
echo "🎯 SUCCESS CRITERIA:"
echo "  All critical issue counts should be 0"
echo "  'make clean && make build' should succeed"
echo "  Android APK should install and run without ClassNotFoundException"
echo ""
echo "📁 ALL RESULTS SAVED TO: $RESULTS_DIR"