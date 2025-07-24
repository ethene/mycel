#!/usr/bin/env python3
"""
Generate PNG notification icons from SVG/Vector XML at different Android densities.

This script converts vector notification icons to PNG format at the correct sizes
for different Android screen densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi).

Dependencies:
- cairosvg (pip install cairosvg)
- Pillow (pip install Pillow)

Usage:
    python generate-notification-pngs.py <vector_xml_file> <output_directory>
"""

import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
import tempfile

try:
    import cairosvg
    from PIL import Image
except ImportError as e:
    print(f"Missing dependency: {e}")
    print("Install with: pip install cairosvg Pillow")
    sys.exit(1)

# Android notification icon densities and corresponding sizes (24dp base)
DENSITY_SIZES = {
    'mdpi': 24,    # 1x
    'hdpi': 36,    # 1.5x
    'xhdpi': 48,   # 2x
    'xxhdpi': 72,  # 3x
    'xxxhdpi': 96  # 4x
}

def vector_xml_to_svg(vector_xml_path):
    """Convert Android Vector XML to SVG format."""
    try:
        tree = ET.parse(vector_xml_path)
        root = tree.getroot()
        
        # Define Android namespace
        android_ns = '{http://schemas.android.com/apk/res/android}'
        
        # Extract dimensions and viewport
        width = root.get(f'{android_ns}width', '24dp').replace('dp', '')
        height = root.get(f'{android_ns}height', '24dp').replace('dp', '')
        viewport_width = root.get(f'{android_ns}viewportWidth', width)
        viewport_height = root.get(f'{android_ns}viewportHeight', height)
        
        # Start SVG content
        svg_content = f'''<?xml version="1.0" encoding="UTF-8"?>
<svg width="{width}" height="{height}" viewBox="0 0 {viewport_width} {viewport_height}" 
     xmlns="http://www.w3.org/2000/svg">
'''
        
        # Convert paths (handle both direct paths and paths in groups)
        for path in root.findall(f'.//{android_ns}path'):
            # Skip paths that are inside groups (they'll be handled separately)
            parent = path.getparent()
            if parent is not None and parent.tag.endswith('group'):
                continue
                
            path_data = path.get(f'{android_ns}pathData', '')
            fill_color = path.get(f'{android_ns}fillColor', '#000000')
            stroke_color = path.get(f'{android_ns}strokeColor', 'none')
            stroke_width = path.get(f'{android_ns}strokeWidth', '0')
            
            if path_data:  # Only add paths that have data
                svg_content += f'  <path d="{path_data}" fill="{fill_color}"'
                if stroke_color != 'none':
                    svg_content += f' stroke="{stroke_color}" stroke-width="{stroke_width}"'
                svg_content += '/>\n'
        
        # Handle groups (for transformations)
        for group in root.findall(f'.//{android_ns}group'):
            scale_x = float(group.get(f'{android_ns}scaleX', '1'))
            scale_y = float(group.get(f'{android_ns}scaleY', '1'))
            translate_x = float(group.get(f'{android_ns}translateX', '0'))
            translate_y = float(group.get(f'{android_ns}translateY', '0'))
            
            transform = f"translate({translate_x},{translate_y}) scale({scale_x},{scale_y})"
            svg_content += f'  <g transform="{transform}">\n'
            
            for path in group.findall(f'.//{android_ns}path'):
                path_data = path.get(f'{android_ns}pathData', '')
                fill_color = path.get(f'{android_ns}fillColor', '#000000')
                stroke_color = path.get(f'{android_ns}strokeColor', 'none')
                stroke_width = path.get(f'{android_ns}strokeWidth', '0')
                
                if path_data:  # Only add paths that have data
                    svg_content += f'    <path d="{path_data}" fill="{fill_color}"'
                    if stroke_color != 'none':
                        svg_content += f' stroke="{stroke_color}" stroke-width="{stroke_width}"'
                    svg_content += '/>\n'
            
            svg_content += '  </g>\n'
        
        svg_content += '</svg>'
        return svg_content
        
    except Exception as e:
        print(f"Error parsing vector XML: {e}")
        return None

def generate_png_at_size(svg_content, size, output_path):
    """Generate PNG from SVG content at specified size."""
    try:
        # Convert SVG to PNG using cairosvg
        png_data = cairosvg.svg2png(
            bytestring=svg_content.encode('utf-8'),
            output_width=size,
            output_height=size
        )
        
        # Save PNG file
        with open(output_path, 'wb') as f:
            f.write(png_data)
        
        print(f"Generated: {output_path} ({size}x{size}px)")
        return True
        
    except Exception as e:
        print(f"Error generating PNG at size {size}: {e}")
        return False

def create_density_directories(base_output_dir):
    """Create Android density directories."""
    for density in DENSITY_SIZES.keys():
        density_dir = Path(base_output_dir) / f"drawable-{density}"
        density_dir.mkdir(parents=True, exist_ok=True)
    return True

def generate_notification_icons(vector_xml_path, output_base_dir, icon_name=None):
    """Generate PNG notification icons at all densities."""
    vector_path = Path(vector_xml_path)
    if not vector_path.exists():
        print(f"Error: Vector XML file not found: {vector_xml_path}")
        return False
    
    if icon_name is None:
        icon_name = vector_path.stem
    
    # Convert vector XML to SVG
    print(f"Converting {vector_path.name} to SVG...")
    svg_content = vector_xml_to_svg(vector_xml_path)
    if not svg_content:
        return False
    
    # Create density directories
    create_density_directories(output_base_dir)
    
    # Generate PNGs at each density
    success_count = 0
    for density, size in DENSITY_SIZES.items():
        output_dir = Path(output_base_dir) / f"drawable-{density}"
        output_path = output_dir / f"{icon_name}.png"
        
        if generate_png_at_size(svg_content, size, output_path):
            success_count += 1
    
    print(f"\nGenerated {success_count}/{len(DENSITY_SIZES)} PNG files successfully")
    return success_count == len(DENSITY_SIZES)

def main():
    if len(sys.argv) < 3:
        print("Usage: python generate-notification-pngs.py <vector_xml_file> <output_directory> [icon_name]")
        print("\nExample:")
        print("  python generate-notification-pngs.py notification_ongoing.xml ./output/")
        print("  python generate-notification-pngs.py /path/to/icon.xml ./res/ my_notification")
        sys.exit(1)
    
    vector_xml_path = sys.argv[1]
    output_directory = sys.argv[2]
    icon_name = sys.argv[3] if len(sys.argv) > 3 else None
    
    print(f"Generating notification PNGs from: {vector_xml_path}")
    print(f"Output directory: {output_directory}")
    print(f"Target densities: {', '.join(DENSITY_SIZES.keys())}")
    print(f"Base icon size: 24dp")
    print()
    
    success = generate_notification_icons(vector_xml_path, output_directory, icon_name)
    
    if success:
        print("\n✅ All notification icons generated successfully!")
        print("\nGenerated files:")
        for density, size in DENSITY_SIZES.items():
            name = icon_name or Path(vector_xml_path).stem
            print(f"  drawable-{density}/{name}.png ({size}x{size}px)")
    else:
        print("\n❌ Some errors occurred during generation")
        sys.exit(1)

if __name__ == "__main__":
    main()