#!/usr/bin/env python3
"""
SVG to Android Vector Drawable Converter

This script converts between SVG files and Android Vector Drawable XML files.
It supports bidirectional conversion and provides analysis of both formats.

Usage:
    python svg-vector-converter.py svg2vector input.svg output.xml
    python svg-vector-converter.py vector2svg input.xml output.svg
    python svg-vector-converter.py analyze input.svg
    python svg-vector-converter.py analyze input.xml

Author: Claude Code Assistant
Created for Mycel project by Quantum Research Pty Ltd
"""

import sys
import xml.etree.ElementTree as ET
import re
from pathlib import Path
from typing import Dict, List, Tuple, Optional


class SVGVectorConverter:
    """
    Handles conversion between SVG and Android Vector Drawable formats.
    
    Key differences between formats:
    - SVG uses CSS-style attributes (style="fill:#000000")
    - Vector uses Android attributes (android:fillColor="#000000")
    - SVG has more complex structure with groups, transforms
    - Vector is flatter with direct path elements
    """
    
    def __init__(self):
        self.svg_namespaces = {
            'svg': 'http://www.w3.org/2000/svg',
            'xmlns': 'http://www.w3.org/2000/svg'
        }
        self.android_namespace = "http://schemas.android.com/apk/res/android"
    
    def analyze_svg(self, file_path: str) -> Dict:
        """Analyze SVG file structure and extract key information"""
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            
            analysis = {
                'format': 'SVG',
                'file': file_path,
                'root_tag': root.tag,
                'namespaces': dict(root.attrib) if hasattr(root, 'attrib') else {},
                'dimensions': {},
                'paths': [],
                'styles': [],
                'structure': []
            }
            
            # Extract dimensions
            for attr in ['width', 'height', 'viewBox']:
                if attr in root.attrib:
                    analysis['dimensions'][attr] = root.attrib[attr]
            
            # Find all path elements (handle namespaced and non-namespaced)
            paths = []
            # Try with different namespace approaches
            for elem in root.iter():
                if elem.tag.endswith('path') or elem.tag == 'path':
                    paths.append(elem)
            
            for i, path in enumerate(paths):
                path_info = {
                    'index': i,
                    'id': path.get('id', f'path_{i}'),
                    'data': path.get('d', ''),
                    'style': {},
                    'attributes': dict(path.attrib)
                }
                
                # Parse style attribute
                style = path.get('style', '')
                if style:
                    path_info['style'] = self._parse_css_style(style)
                
                analysis['paths'].append(path_info)
            
            # Analyze structure
            self._analyze_element_structure(root, analysis['structure'], 0)
            
            return analysis
            
        except Exception as e:
            return {'error': str(e), 'format': 'SVG'}
    
    def analyze_vector(self, file_path: str) -> Dict:
        """Analyze Android Vector Drawable file structure"""
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            
            analysis = {
                'format': 'Android Vector Drawable',
                'file': file_path,
                'root_tag': root.tag,
                'namespaces': dict(root.attrib) if hasattr(root, 'attrib') else {},
                'dimensions': {},
                'paths': [],
                'attributes': dict(root.attrib),
                'structure': []
            }
            
            # Extract vector dimensions
            for attr, value in root.attrib.items():
                if 'android' in attr or attr.startswith('{http://schemas.android.com/apk/res/android}'):
                    clean_attr = attr.replace('{http://schemas.android.com/apk/res/android}', 'android:')
                    if not clean_attr.startswith('android:'):
                        clean_attr = f"android:{clean_attr}" if clean_attr != 'xmlns:android' else clean_attr
                    analysis['dimensions'][clean_attr] = value
            
            # Find all path elements
            paths = root.findall('.//path')
            for i, path in enumerate(paths):
                path_info = {
                    'index': i,
                    'data': '',
                    'android_attributes': {},
                    'raw_attributes': dict(path.attrib)
                }
                
                # Extract android attributes
                for attr, value in path.attrib.items():
                    if 'android' in attr or attr.startswith('{http://schemas.android.com/apk/res/android}'):
                        clean_attr = attr.replace('{http://schemas.android.com/apk/res/android}', 'android:')
                        if not clean_attr.startswith('android:') and clean_attr != 'xmlns:android':
                            clean_attr = f"android:{clean_attr}"
                        path_info['android_attributes'][clean_attr] = value
                        if clean_attr == 'android:pathData':
                            path_info['data'] = value
                
                analysis['paths'].append(path_info)
            
            # Analyze structure
            self._analyze_element_structure(root, analysis['structure'], 0)
            
            return analysis
            
        except Exception as e:
            return {'error': str(e), 'format': 'Android Vector Drawable'}
    
    def svg_to_vector(self, svg_path: str, vector_path: str) -> bool:
        """Convert SVG file to Android Vector Drawable XML"""
        try:
            # Parse SVG
            svg_tree = ET.parse(svg_path)
            svg_root = svg_tree.getroot()
            
            # Create vector root element
            vector_root = ET.Element('vector')
            vector_root.set('xmlns:android', self.android_namespace)
            
            # Extract and convert dimensions
            width = svg_root.get('width', '24dp')
            height = svg_root.get('height', '24dp')
            viewbox = svg_root.get('viewBox', '0 0 24 24')
            
            # Convert dimensions
            if width.replace('.', '').isdigit():
                width = width + 'dp'
            if height.replace('.', '').isdigit():
                height = height + 'dp'
                
            vector_root.set('android:width', width)
            vector_root.set('android:height', height)
            
            # Parse viewBox
            if viewbox:
                parts = viewbox.split()
                if len(parts) >= 4:
                    vector_root.set('android:viewportWidth', parts[2])
                    vector_root.set('android:viewportHeight', parts[3])
            
            # Find and convert paths (handle namespaced paths)
            svg_paths = []
            for elem in svg_root.iter():
                if elem.tag.endswith('path') or elem.tag == 'path':
                    svg_paths.append(elem)
            
            for svg_path in svg_paths:
                # Create vector path element
                vector_path_elem = ET.SubElement(vector_root, 'path')
                
                # Convert path data
                path_data = svg_path.get('d', '')
                if path_data:
                    vector_path_elem.set('android:pathData', path_data)
                
                # Convert styles
                style = svg_path.get('style', '')
                fill_color = '#000000'  # default
                
                if style:
                    style_dict = self._parse_css_style(style)
                    if 'fill' in style_dict:
                        fill_color = style_dict['fill']
                
                # Handle direct fill attribute
                if svg_path.get('fill'):
                    fill_color = svg_path.get('fill')
                
                vector_path_elem.set('android:fillColor', fill_color)
            
            # Write vector XML
            self._write_pretty_xml(vector_root, vector_path)
            return True
            
        except Exception as e:
            print(f"Error converting SVG to Vector: {e}")
            return False
    
    def vector_to_svg(self, vector_path: str, svg_path: str) -> bool:
        """Convert Android Vector Drawable XML to SVG file"""
        try:
            # Parse vector XML
            vector_tree = ET.parse(vector_path)
            vector_root = vector_tree.getroot()
            
            # Create SVG root
            svg_root = ET.Element('svg')
            svg_root.set('version', '1.1')
            svg_root.set('xmlns', 'http://www.w3.org/2000/svg')
            svg_root.set('xmlns:svg', 'http://www.w3.org/2000/svg')
            
            # Convert dimensions
            width = self._extract_android_attr(vector_root, 'width', '24dp')
            height = self._extract_android_attr(vector_root, 'height', '24dp')
            viewport_width = self._extract_android_attr(vector_root, 'viewportWidth', '24')
            viewport_height = self._extract_android_attr(vector_root, 'viewportHeight', '24')
            
            # Clean up dp units
            width = width.replace('dp', '')
            height = height.replace('dp', '')
            
            svg_root.set('width', width)
            svg_root.set('height', height)
            svg_root.set('viewBox', f"0 0 {viewport_width} {viewport_height}")
            
            # Create group for paths
            g_elem = ET.SubElement(svg_root, 'g')
            g_elem.set('id', 'converted_paths')
            
            # Convert paths
            vector_paths = vector_root.findall('.//path')
            
            for i, vector_path in enumerate(vector_paths):
                svg_path_elem = ET.SubElement(g_elem, 'path')
                
                # Convert path data
                path_data = self._extract_android_attr(vector_path, 'pathData', '')
                if path_data:
                    svg_path_elem.set('d', path_data)
                
                # Convert fill color
                fill_color = self._extract_android_attr(vector_path, 'fillColor', '#000000')
                svg_path_elem.set('style', f'fill:{fill_color}')
                
                # Add ID
                svg_path_elem.set('id', f'path_{i}')
            
            # Write SVG
            self._write_pretty_xml(svg_root, svg_path)
            return True
            
        except Exception as e:
            print(f"Error converting Vector to SVG: {e}")
            return False
    
    def _parse_css_style(self, style_str: str) -> Dict[str, str]:
        """Parse CSS style string into dictionary"""
        style_dict = {}
        if not style_str:
            return style_dict
            
        for rule in style_str.split(';'):
            if ':' in rule:
                key, value = rule.split(':', 1)
                style_dict[key.strip()] = value.strip()
        
        return style_dict
    
    def _extract_android_attr(self, element: ET.Element, attr_name: str, default: str = '') -> str:
        """Extract Android attribute, handling namespace"""
        # Try with namespace
        namespaced = f"{{{self.android_namespace}}}{attr_name}"
        if namespaced in element.attrib:
            return element.attrib[namespaced]
        
        # Try with android: prefix
        prefixed = f"android:{attr_name}"
        if prefixed in element.attrib:
            return element.attrib[prefixed]
        
        # Try without namespace
        if attr_name in element.attrib:
            return element.attrib[attr_name]
        
        return default
    
    def _analyze_element_structure(self, element: ET.Element, structure: List, depth: int):
        """Recursively analyze XML element structure"""
        indent = "  " * depth
        tag_name = element.tag.split('}')[-1] if '}' in element.tag else element.tag
        
        elem_info = {
            'tag': tag_name,
            'depth': depth,
            'attributes': len(element.attrib),
            'children': len(list(element)),
            'text': bool(element.text and element.text.strip())
        }
        
        structure.append(elem_info)
        
        for child in element:
            self._analyze_element_structure(child, structure, depth + 1)
    
    def _write_pretty_xml(self, root: ET.Element, file_path: str):
        """Write XML with proper formatting"""
        # Create string representation
        xml_str = ET.tostring(root, encoding='unicode')
        
        # Add XML declaration and format
        formatted_xml = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n'
        
        # Simple pretty printing
        formatted_xml += self._format_xml_string(xml_str)
        
        # Write to file
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(formatted_xml)
    
    def _format_xml_string(self, xml_str: str) -> str:
        """Simple XML formatting"""
        # This is a basic formatter - for production use, consider using xml.dom.minidom
        lines = []
        indent = 0
        
        # Split into tokens
        tokens = re.split(r'(<[^>]+>)', xml_str)
        
        for token in tokens:
            if not token.strip():
                continue
                
            if token.startswith('</'):
                indent -= 2
                lines.append(' ' * indent + token)
            elif token.startswith('<') and not token.endswith('/>'):
                lines.append(' ' * indent + token)
                if not token.startswith('<?') and not token.startswith('<!--'):
                    indent += 2
            elif token.startswith('<') and token.endswith('/>'):
                lines.append(' ' * indent + token)
            else:
                if token.strip():
                    lines.append(' ' * indent + token.strip())
        
        return '\n'.join(lines)


def print_analysis(analysis: Dict):
    """Print formatted analysis results"""
    if 'error' in analysis:
        print(f"ERROR: {analysis['error']}")
        return
    
    print(f"\n=== {analysis['format']} Analysis ===")
    print(f"File: {analysis['file']}")
    print(f"Root element: {analysis['root_tag']}")
    
    if 'dimensions' in analysis and analysis['dimensions']:
        print(f"\nDimensions:")
        for key, value in analysis['dimensions'].items():
            print(f"  {key}: {value}")
    
    if 'paths' in analysis:
        print(f"\nPaths found: {len(analysis['paths'])}")
        for i, path in enumerate(analysis['paths'][:3]):  # Show first 3 paths
            print(f"  Path {i}:")
            if 'id' in path:
                print(f"    ID: {path['id']}")
            if 'data' in path and path['data']:
                data_preview = path['data'][:100] + "..." if len(path['data']) > 100 else path['data']
                print(f"    Data: {data_preview}")
            if 'style' in path and path['style']:
                print(f"    Style: {path['style']}")
            if 'android_attributes' in path and path['android_attributes']:
                print(f"    Android attrs: {path['android_attributes']}")
        
        if len(analysis['paths']) > 3:
            print(f"  ... and {len(analysis['paths']) - 3} more paths")
    
    if 'structure' in analysis:
        print(f"\nStructure:")
        for elem in analysis['structure'][:10]:  # Show first 10 elements
            indent = "  " * elem['depth']
            print(f"  {indent}<{elem['tag']}> (attrs: {elem['attributes']}, children: {elem['children']})")
        
        if len(analysis['structure']) > 10:
            print(f"  ... and {len(analysis['structure']) - 10} more elements")


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return
    
    command = sys.argv[1].lower()
    converter = SVGVectorConverter()
    
    if command == 'svg2vector':
        if len(sys.argv) != 4:
            print("Usage: svg2vector <input.svg> <output.xml>")
            return
        
        input_file, output_file = sys.argv[2], sys.argv[3]
        print(f"Converting SVG '{input_file}' to Vector '{output_file}'...")
        
        if converter.svg_to_vector(input_file, output_file):
            print("✅ Conversion successful!")
            
            # Show analysis of both files
            print("\nSource SVG analysis:")
            svg_analysis = converter.analyze_svg(input_file)
            print_analysis(svg_analysis)
            
            print("\nGenerated Vector analysis:")
            vector_analysis = converter.analyze_vector(output_file)
            print_analysis(vector_analysis)
        else:
            print("❌ Conversion failed!")
    
    elif command == 'vector2svg':
        if len(sys.argv) != 4:
            print("Usage: vector2svg <input.xml> <output.svg>")
            return
        
        input_file, output_file = sys.argv[2], sys.argv[3]
        print(f"Converting Vector '{input_file}' to SVG '{output_file}'...")
        
        if converter.vector_to_svg(input_file, output_file):
            print("✅ Conversion successful!")
            
            # Show analysis of both files
            print("\nSource Vector analysis:")
            vector_analysis = converter.analyze_vector(input_file)
            print_analysis(vector_analysis)
            
            print("\nGenerated SVG analysis:")
            svg_analysis = converter.analyze_svg(output_file)
            print_analysis(svg_analysis)
        else:
            print("❌ Conversion failed!")
    
    elif command == 'analyze':
        if len(sys.argv) != 3:
            print("Usage: analyze <input.svg|input.xml>")
            return
        
        input_file = sys.argv[2]
        file_path = Path(input_file)
        
        if not file_path.exists():
            print(f"❌ File not found: {input_file}")
            return
        
        # Determine file type and analyze
        if file_path.suffix.lower() == '.svg':
            analysis = converter.analyze_svg(input_file)
        elif file_path.suffix.lower() == '.xml':
            analysis = converter.analyze_vector(input_file)
        else:
            print("❌ Unsupported file type. Use .svg or .xml files.")
            return
        
        print_analysis(analysis)
    
    elif command == 'compare':
        if len(sys.argv) != 4:
            print("Usage: compare <file1.svg|file1.xml> <file2.svg|file2.xml>")
            return
        
        file1, file2 = sys.argv[2], sys.argv[3]
        
        # Analyze both files
        if Path(file1).suffix.lower() == '.svg':
            analysis1 = converter.analyze_svg(file1)
        else:
            analysis1 = converter.analyze_vector(file1)
        
        if Path(file2).suffix.lower() == '.svg':
            analysis2 = converter.analyze_svg(file2)
        else:
            analysis2 = converter.analyze_vector(file2)
        
        print_analysis(analysis1)
        print_analysis(analysis2)
        
        # Compare key aspects
        print(f"\n=== Comparison ===")
        print(f"Paths: {len(analysis1.get('paths', []))} vs {len(analysis2.get('paths', []))}")
        
    else:
        print("❌ Unknown command. Available commands:")
        print("  svg2vector <input.svg> <output.xml>")
        print("  vector2svg <input.xml> <output.svg>")
        print("  analyze <input.svg|input.xml>")
        print("  compare <file1> <file2>")


if __name__ == '__main__':
    main()