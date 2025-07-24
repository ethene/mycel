#!/bin/bash

# Deep Search for Briar/Bramble References
# This script searches for all remaining references comprehensively

echo "🔍 Deep Search for Briar/Bramble References"
echo "==========================================="

# Create results directory
mkdir -p search-results
cd search-results

echo "📁 Searching in different file types..."

# Search in code files
echo "  🔍 Java/Kotlin files..."
find .. -name "*.java" -o -name "*.kt" | grep -v search-results | xargs grep -l -i "briar\|bramble" > code-files.txt 2>/dev/null

# Search in configuration files
echo "  🔍 Configuration files..."
find .. -name "*.gradle" -o -name "*.xml" -o -name "*.properties" -o -name "*.json" | grep -v search-results | xargs grep -l -i "briar\|bramble" > config-files.txt 2>/dev/null

# Search in documentation
echo "  🔍 Documentation files..."
find .. -name "*.md" -o -name "*.txt" -o -name "*.rst" | grep -v search-results | xargs grep -l -i "briar\|bramble" > doc-files.txt 2>/dev/null

# Search in resource files
echo "  🔍 Resource files..."
find .. -name "*.xml" -path "*/res/*" -o -name "*.html" | grep -v search-results | xargs grep -l -i "briar\|bramble" > resource-files.txt 2>/dev/null

# Search in script files
echo "  🔍 Script files..."
find .. -name "*.sh" -o -name "*.py" -o -name "*.js" | grep -v search-results | xargs grep -l -i "briar\|bramble" > script-files.txt 2>/dev/null

# Search in all other files (catch-all)
echo "  🔍 Other files..."
find .. -type f ! -path "*/build/*" ! -path "*/.gradle/*" ! -path "*/search-results/*" ! -name "*.jar" ! -name "*.class" | xargs grep -l -i "briar\|bramble" > other-files.txt 2>/dev/null

echo ""
echo "📊 Results Summary:"
echo "=================="

total=0
for file in code-files.txt config-files.txt doc-files.txt resource-files.txt script-files.txt other-files.txt; do
    if [[ -f "$file" ]]; then
        count=$(wc -l < "$file")
        echo "  $(basename "$file" .txt): $count files"
        total=$((total + count))
    fi
done

echo "  TOTAL: $total files with references"
echo ""

# Create combined results
cat *.txt | sort -u > all-files-with-references.txt

echo "📋 Detailed Results:"
echo "==================="
echo "Files with briar/bramble references:"
cat all-files-with-references.txt

cd ..
echo ""
echo "✅ Search complete! Results saved in search-results/ directory"