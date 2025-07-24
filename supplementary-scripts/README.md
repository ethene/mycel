# SVG ↔ Android Vector Drawable Converter

This directory contains a bidirectional converter between SVG files and Android Vector Drawable XML files.

## Files

- `svg-vector-converter.py` - Main conversion utility script
- `bw_mycel.svg` - Sample SVG file (original Mycel logo)
- `bw_mycel.xml` - Sample Android Vector Drawable XML file
- `README.md` - This documentation

## Usage

```bash
# Convert SVG to Android Vector Drawable
python3 svg-vector-converter.py svg2vector input.svg output.xml

# Convert Android Vector Drawable to SVG
python3 svg-vector-converter.py vector2svg input.xml output.svg

# Analyze a file (SVG or Vector XML)
python3 svg-vector-converter.py analyze input.svg
python3 svg-vector-converter.py analyze input.xml

# Compare two files
python3 svg-vector-converter.py compare file1.svg file2.xml
```

## Format Differences

### SVG Format
- Uses standard SVG elements and CSS-style attributes
- Supports complex structure with groups (`<g>`) and transforms
- Style defined as `style="fill:#000000"`
- Path data in `d` attribute
- Full XML namespace support

**Example:**
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">
  <g id="g1">
    <path style="fill:#000000" d="m283.77,922.13c-13.49..." id="path1" />
  </g>
</svg>
```

### Android Vector Drawable Format
- Uses Android-specific attributes with `android:` namespace
- Flatter structure, paths directly under `<vector>`
- Style defined as `android:fillColor="#000000"`
- Path data in `android:pathData` attribute
- Dimensions in dp units

**Example:**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="1024dp"
    android:height="1024dp"
    android:viewportWidth="1024"
    android:viewportHeight="1024">
  <path
      android:pathData="m283.8,922.1c-13.5..."
      android:fillColor="#000000"/>
</vector>
```

## Key Differences Handled

| Aspect | SVG | Android Vector |
|--------|-----|----------------|
| Fill Color | `style="fill:#000000"` | `android:fillColor="#000000"` |
| Path Data | `d="..."` | `android:pathData="..."` |
| Dimensions | `width="1024"` | `android:width="1024dp"` |
| Viewport | `viewBox="0 0 1024 1024"` | `android:viewportWidth="1024"` |
| Structure | Can have groups & transforms | Flat path structure |
| Namespace | SVG namespace | Android APK namespace |

## Conversion Examples

### Example 1: Basic Logo Conversion

```bash
# Convert Mycel logo from SVG to Vector
python3 svg-vector-converter.py svg2vector bw_mycel.svg mycel_logo.xml

# Convert back to SVG
python3 svg-vector-converter.py vector2svg mycel_logo.xml mycel_restored.svg
```

### Example 2: Analysis and Comparison

```bash
# Analyze original SVG
python3 svg-vector-converter.py analyze bw_mycel.svg

# Compare original SVG with Vector XML
python3 svg-vector-converter.py compare bw_mycel.svg bw_mycel.xml
```

## Limitations

1. **Complex SVG Features**: Advanced SVG features (gradients, filters, animations) are not supported
2. **Transformations**: SVG transforms are not converted to Android equivalents
3. **Text Elements**: Text is not supported in Android Vector Drawables
4. **Multiple Colors**: Only single fill color per path is handled
5. **Precision**: Some precision may be lost in coordinate conversion

## Use Cases for Mycel Project

1. **Icon Conversion**: Convert designer-provided SVG icons to Android Vector Drawables for the app
2. **Logo Assets**: Maintain both SVG (for web/docs) and Vector XML (for Android) versions
3. **Asset Pipeline**: Automate conversion of SVG assets to Android-compatible formats
4. **Cross-Platform**: Ensure visual consistency between web and Android versions

## Understanding the Formats

### SVG Path Data
- Uses absolute (M, L, C) and relative (m, l, c) commands
- Supports cubic bezier curves, arcs, and complex shapes
- Coordinates can be floating-point with high precision

### Android Path Data
- Same path command syntax as SVG
- Coordinate precision may be reduced for performance
- Optimized for mobile rendering

### Color Handling
- SVG: CSS color syntax (`#000000`, `rgb(0,0,0)`, `black`)
- Android: Hex color codes (`#000000`, `#FF000000` with alpha)

## Tips for Best Results

1. **Simplify SVGs**: Remove unnecessary groups and transforms before conversion
2. **Use Single Colors**: Avoid gradients and multiple fills per path
3. **Check Precision**: Verify that coordinate precision meets your needs
4. **Test Rendering**: Always test converted files in both formats
5. **Backup Originals**: Keep original SVG files for future reference

## Created for Mycel Project

This utility was created to support the Mycel messaging app developed by Quantum Research Pty Ltd, facilitating the conversion between SVG assets and Android Vector Drawables for consistent branding across platforms.