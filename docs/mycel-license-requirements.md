# Mycel Licensing Requirements

## License Analysis Summary

Based on analysis of the Briar codebase, here are the licensing requirements for Mycel:

## Current Briar Licensing
- **Primary License**: GNU General Public License v3.0 (GPL-3.0-or-later)
- **Applies to**: All components (Android, desktop, headless)
- **Location**: `LICENSE.txt` in project root

## Required License for Mycel

### Must Use: GPL-3.0-or-later
Since Mycel is a derivative work of Briar, it must be licensed under the same terms:

```
Mycel - Secure Messaging Application
Copyright (C) 2024 Quantum Research Pty Ltd

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
```

## Required Actions for Mycel

### 1. Update Root LICENSE File
Replace the current `LICENSE.txt` with:
- Same GPL-3.0 license text
- Updated copyright: "Copyright (C) 2024 Quantum Research Pty Ltd"
- Add attribution: "Based on Briar, Copyright (C) Briar contributors"

### 2. Add License Headers to Source Files
Add GPL headers to all new/modified source files:

```java
/*
 * Mycel - Secure Messaging Application
 * Copyright (C) 2024 Quantum Research Pty Ltd
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
```

### 3. Source Code Availability
Must provide source code access:
- Include "source available" notice in About dialog
- Provide download link or offer to provide source
- Include in app store descriptions

### 4. Third-Party License Compliance
Preserve existing third-party license files:
- `briar-android/src/main/java/info/guardianproject/LICENSE.txt` (LGPL v2.1)
- Apache License 2.0 headers in third-party files
- All dependency licenses

### 5. App Store Compliance

#### Google Play Store
- Include license information in app description
- Add "Open Source" to app categories
- Mention GPL-3.0 in privacy policy

#### F-Droid
Update metadata:
```yaml
License: GPL-3.0-or-later
AuthorName: Quantum Research Pty Ltd
SourceCode: https://github.com/quantumresearch/mycel
```

### 6. Documentation Updates
Update all documentation to reflect:
- New copyright ownership
- GPL-3.0 license
- Source code availability
- Attribution to original Briar project

## Legal Obligations

### Must Do:
1. **Keep GPL License**: Cannot change to proprietary license
2. **Provide Source**: Make source code available to users
3. **Preserve Attribution**: Credit original Briar contributors
4. **License Compatibility**: Ensure all dependencies are GPL-compatible

### Cannot Do:
1. **Proprietary Distribution**: Cannot distribute without source
2. **License Change**: Cannot relicense under different terms
3. **Patent Claims**: Cannot assert patent claims against users
4. **Trademark Confusion**: Must clearly distinguish from Briar

### Recommended:
1. **Clear Attribution**: "Based on Briar by Briar contributors"
2. **Contribution Guidelines**: Encourage community contributions
3. **CLA (Contributor License Agreement)**: For future contributions
4. **Trademark Registration**: Register "Mycel" trademark separately

## Distribution Checklist

- [ ] Update LICENSE.txt with Quantum Research copyright
- [ ] Add license headers to modified source files
- [ ] Include source code availability notice in app
- [ ] Update app store descriptions with license info
- [ ] Preserve all third-party license files
- [ ] Add attribution to Briar in About dialog
- [ ] Create public source code repository
- [ ] Update build scripts with new copyright
- [ ] Review all user-facing text for license compliance
- [ ] Add GPL notice to documentation

## Example License Notice for App

**About Dialog Text**:
```
Mycel v1.0.0
Copyright (C) 2024 Quantum Research Pty Ltd

Based on Briar, Copyright (C) Briar contributors

This program is free software licensed under the GNU General Public License v3.0.
Source code is available at: https://github.com/quantumresearch/mycel

This program comes with ABSOLUTELY NO WARRANTY.
```

This ensures full compliance with GPL requirements while properly attributing the original Briar project.