# Git Initialization Guide for Mycel Project
## Converting from Briar to Clean Mycel Repository

This guide provides step-by-step instructions to strip the old Briar git history and create a fresh Mycel repository with proper versioning for the rebranding process.

---

## 🎯 **OVERVIEW**

### **Goal**: 
- Remove all Briar git history to start fresh
- Create new GitHub repository for Mycel by Quantum Research Pty Ltd
- Establish git commit policy for incremental rebranding phases
- Maintain full source code while resetting version history

### **Approach**:
1. **Backup current state** (safety first)
2. **Remove old git history** (clean slate)
3. **Initialize new repository** (fresh start)
4. **Create GitHub repository** (hosted version control)
5. **Establish commit policy** (structured versioning)

---

## 📋 **STEP 1: BACKUP CURRENT STATE**

### **1.1 Create Backup**
```bash
# Navigate to project root
cd /Users/dmitrystakhin/Library/CloudStorage/Dropbox/work/mycel

# Create complete backup (excluding git history)
cp -r . ../mycel-backup-$(date +%Y%m%d)
echo "✅ Backup created at ../mycel-backup-$(date +%Y%m%d)"

# Verify backup has all files
ls -la ../mycel-backup-*/
```

### **1.2 Document Current State**
```bash
# Save current git status for reference
git status > git-status-before-cleanup.txt
git log --oneline -20 > git-log-before-cleanup.txt
git branch -a > git-branches-before-cleanup.txt

echo "✅ Current git state documented"
```

---

## 📋 **STEP 2: REMOVE OLD GIT HISTORY**

### **2.1 Remove Git Directory**
```bash
# Remove all git history and references
rm -rf .git/

# Verify git history is removed
ls -la | grep -i git
# Should show no .git directory

echo "✅ Old Briar git history removed"
```

### **2.2 Clean Git-Related Files**
```bash
# Remove git-related files that may contain Briar references
rm -f .gitignore
rm -f .gitattributes
rm -f .gitmodules

# Clean any git hooks or config files
find . -name ".git*" -type f -delete

echo "✅ Git-related files cleaned"
```

---

## 📋 **STEP 3: INITIALIZE NEW MYCEL REPOSITORY**

### **3.1 Create New Git Repository**
```bash
# Initialize fresh git repository
git init

# Set initial branch name to main (modern standard)
git branch -M main

echo "✅ New git repository initialized with main branch"
```

### **3.2 Configure Git for Mycel Project**
```bash
# Set repository-specific configuration
git config user.name "Quantum Research Pty Ltd"
git config user.email "dev@quantumresearch.com"  # Update with actual email

# Set repository description
echo "Mycel - Secure Decentralized Messaging by Quantum Research Pty Ltd" > .git/description

echo "✅ Git configuration set for Mycel project"
```

### **3.3 Create New .gitignore**
```bash
# Create comprehensive .gitignore for Android/Java project
cat > .gitignore << 'EOF'
# Mycel Project - Git Ignore File
# Generated for Quantum Research Pty Ltd

# Gradle files
.gradle/
build/
gradle-app.setting
!gradle-wrapper.jar
!gradle-wrapper.properties

# Android
*.apk
*.aab
*.ap_
*.dex
local.properties

# IDE files
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS generated files
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# Logs
*.log

# Runtime
*.tmp
*.temp

# Security
keystore.properties
*.jks
*.keystore

# Test results
/test-results/
/androidTest-results/

# Generated files
/gen/
/out/
EOF

echo "✅ New .gitignore created for Mycel project"
```

---

## 📋 **STEP 4: INITIAL COMMIT WITH CURRENT STATE**

### **4.1 Stage All Files**
```bash
# Add all current files to staging
git add .

# Verify what will be committed
git status

echo "✅ All files staged for initial commit"
```

### **4.2 Create Initial Commit**
```bash
# Create initial commit with Mycel branding
git commit -m "$(cat <<'EOF'
Initial commit: Mycel project setup

Mycel is a secure decentralized messaging application
developed by Quantum Research Pty Ltd.

This codebase is based on the Briar project and will be
rebranded to Mycel through incremental phases.

Original Briar project: https://briarproject.org/
License: GPL-3.0-or-later

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"

echo "✅ Initial commit created"
```

### **4.3 Verify Initial State**
```bash
# Check commit was created successfully
git log --oneline
git status

# Show project stats
echo "Files in repository: $(git ls-files | wc -l)"
echo "Repository size: $(du -sh .git)"
```

---

## 📋 **STEP 5: CREATE GITHUB REPOSITORY**

### **5.1 Create Repository on GitHub**
```bash
# Option 1: Using GitHub CLI (if installed)
gh repo create quantumresearch/mycel \
  --description "Mycel - Secure Decentralized Messaging by Quantum Research Pty Ltd" \
  --public \
  --clone=false

# Option 2: Manual creation
echo "Manual steps:"
echo "1. Go to https://github.com/new"
echo "2. Repository name: mycel"
echo "3. Description: Mycel - Secure Decentralized Messaging by Quantum Research Pty Ltd"
echo "4. Public repository"
echo "5. Do NOT initialize with README, .gitignore, or license"
echo "6. Click 'Create repository'"
```

### **5.2 Add GitHub Remote**
```bash
# Add GitHub as remote origin
git remote add origin https://github.com/quantumresearch/mycel.git

# Verify remote was added
git remote -v

echo "✅ GitHub remote added"
```

### **5.3 Push Initial Commit**
```bash
# Push to GitHub
git push -u origin main

echo "✅ Initial commit pushed to GitHub"
```

---

## 📋 **STEP 6: ESTABLISH REBRANDING COMMIT POLICY**

### **6.1 Git Commit Policy for Rebranding**

#### **Commit Message Format:**
```
[PHASE-X] Brief description of changes

Detailed description of what was changed and why.
Reference to specific files and line numbers when applicable.

Testing:
- ✅ Build: ./gradlew build
- ✅ Tests: ./gradlew test
- ✅ APK: Installs and runs correctly

Phase Progress: X/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

#### **Phase-Specific Commit Examples:**

**Phase 1 - String Resources:**
```bash
git commit -m "[PHASE-1] Update string resources from Briar to Mycel

Changed app_name and user-facing strings in:
- briar-android/src/main/res/values/strings.xml:4,8
- All 45+ localized string files

Testing:
- ✅ Build: ./gradlew build  
- ✅ Tests: ./gradlew test
- ✅ APK: App displays 'Mycel' throughout UI

Phase Progress: 1/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

**Phase 2 - Application ID:**
```bash
git commit -m "[PHASE-2] Update Android application ID to com.quantumresearch.mycel

Changed application ID and version info in:
- briar-android/build.gradle:31,29,30
- briar-android/src/debug/res/values/strings.xml:109

Testing:
- ✅ Build: ./gradlew :briar-android:assembleDebug
- ✅ APK: Shows package name 'com.quantumresearch.mycel'
- ✅ Install: Installs as separate app from Briar

Phase Progress: 2/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

### **6.2 Testing Verification Script**
```bash
# Create testing script for each phase
cat > test-phase.sh << 'EOF'
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
EOF

chmod +x test-phase.sh

echo "✅ Testing script created"
```

---

## 📋 **STEP 7: DOCUMENT REBRANDING PROGRESS**

### **7.1 Create Progress Tracking**
```bash
# Create rebranding progress file
cat > REBRANDING-PROGRESS.md << 'EOF'
# Mycel Rebranding Progress
## Quantum Research Pty Ltd

### 📊 **OVERALL PROGRESS: 0/7 PHASES COMPLETED**

---

## ✅ **COMPLETED PHASES**

*None yet - ready to begin Phase 1*

---

## 🚧 **CURRENT PHASE**

**Phase 1: String Resources**
- **Status**: Ready to start
- **Target**: Change "Briar" → "Mycel" in all string resources
- **Files**: 45+ localized string files
- **Estimated Duration**: 1-2 days

---

## 📋 **UPCOMING PHASES**

### **Phase 2: Application ID** (After Phase 1)
- Update Android applicationId to `com.quantumresearch.mycel`
- Reset version to 1.0.0

### **Phase 3: Infrastructure Packages** (After Phase 2)  
- Rename `org.briarproject.bramble.*` → `com.quantumresearch.mycel.infrastructure.*`

### **Phase 4: Application Packages** (After Phase 3)
- Rename `org.briarproject.briar.*` → `com.quantumresearch.mycel.app.*`

### **Phase 5: Configuration** (After Phase 4)
- Update deep links: `briar://` → `mycel://`
- Update themes and layout names

### **Phase 6: Visual Assets** (After Phase 5)
- Replace all logos, icons, and graphics
- Update color schemes

### **Phase 7: Documentation** (After Phase 6)
- Final documentation updates
- External reference updates

---

## 🎯 **SUCCESS CRITERIA**

### **Each Phase Must:**
- ✅ Build successfully (`./gradlew build`)
- ✅ Pass all tests (`./gradlew test`) 
- ✅ Create working APK
- ✅ Maintain all functionality
- ✅ Be committed to git with proper message

### **Final Success:**
- ✅ Complete rebrand to Mycel
- ✅ No "Briar" references visible to users
- ✅ GPL-3.0 license compliance maintained
- ✅ All functionality preserved

---

**Last Updated**: $(date +"%Y-%m-%d %H:%M:%S")
**Repository**: https://github.com/quantumresearch/mycel
EOF

echo "✅ Progress tracking file created"
```

---

## 🔄 **WORKFLOW SUMMARY**

### **Daily Workflow:**
1. **Start Phase**: Update `REBRANDING-PROGRESS.md`
2. **Make Changes**: Follow incremental plan
3. **Test Changes**: Run `./test-phase.sh`
4. **Commit**: Use proper commit message format
5. **Push**: `git push origin main`
6. **Update Progress**: Mark phase complete

### **Git Commands Reference:**
```bash
# Check status
git status

# Stage specific files
git add path/to/file

# Stage all changes
git add .

# Commit with message
git commit -m "[PHASE-X] Description..."

# Push to GitHub
git push origin main

# View recent commits
git log --oneline -5

# Check differences
git diff
```

---

## ⚠️ **IMPORTANT NOTES**

### **Safety Guidelines:**
- ✅ **Always test before committing**
- ✅ **One phase per commit**
- ✅ **Never commit broken builds**
- ✅ **Document all changes clearly**

### **Rollback Strategy:**
```bash
# If something goes wrong, rollback to previous commit
git reset --hard HEAD~1

# Or rollback to specific commit
git reset --hard <commit-hash>

# Force push if needed (use carefully)
git push --force-with-lease origin main
```

### **Backup Strategy:**
- ✅ **GitHub serves as primary backup**
- ✅ **Local backup created before git cleanup**
- ✅ **Each commit is a restoration point**

---

This git initialization guide ensures a clean start for the Mycel project with proper version control and structured rebranding approach.