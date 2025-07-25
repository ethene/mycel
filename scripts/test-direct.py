#!/usr/bin/env python3
import xml.etree.ElementTree as ET

# Parse the XML file
tree = ET.parse('/Users/dmitrystakhin/mycel/mycel-android/src/main/res/drawable-anydpi-v24/notification_ongoing.xml')
root = tree.getroot()

android_ns = '{http://schemas.android.com/apk/res/android}'

# Extract dimensions and viewport
width = root.get(f'{android_ns}width', '24dp').replace('dp', '')
height = root.get(f'{android_ns}height', '24dp').replace('dp', '')
viewport_width = root.get(f'{android_ns}viewportWidth', width)
viewport_height = root.get(f'{android_ns}viewportHeight', height)

print(f"Dimensions: {width}x{height}")
print(f"Viewport: {viewport_width}x{viewport_height}")

# Start SVG content
svg_content = f'''<?xml version="1.0" encoding="UTF-8"?>
<svg width="{width}" height="{height}" viewBox="0 0 {viewport_width} {viewport_height}" 
     xmlns="http://www.w3.org/2000/svg">
'''

print("\nPaths found:")
path_count = 0
for child in root:
    print(f"Child tag: '{child.tag}'")
    print(f"Child attrib: {child.attrib}")
    if child.tag == 'path':  # The tag is just 'path', not with namespace
        path_count += 1
        path_data = child.get(f'{android_ns}pathData', '')
        fill_color = child.get(f'{android_ns}fillColor', '#000000')
        
        print(f"  Path {path_count}: fill={fill_color}")
        print(f"  Data length: {len(path_data)} chars")
        
        if path_data:
            svg_content += f'  <path d="{path_data}" fill="{fill_color}"/>\n'

svg_content += '</svg>'

print(f"\nFinal SVG length: {len(svg_content)} characters")
print("First 200 chars of SVG:")
print(svg_content[:200])

# Write the SVG to test
with open('debug.svg', 'w') as f:
    f.write(svg_content)
print("\nSaved as debug.svg")