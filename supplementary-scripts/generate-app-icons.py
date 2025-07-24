#!/usr/bin/env python3
"""
Generate PNG app icons from Android adaptive icon XML sources.

This script creates both square and round app icons at all required densities
by combining the background color and foreground drawable.
"""

import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
import tempfile

try:
    import cairosvg
    from PIL import Image, ImageDraw
except ImportError as e:
    print(f"Missing dependency: {e}")
    print("Install with: pip install cairosvg Pillow")
    sys.exit(1)

# Android app icon densities and sizes
ICON_SIZES = {
    'mdpi': 48,     # 1x
    'hdpi': 72,     # 1.5x
    'xhdpi': 96,    # 2x
    'xxhdpi': 144,  # 3x
    'xxxhdpi': 192  # 4x
}

def read_color_value(color_file_path):
    """Read color value from Android color XML file."""
    try:
        tree = ET.parse(color_file_path)
        root = tree.getroot()
        
        for color in root.findall('color'):
            if color.get('name') == 'ic_launcher_background':
                return color.text.strip()
    except Exception as e:
        print(f"Error reading color file: {e}")
    
    return "#FFFFFF"  # Default white

def vector_xml_to_svg(vector_xml_path, target_size=108):
    """Convert Android Vector XML to SVG format."""
    try:
        tree = ET.parse(vector_xml_path)
        root = tree.getroot()
        
        android_ns = '{http://schemas.android.com/apk/res/android}'
        
        # Extract dimensions and viewport
        width = root.get(f'{android_ns}width', '108dp').replace('dp', '')
        height = root.get(f'{android_ns}height', '108dp').replace('dp', '')
        viewport_width = root.get(f'{android_ns}viewportWidth', width)
        viewport_height = root.get(f'{android_ns}viewportHeight', height)
        
        # Start SVG content  
        svg_content = f'''<?xml version="1.0" encoding="UTF-8"?>
<svg width="{target_size}" height="{target_size}" viewBox="0 0 {viewport_width} {viewport_height}" 
     xmlns="http://www.w3.org/2000/svg">
'''
        
        # Process all children (paths and groups)
        for child in root:
            if child.tag == 'path':
                path_data = child.get(f'{android_ns}pathData', '')
                fill_color = child.get(f'{android_ns}fillColor', '#000000')
                
                if path_data:
                    svg_content += f'  <path d="{path_data}" fill="{fill_color}"/>\n'
                    
            elif child.tag == 'group':
                scale_x = float(child.get(f'{android_ns}scaleX', '1'))
                scale_y = float(child.get(f'{android_ns}scaleY', '1'))
                translate_x = float(child.get(f'{android_ns}translateX', '0'))
                translate_y = float(child.get(f'{android_ns}translateY', '0'))
                
                transform = f"translate({translate_x},{translate_y}) scale({scale_x},{scale_y})"
                svg_content += f'  <g transform="{transform}">\n'
                
                for path in child:
                    if path.tag == 'path':
                        path_data = path.get(f'{android_ns}pathData', '')
                        fill_color = path.get(f'{android_ns}fillColor', '#000000')
                        
                        if path_data:
                            svg_content += f'    <path d="{path_data}" fill="{fill_color}"/>\n'
                
                svg_content += '  </g>\n'
        
        svg_content += '</svg>'
        return svg_content
        
    except Exception as e:
        print(f"Error converting vector XML to SVG: {e}")
        return None

def create_app_icon(background_color, foreground_svg, size, is_round=False):
    """Create app icon with proper background shapes like original Briar design."""
    try:
        from io import BytesIO
        
        # Create transparent background
        icon = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(icon)
        
        if is_round:
            # Round icon: Create white circle with small margin
            margin = size * 0.05  # 5% margin
            circle_size = size - (2 * margin)
            circle_pos = margin
            
            # Draw white circle background
            draw.ellipse(
                (circle_pos, circle_pos, circle_pos + circle_size, circle_pos + circle_size),
                fill=background_color,
                outline=None
            )
            
            # Create logo at appropriate size (smaller than circle)
            logo_size = int(circle_size * 1.30)  # Logo takes 130% of circle
            logo_offset = (size - logo_size) // 2
            
        else:
            # Square icon: Create white rounded rectangle with margins
            margin = size * 0.08  # 8% margin  
            rect_size = size - (2 * margin)
            rect_pos = margin
            corner_radius = size * 0.15  # 15% corner radius
            
            # Draw white rounded rectangle background
            # PIL doesn't have built-in rounded rectangle, so we'll create one
            # Create a mask for rounded rectangle
            mask = Image.new('L', (size, size), 0)
            mask_draw = ImageDraw.Draw(mask)
            
            # Draw rounded rectangle on mask
            # Top-left corner
            mask_draw.pieslice((rect_pos, rect_pos, rect_pos + 2*corner_radius, rect_pos + 2*corner_radius), 180, 270, fill=255)
            # Top-right corner  
            mask_draw.pieslice((rect_pos + rect_size - 2*corner_radius, rect_pos, rect_pos + rect_size, rect_pos + 2*corner_radius), 270, 360, fill=255)
            # Bottom-right corner
            mask_draw.pieslice((rect_pos + rect_size - 2*corner_radius, rect_pos + rect_size - 2*corner_radius, rect_pos + rect_size, rect_pos + rect_size), 0, 90, fill=255)
            # Bottom-left corner
            mask_draw.pieslice((rect_pos, rect_pos + rect_size - 2*corner_radius, rect_pos + 2*corner_radius, rect_pos + rect_size), 90, 180, fill=255)
            
            # Fill the middle areas
            mask_draw.rectangle((rect_pos, rect_pos + corner_radius, rect_pos + rect_size, rect_pos + rect_size - corner_radius), fill=255)
            mask_draw.rectangle((rect_pos + corner_radius, rect_pos, rect_pos + rect_size - corner_radius, rect_pos + rect_size), fill=255)
            
            # Create white background with the mask
            background_img = Image.new('RGBA', (size, size), background_color)
            background_img.putalpha(mask)
            
            # Composite onto icon
            icon = Image.alpha_composite(icon, background_img)
            
            # Create logo at appropriate size
            logo_size = int(rect_size * 1.30)  # Logo takes 130% of rectangle
            logo_offset = (size - logo_size) // 2
        
        # Convert foreground SVG to PNG at the calculated logo size
        foreground_png = cairosvg.svg2png(
            bytestring=foreground_svg.encode('utf-8'),
            output_width=logo_size,
            output_height=logo_size
        )
        
        # Open foreground as PIL Image
        foreground = Image.open(BytesIO(foreground_png)).convert('RGBA')
        
        # Create a new image for positioning the logo
        logo_positioned = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        logo_positioned.paste(foreground, (logo_offset, logo_offset), foreground)
        
        # Composite logo onto icon
        icon = Image.alpha_composite(icon, logo_positioned)
        
        return icon
        
    except Exception as e:
        print(f"Error creating app icon: {e}")
        return None

def generate_app_icons(project_root):
    """Generate all app icon PNG files."""
    project_path = Path(project_root)
    
    # Paths
    color_file = project_path / "mycel-android/src/main/res/values/ic_launcher_background.xml"
    foreground_file = project_path / "mycel-android/src/main/res/drawable/ic_launcher_foreground.xml"
    
    if not color_file.exists():
        print(f"Background color file not found: {color_file}")
        return False
        
    if not foreground_file.exists():
        print(f"Foreground drawable not found: {foreground_file}")
        return False
    
    # Read background color
    background_color = read_color_value(str(color_file))
    print(f"Background color: {background_color}")
    
    # Convert foreground to SVG
    print("Converting foreground to SVG...")
    foreground_svg = vector_xml_to_svg(str(foreground_file))
    if not foreground_svg:
        return False
    
    # Generate icons for each density
    success_count = 0
    total_count = len(ICON_SIZES) * 2  # square + round for each density
    
    for density, size in ICON_SIZES.items():
        print(f"\nGenerating {density} icons ({size}x{size}px)...")
        
        # Square icon
        mipmap_dir = project_path / f"mycel-android/src/main/res/mipmap-{density}"
        mipmap_dir.mkdir(parents=True, exist_ok=True)
        
        square_icon = create_app_icon(background_color, foreground_svg, size, is_round=False)
        if square_icon:
            square_path = mipmap_dir / "ic_launcher.png"
            square_icon.save(str(square_path), "PNG")
            print(f"  ✓ {square_path}")
            success_count += 1
        
        # Round icon
        round_icon = create_app_icon(background_color, foreground_svg, size, is_round=True)
        if round_icon:
            round_path = mipmap_dir / "ic_launcher_round.png"
            round_icon.save(str(round_path), "PNG")
            print(f"  ✓ {round_path}")
            success_count += 1
    
    print(f"\nGenerated {success_count}/{total_count} icons successfully!")
    return success_count == total_count

def main():
    if len(sys.argv) < 2:
        print("Usage: python generate-app-icons.py <project_root>")
        print("Example: python generate-app-icons.py /Users/dmitrystakhin/mycel")
        sys.exit(1)
    
    project_root = sys.argv[1]
    
    print("🚀 Generating Android app icons...")
    print(f"Project root: {project_root}")
    print(f"Target densities: {', '.join(ICON_SIZES.keys())}")
    print(f"Icon types: square + round")
    print()
    
    success = generate_app_icons(project_root)
    
    if success:
        print("\n✅ All app icons generated successfully!")
        print("\nGenerated files:")
        for density, size in ICON_SIZES.items():
            print(f"  mipmap-{density}/ic_launcher.png ({size}x{size}px)")
            print(f"  mipmap-{density}/ic_launcher_round.png ({size}x{size}px)")
    else:
        print("\n❌ Some errors occurred during generation")
        sys.exit(1)

if __name__ == "__main__":
    main()