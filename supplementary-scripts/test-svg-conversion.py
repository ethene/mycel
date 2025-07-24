#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys

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
            path_data = path.get(f'{android_ns}pathData', '')
            fill_color = path.get(f'{android_ns}fillColor', '#000000')
            
            if path_data:  # Only add paths that have data
                svg_content += f'  <path d="{path_data}" fill="{fill_color}"/>\n'
        
        svg_content += '</svg>'
        return svg_content
        
    except Exception as e:
        print(f"Error parsing vector XML: {e}")
        return None

if __name__ == "__main__":
    xml_path = sys.argv[1] if len(sys.argv) > 1 else "/Users/dmitrystakhin/mycel/mycel-android/src/main/res/drawable-anydpi-v24/notification_ongoing.xml"
    svg = vector_xml_to_svg(xml_path)
    print("SVG CONTENT:")
    print(svg)