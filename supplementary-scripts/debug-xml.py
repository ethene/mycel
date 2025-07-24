#!/usr/bin/env python3
import xml.etree.ElementTree as ET

# Parse the XML file
tree = ET.parse('/Users/dmitrystakhin/mycel/mycel-android/src/main/res/drawable-anydpi-v24/notification_ongoing.xml')
root = tree.getroot()

print("Root tag:", root.tag)
print("Root attributes:", root.attrib)
print()

# List all elements
for elem in root.iter():
    print(f"Tag: {elem.tag}")
    print(f"Attributes: {elem.attrib}")
    if elem.text and elem.text.strip():
        print(f"Text: {elem.text.strip()}")
    print()