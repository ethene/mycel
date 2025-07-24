#!/usr/bin/env python3
"""
Update hardcoded colors in Android Vector XML files to use Mycel color scheme.

This script maps hardcoded hex colors to appropriate Mycel color references
based on color similarity and semantic meaning.
"""

import os
import re
import sys
from pathlib import Path

# Color mapping from hardcoded hex to Mycel color references
COLOR_MAPPINGS = {
    # Black colors → Mycel text/neutral
    '#FF000000': '@color/mycel_text_main',
    '#000000': '@color/mycel_text_main',
    '#000': '@color/mycel_text_main',
    
    # White colors → Mycel inverted text/backgrounds
    '#FFFFFFFF': '@color/mycel_text_invert',
    '#FFFFFF': '@color/mycel_text_invert',
    '#FFF': '@color/mycel_text_invert',
    '#F9FAFB': '@color/mycel_bg_light',
    '#f9fafb': '@color/mycel_bg_light',
    
    # Gray colors → Mycel neutrals
    '#D9D9D9': '@color/mycel_ui_border',
    '#CCCCCC': '@color/mycel_ui_border',
    '#999999': '@color/mycel_ui_muted',
    '#707070': '@color/mycel_ui_muted',  # Gray
    '#666666': '@color/mycel_ui_muted',
    '#2e3d4f': '@color/mycel_ui_muted',  # Dark blue-gray
    
    # Green colors → Mycel green family
    '#65A30D': '@color/mycel_green_dark',
    '#67a60f': '@color/mycel_green_dark',  # Dark green
    '#82c91e': '@color/mycel_green',
    '#A3E635': '@color/mycel_green',
    '#BEF264': '@color/mycel_green',
    '#ECFCCB': '@color/mycel_green_soft',
    '#D9F99D': '@color/mycel_green_soft',
    '#4ade80': '@color/mycel_green',
    '#22c55e': '@color/mycel_green_dark',
    
    # Blue colors → Mycel blue family
    '#196FDE': '@color/mycel_blue_dark',
    '#1e40af': '@color/mycel_blue_dark',
    '#3b82f6': '@color/mycel_blue',
    '#52ADF9': '@color/mycel_blue_dark',
    '#60a5fa': '@color/mycel_blue',
    '#8BCAFD': '@color/mycel_blue',
    '#93c5fd': '@color/mycel_blue',
    '#BDDFFE': '@color/mycel_blue_soft',
    '#dbeafe': '@color/mycel_blue_soft',
    '#DDEDFE': '@color/mycel_blue_soft',
    
    # Red/Orange colors → Mycel coral family
    '#ef4444': '@color/mycel_error',
    '#E32017': '@color/mycel_error',    # Strong red
    '#dc2626': '@color/mycel_error',
    '#f87171': '@color/mycel_coral',
    '#FFf87171': '@color/mycel_coral',  # Red with alpha
    '#fca5a5': '@color/mycel_coral_soft',
    '#f97316': '@color/mycel_coral',
    '#fb923c': '@color/mycel_coral',
    '#fed7aa': '@color/mycel_coral_soft',
    
    # Special cases for specific elements
    '#FFFF00': '@color/mycel_warning',  # Yellow → Warning
    '#FFD700': '@color/mycel_warning',  # Gold → Warning
    
    # Additional specific colors found
    '#2196F3': '@color/mycel_primary',  # Material blue
    '#03A9F4': '@color/mycel_blue',     # Light blue
    '#418cd8': '@color/mycel_blue',     # Blue for icons
    '#418CD8': '@color/mycel_blue',     # Blue for icons (uppercase)
    '#1d1d1b': '@color/mycel_text_main', # Dark text/graphics
    '#1D1D1B': '@color/mycel_text_main', # Dark text/graphics (uppercase)
    '#FFf87171': '@color/mycel_coral',  # Red with alpha
    '#E32017': '@color/mycel_error',    # Strong red
    '#4CAF50': '@color/mycel_green',    # Material green
    '#8BC34A': '@color/mycel_green',    # Light green
    '#CDDC39': '@color/mycel_green_soft', # Lime
    '#9C27B0': '@color/mycel_lavender',  # Purple
    '#FFC107': '@color/mycel_warning',   # Amber
}

def update_vector_xml_colors(file_path):
    """Update hardcoded colors in a vector XML file."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        changes_made = []
        
        # Find all fillColor, strokeColor, and tint attributes with hex values
        pattern = r'android:(fillColor|strokeColor|tint)="(#[A-Fa-f0-9]{3,8})"'
        matches = re.findall(pattern, content)
        
        for attr_name, hex_color in matches:
            # Normalize hex color (handle both #RGB and #RRGGBB formats)
            normalized_hex = hex_color.upper()
            if len(normalized_hex) == 4:  # #RGB → #RRGGBB
                normalized_hex = '#' + ''.join([c*2 for c in normalized_hex[1:]])
            
            # Check if we have a mapping for this color
            if normalized_hex in COLOR_MAPPINGS:
                mycel_color = COLOR_MAPPINGS[normalized_hex]
                old_attr = f'android:{attr_name}="{hex_color}"'
                new_attr = f'android:{attr_name}="{mycel_color}"'
                
                content = content.replace(old_attr, new_attr)
                changes_made.append(f'  {hex_color} → {mycel_color}')
            elif hex_color.upper() in COLOR_MAPPINGS:
                mycel_color = COLOR_MAPPINGS[hex_color.upper()]
                old_attr = f'android:{attr_name}="{hex_color}"'
                new_attr = f'android:{attr_name}="{mycel_color}"'
                
                content = content.replace(old_attr, new_attr)
                changes_made.append(f'  {hex_color} → {mycel_color}')
        
        # Write back if changes were made
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            
            print(f"✅ Updated {file_path.name}:")
            for change in changes_made:
                print(change)
            return True
        else:
            return False
            
    except Exception as e:
        print(f"❌ Error updating {file_path}: {e}")
        return False

def main():
    if len(sys.argv) != 2:
        print("Usage: python update-vector-colors.py <drawable_directory>")
        print("Example: python update-vector-colors.py /Users/dmitrystakhin/mycel/mycel-android/src/main/res/drawable")
        sys.exit(1)
    
    drawable_dir = Path(sys.argv[1])
    
    if not drawable_dir.exists() or not drawable_dir.is_dir():
        print(f"❌ Directory not found: {drawable_dir}")
        sys.exit(1)
    
    print("🎨 Updating Vector XML files to use Mycel colors...")
    print(f"📁 Directory: {drawable_dir}")
    print()
    
    # Find all XML files in drawable directory
    xml_files = list(drawable_dir.glob("*.xml"))
    updated_count = 0
    
    for xml_file in xml_files:
        # Skip files that might not be vector drawables
        if xml_file.name.startswith('.'):
            continue
            
        # Check if file contains vector content
        try:
            with open(xml_file, 'r', encoding='utf-8') as f:
                content = f.read()
                if '<vector' not in content:
                    continue
        except:
            continue
        
        if update_vector_xml_colors(xml_file):
            updated_count += 1
    
    print()
    print(f"🎉 Complete! Updated {updated_count} vector XML files with Mycel colors.")
    print()
    print("Color mapping used:")
    print("  Green colors → mycel_green family")
    print("  Blue colors → mycel_blue family") 
    print("  Red/Orange → mycel_coral family")
    print("  Black/Gray → mycel neutral colors")
    print("  White → mycel_text_invert/mycel_bg_light")

if __name__ == "__main__":
    main()