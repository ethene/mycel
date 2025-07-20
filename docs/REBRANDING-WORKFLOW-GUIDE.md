# Mycel Rebranding Workflow Guide
## Quick Reference for Phase Implementation

This guide provides a streamlined workflow for implementing each rebranding phase using Task Master and git.

---

## 🚀 **Phase Implementation Workflow**

### **1. Start Phase**
```bash
# Check current status
task-master list
task-master next

# Start the phase
task-master show <id>
task-master set-status --id=<id> --status=in-progress
```

### **2. Implement Changes**
Follow the specific file updates for each phase as documented in:
- `CLAUDE.md` - Section "Files to Update for Each Phase"
- `docs/INCREMENTAL-REBRANDING-PLAN.md` - Detailed instructions
- `docs/VERIFIED-REBRANDING-PLAN.md` - Verified file locations

### **3. Test Changes**
```bash
# Run the phase testing script
./test-phase.sh

# Or run tests manually if needed
./gradlew clean build test
./gradlew :briar-android:assembleDebug
```

### **4. Update Task Master**
```bash
# Add implementation notes
task-master update-subtask --id=<id> --prompt="Completed: <summary of changes>"

# Mark task as done
task-master set-status --id=<id> --status=done
```

### **5. Commit Changes**
```bash
# Stage changes
git add .

# Commit with proper format
git commit -m "[PHASE-X] Brief description

Detailed changes:
- List of key changes
- Files updated count
- Any important notes

Testing:
- ✅ Build: ./gradlew build
- ✅ Tests: ./gradlew test
- ✅ APK: Builds and installs correctly

Phase Progress: X/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

### **6. Update Documentation**
1. **Update REBRANDING-PROGRESS.md**:
   - Mark phase as completed
   - Add completion date and commit hash
   - Update current phase to next

2. **Update CLAUDE.md**:
   - Update "Current Phase" status
   - Add phase to completion list

3. **Push changes**:
   ```bash
   git push origin main
   ```

---

## 📊 **Phase Status Tracking**

| Phase | Status | Task ID | Description | Key Files |
|-------|--------|---------|-------------|-----------|
| 1 | ✅ DONE | 1 | String Resources | 51 strings.xml files |
| 2 | ⏳ READY | 2 | Application ID | build.gradle |
| 3 | 🔒 BLOCKED | 3 | Bramble Packages | All bramble-* Java files |
| 4 | 🔒 BLOCKED | 4 | Briar Packages | All briar-* Java files |
| 5 | 🔒 BLOCKED | 5 | Configuration | AndroidManifest.xml, styles |
| 6 | 🔒 BLOCKED | 6 | Visual Assets | Icons, graphics, colors |
| 7 | 🔒 BLOCKED | 7 | Documentation | README, docs/* |

---

## 🛠️ **Common Task Master Commands**

```bash
# View progress
task-master list                          # All tasks with status
task-master status                        # Project overview
task-master complexity-report             # Complexity analysis

# Task management
task-master show <id>                     # Task details
task-master next                          # Next recommended task
task-master set-status --id=X --status=Y  # Update status

# Task updates
task-master update-task --id=X --prompt="notes"
task-master update-subtask --id=X --prompt="progress"
task-master expand --id=X                 # Break into subtasks

# Documentation
task-master generate                      # Generate task markdown files
```

---

## 📝 **Git Commit Templates**

### **Phase Completion Commit**
```
[PHASE-X] <Brief description of phase>

<Detailed list of changes>
- Change 1
- Change 2
- Files updated: X

Testing:
- ✅ Build: ./gradlew build
- ✅ Tests: ./gradlew test  
- ✅ APK: Installs and runs correctly

Phase Progress: X/7 completed

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

### **Documentation Update Commit**
```
Update <document> for Phase X completion

- Updated phase status
- Added completion details
- Updated progress metrics

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

## ⚠️ **Important Reminders**

1. **Always test before marking done** - Run `./test-phase.sh`
2. **Update documentation** - Both CLAUDE.md and REBRANDING-PROGRESS.md
3. **Follow phase order** - Don't skip phases
4. **Use proper commit format** - [PHASE-X] prefix
5. **Track in Task Master** - Update task status

---

## 🔗 **Quick Links**

- **Main Guide**: `CLAUDE.md`
- **Progress**: `REBRANDING-PROGRESS.md`
- **Detailed Plan**: `docs/INCREMENTAL-REBRANDING-PLAN.md`
- **File Locations**: `docs/VERIFIED-REBRANDING-PLAN.md`
- **Task Master Tasks**: `.taskmaster/tasks/tasks.json`
- **GitHub**: https://github.com/ethene/mycel

---

This workflow ensures consistent, trackable progress through all rebranding phases!