# Design System Catalog App 🎨

A standalone Android application that showcases all components from the GPS Device Project design
system. This catalog serves as both a visual reference and an interactive testing tool for
developers.

## Purpose

The app-catalog module provides:

- **Visual Reference**: See all design system components in one place
- **Interactive Testing**: Test component states, interactions, and behaviors
- **Theme Testing**: Toggle between light and dark themes to ensure proper styling
- **Documentation**: Live examples of each component with descriptions
- **Quality Assurance**: Verify component appearance and functionality

## Features

### 📱 Complete Component Showcase

The catalog displays all design system components organized into sections:

1. **Theme Colors** - Primary, Secondary, and Error color swatches
2. **Buttons** - All button variants (Primary, Secondary, Text, Icon, FAB)
3. **Selection Controls** - Checkboxes, Radio Buttons, and Filter Chips
4. **Text Fields** - Input fields with validation, icons, and error states
5. **Progress Indicators** - Circular and Linear progress indicators
6. **Dialogs** - Type-based dialogs (INFO, WARNING, ERROR, SUCCESS)
7. **Snackbars** - Feedback snackbars with different types
8. **Dropdowns** - Single and Multi-select dropdowns with chip display
9. **Loading Views** - Full-screen and compact loaders with animations
10. **Pickers** - Date and Time pickers with Material 3 styling
11. **Bottom Sheets** - Modal bottom sheets with drag handles
12. **Cards** - InfoCard containers with elevation
13. **App Logo** - Branded logo component
14. **Spacing Guide** - 4dp grid system spacing constants

### 🌓 Theme Toggle

- Built-in light/dark theme switcher in the top-left corner
- Test all components in both themes instantly
- Ensures proper color contrast and visibility

### 📋 Interactive Examples

- All components are fully interactive
- State changes are visible in real-time
- Dialogs and bottom sheets can be triggered
- Form inputs are functional

## How to Run

### Option 1: Android Studio

1. Open the project in Android Studio
2. In the "Run Configuration" dropdown at the top, select **"app-catalog"**
3. Click the Run button (▶️) or press `Shift+F10`
4. The catalog app will launch on your device/emulator

### Option 2: Command Line

```bash
# From project root
./gradlew :app-catalog:installDebug

# Or run directly
./gradlew :app-catalog:assembleDebug
adb install app-catalog/build/outputs/apk/debug/app-catalog-debug.apk
```

## Module Structure

```
app-catalog/
├── src/
│   └── main/
│       ├── java/com/enbridge/electronicservices/catalog/
│       │   ├── CatalogActivity.kt          # Main activity
│       │   └── ComponentCatalogScreen.kt   # Catalog screen with all sections
│       └── AndroidManifest.xml
├── build.gradle.kts                        # Module configuration
└── README.md                               # This file
```

## Dependencies

The catalog module depends only on:

- `:design-system` - The design system being showcased
- Compose BOM and Material 3
- Core Android libraries

No other feature modules or domain/data layers are required, keeping it lightweight and fast to
build.

## Use Cases

### For Developers

- **Quick Reference**: See how to use each component
- **Copy-Paste Examples**: View implementation patterns
- **Component Testing**: Verify components work as expected
- **Theme Verification**: Check light/dark theme consistency

### For Designers

- **Design Review**: Verify implementation matches designs
- **Color Verification**: Ensure brand colors are correct
- **Spacing Review**: Check spacing consistency (4dp grid)
- **Interaction Review**: Test component behaviors and animations

### For QA

- **Visual Testing**: Screenshot comparison across devices
- **Accessibility Testing**: Verify touch targets and contrast
- **Theme Testing**: Ensure all components support both themes
- **State Testing**: Test enabled/disabled/error states

## Benefits

✅ **Fast Iteration**: No need to navigate through the full app  
✅ **Isolated Testing**: Test components without app dependencies  
✅ **Visual Documentation**: Living documentation of the design system  
✅ **Onboarding Tool**: Help new developers understand available components  
✅ **Design Handoff**: Share with designers for approval  
✅ **Lightweight**: Minimal dependencies, fast build times

## Navigation

The catalog is a single scrollable screen with sections for each component type. Simply scroll
through to see all components, or use the section headers as visual guides.

## Screenshots

The catalog displays:

- A header with the app name and description
- Collapsible sections for each component type
- Interactive examples with state management
- Descriptive text explaining each component
- Visual indicators (emojis) for easy section identification

## Future Enhancements

Potential additions to the catalog:

- [ ] Search functionality to find components quickly
- [ ] Filtering by component type
- [ ] Code snippet display for each example
- [ ] Screenshot export for documentation
- [ ] Accessibility scanner integration
- [ ] Component usage statistics
- [ ] Version history of component changes

## Running Tests

The catalog can be used for manual testing. For automated testing:

```bash
# Run UI tests
./gradlew :app-catalog:connectedAndroidTest

# Run unit tests
./gradlew :app-catalog:test
```

## Contributing

When adding new components to the design system:

1. Add a new section to `ComponentCatalogScreen.kt`
2. Create a composable function following the pattern: `[ComponentName]Section()`
3. Add it to the LazyColumn in `ComponentCatalogScreen()`
4. Provide descriptions and interactive examples

## Notes

- The catalog uses the same theme (`ElectronicServicesTheme`) as the main app
- All components use the design system's spacing, colors, and typography
- The catalog is edge-to-edge for modern Android UI
- Snackbars appear at the bottom of the screen
- Dialogs and bottom sheets properly handle dismissal

## Support

For issues or questions about the catalog:

1. Check the design system documentation in `design-system/README.md`
2. Review the visual guide in `design-system/VISUAL_GUIDE.md`
3. Consult the implementation guide in `design-system/IMPLEMENTATION_GUIDE.md`

---

**Version**: 1.0.0  
**Last Updated**: October 2024  
**Maintained By**: Design System Team
