# Design System Documentation

Comprehensive design system for the Electronic Services Android application, built with Jetpack
Compose and Material 3. This system provides accessible, consistent, and production-ready UI
components.

## Table of Contents

- [Overview](#overview)
- [Core Principles](#core-principles)
- [Getting Started](#getting-started)
- [Theme Foundation](#theme-foundation)
- [Components Reference](#components-reference)
- [Implementation Patterns](#implementation-patterns)
- [Best Practices](#best-practices)

## Overview

The Electronic Services Design System implements Material 3 guidelines with custom branding to
ensure consistency, accessibility, and maintainability across the application. All components follow
a 4dp grid system and support both light and dark themes.

### Key Features

- 36+ production-ready components
- WCAG AA accessibility compliance
- Full light and dark theme support
- 48dp minimum touch targets
- Responsive design for all screen sizes
- Material 3 design language

## Core Principles

### Consistency

- **Spacing**: 4dp grid system (4, 8, 12, 16, 24, 32, 48, 64dp)
- **Corner Radii**: Standardized shapes (4, 8, 12, 16, 28dp)
- **Elevation**: Consistent shadow depths
- **Interaction Feedback**: Ripple effects and state changes

### Accessibility

- **Touch Targets**: Minimum 48dp × 48dp for all interactive elements
- **Color Contrast**: WCAG AA standards compliance
- **Content Descriptions**: Screen reader support for all components
- **State Visibility**: Clear visual states (enabled, disabled, pressed, focused)

### Adaptability

- Responsive design for different screen sizes
- Support for both light and dark themes
- Graceful degradation on smaller devices
- Tablet and phone optimization

## Getting Started

### Add Dependency

In your feature module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":design-system"))
}
```

### Basic Usage

```kotlin
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme
import com.enbridge.electronicservices.designsystem.components.*

@Composable
fun MyScreen() {
    ElectronicServicesTheme {
        Column(modifier = Modifier.padding(Spacing.normal)) {
            PrimaryButton(
                text = "Submit",
                onClick = { /* action */ }
            )
        }
    }
}
```

## Theme Foundation

### Color Palette

#### Brand Colors

```kotlin
Primary: #FFB81C (Yellow)
OnPrimary: #353535 (Dark Grey)
Secondary: #353535 (Grey)
OnSecondary: #FFFFFF (White)
```

#### State Colors

```kotlin
Success: #00C853 (Green)
Warning: #FFC107 (Amber)
Error: #B00020 (Red)
Info: #2196F3 (Blue)
```

### Typography Scale

```kotlin
TitleLarge: 24sp, Bold      // Screen titles
TitleMedium: 20sp, SemiBold // Dialog titles, card headers
BodyLarge: 16sp             // Content text, list items
BodyMedium: 14sp            // Supporting text, captions
LabelLarge: 16sp, Medium    // Button text
```

### Spacing System

```kotlin
Spacing.extraSmall = 4dp
Spacing.small = 8dp
Spacing.medium = 12dp
Spacing.normal = 16dp
Spacing.large = 24dp
Spacing.extraLarge = 32dp
Spacing.huge = 48dp
Spacing.massive = 64dp
```

**Usage:**

```kotlin
Column(
    modifier = Modifier.padding(Spacing.normal),
    verticalArrangement = Arrangement.spacedBy(Spacing.small)
) {
    // Content
}
```

## Components Reference

### Buttons

#### PrimaryButton

Filled button with primary yellow color for main call-to-action.

```kotlin
PrimaryButton(
    text = "Submit",
    onClick = { /* action */ },
    icon = Icons.Default.Check,
    enabled = true,
    modifier = Modifier.fillMaxWidth()
)
```

#### SecondaryButton

Outlined button with secondary grey color for alternative actions.

```kotlin
SecondaryButton(
    text = "Cancel",
    onClick = { /* action */ }
)
```

#### AppTextButton

Text-only button for low-emphasis actions.

```kotlin
AppTextButton(
    text = "Skip",
    onClick = { /* action */ }
)
```

#### AppIconButton

Icon button for toolbar and navigation actions with 48dp touch target.

```kotlin
AppIconButton(
    icon = Icons.Default.Search,
    contentDescription = "Search",
    onClick = { /* action */ }
)
```

#### AppFloatingActionButton

Floating action button for primary screen actions.

```kotlin
AppFloatingActionButton(
    icon = Icons.Default.Add,
    contentDescription = "Add Item",
    onClick = { /* action */ },
    useSecondaryColor = false
)
```

### Selection Controls

#### AppCheckbox

Checkbox with integrated label for multiple selections.

```kotlin
AppCheckbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it },
    label = "Accept Terms and Conditions"
)
```

#### AppRadioButton

Radio button with label for single selection from group.

```kotlin
AppRadioButton(
    selected = selectedOption == "option1",
    onClick = { selectedOption = "option1" },
    label = "Option 1"
)
```

#### AppChip

Filter chip with selection states for categories and filters.

```kotlin
AppChip(
    label = "Category",
    selected = isSelected,
    onClick = { isSelected = !isSelected },
    leadingIcon = Icons.Default.FilterList
)
```

### Text Input

#### AppTextField

Comprehensive text field with icons, validation, and error states.

```kotlin
AppTextField(
    value = text,
    onValueChange = { text = it },
    label = "Email",
    placeholder = "Enter your email",
    leadingIcon = Icons.Default.Email,
    trailingIcon = Icons.Default.Check,
    supportingText = if (isError) "Invalid email" else null,
    isError = isError,
    modifier = Modifier.fillMaxWidth()
)
```

### Progress Indicators

#### AppProgressIndicator

Unified progress indicator supporting circular and linear types.

```kotlin
// Circular indeterminate
AppProgressIndicator(
    type = ProgressIndicatorType.CIRCULAR
)

// Linear determinate
AppProgressIndicator(
    type = ProgressIndicatorType.LINEAR,
    progress = 0.65f
)
```

#### LoadingView

Full-screen or overlay loader with animated brand icon.

```kotlin
if (isLoading) {
    LoadingView(
        message = "Loading data...",
        fullScreen = true
    )
}
```

#### CompactLoader

Inline loader for smaller sections and cards.

```kotlin
CompactLoader(message = "Syncing...")
```

### Dialogs

#### AppDialog

Custom dialog with type-based styling and colored indicators.

```kotlin
AppDialog(
    onDismissRequest = { showDialog = false },
    title = "Confirm Action",
    type = DialogType.WARNING, // INFO, WARNING, ERROR, SUCCESS
    content = {
        Text("Are you sure you want to proceed?")
    },
    confirmButton = {
        PrimaryButton(
            text = "Confirm",
            onClick = { /* action */ }
        )
    },
    dismissButton = {
        AppTextButton(
            text = "Cancel",
            onClick = { showDialog = false }
        )
    }
)
```

### Feedback

#### AppSnackbar

Type-based snackbar with color-coded feedback.

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

AppSnackbarHost(
    hostState = snackbarHostState,
    snackbarType = SnackbarType.SUCCESS // INFO, WARNING, ERROR, SUCCESS
)

// Show snackbar
scope.launch {
    snackbarHostState.showSnackbar("Operation completed successfully!")
}
```

### Dropdowns

#### SingleSelectDropdown

Standard dropdown for selecting one item.

```kotlin
SingleSelectDropdown(
    items = listOf("Option 1", "Option 2", "Option 3"),
    selectedItem = selectedItem,
    onItemSelected = { selectedItem = it },
    label = "Select Option"
)
```

#### MultiSelectDropdown

Multi-select dropdown with chip display.

```kotlin
MultiSelectDropdown(
    items = categories,
    selectedItems = selectedCategories,
    onItemsSelected = { selectedCategories = it },
    label = "Select Categories",
    itemLabel = { it.name }
)
```

### Pickers

#### AppDatePicker

Material 3 date picker with theme styling.

```kotlin
AppDatePicker(
    selectedDateMillis = selectedDate,
    onDateSelected = { selectedDate = it },
    label = "Select Date",
    modifier = Modifier.fillMaxWidth()
)
```

#### AppTimePicker

Time picker with 12/24 hour format support.

```kotlin
AppTimePicker(
    selectedHour = selectedHour,
    selectedMinute = selectedMinute,
    onTimeSelected = { hour, minute ->
        selectedHour = hour
        selectedMinute = minute
    },
    label = "Select Time",
    is24Hour = false,
    modifier = Modifier.fillMaxWidth()
)
```

### Bottom Sheets

#### AppBottomSheet

Modal bottom sheet with drag handle.

```kotlin
var showBottomSheet by remember { mutableStateOf(false) }

if (showBottomSheet) {
    AppBottomSheet(
        onDismissRequest = { showBottomSheet = false },
        title = "Bottom Sheet Title"
    ) {
        // Content
    }
}
```

#### ActionBottomSheet

Bottom sheet with built-in action buttons.

```kotlin
ActionBottomSheet(
    onDismissRequest = { showSheet = false },
    title = "Confirm Action",
    confirmButtonText = "Confirm",
    onConfirmClick = { /* action */ },
    dismissButtonText = "Cancel"
) {
    Text("Sheet content")
}
```

### Layout Components

#### InfoCard

Card container with elevation for content grouping.

```kotlin
InfoCard {
    Column {
        Text("Card Title", style = MaterialTheme.typography.titleMedium)
        Text("Card content", style = MaterialTheme.typography.bodyMedium)
    }
}
```

#### AppTopBar

Top app bar with edge-to-edge support and window insets handling.

```kotlin
AppTopBar(
    title = "Screen Title",
    onActionClick = { /* search */ }
)
```

#### AppLogo

Application logo with brand icon and text.

```kotlin
AppLogo()
```

## Implementation Patterns

### Pattern 1: Form Screen

```kotlin
@Composable
fun LoginScreen(onLoginClick: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    ElectronicServicesTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.normal)
            ) {
                AppLogo()
                
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = Icons.Default.Email,
                    modifier = Modifier.fillMaxWidth()
                )
                
                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                PrimaryButton(
                    text = "Login",
                    onClick = { onLoginClick(email, password) },
                    enabled = email.isNotEmpty() && password.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (isLoading) {
                LoadingView(message = "Logging in...")
            }
        }
    }
}
```

### Pattern 2: List Screen with FAB

```kotlin
@Composable
fun JobsListScreen(
    jobs: List<Job>,
    onJobClick: (Job) -> Unit,
    onAddClick: () -> Unit
) {
    ElectronicServicesTheme {
        Scaffold(
            topBar = { AppTopBar(title = "Jobs") },
            floatingActionButton = {
                AppFloatingActionButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add Job",
                    onClick = onAddClick
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Spacing.normal),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                items(jobs) { job ->
                    InfoCard(modifier = Modifier.clickable { onJobClick(job) }) {
                        Text(job.title, style = MaterialTheme.typography.titleMedium)
                        Text(job.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
```

### Pattern 3: Confirmation Dialog

```kotlin
@Composable
fun DeleteConfirmation(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = "Delete Item",
        type = DialogType.WARNING,
        content = {
            Text("Are you sure you want to delete \"$itemName\"? This action cannot be undone.")
        },
        confirmButton = {
            PrimaryButton(text = "Delete", onClick = onConfirm)
        },
        dismissButton = {
            AppTextButton(text = "Cancel", onClick = onDismiss)
        }
    )
}
```

### Pattern 4: Feedback with Snackbar

```kotlin
@Composable
fun ScreenWithFeedback() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ElectronicServicesTheme {
        Scaffold(
            snackbarHost = {
                AppSnackbarHost(
                    hostState = snackbarHostState,
                    snackbarType = SnackbarType.SUCCESS
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                PrimaryButton(
                    text = "Save",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Changes saved successfully!")
                        }
                    }
                )
            }
        }
    }
}
```

## Best Practices

### DO

1. **Use theme colors**
   ```kotlin
   Text(color = MaterialTheme.colorScheme.onSurface)
   ```

2. **Use spacing constants**
   ```kotlin
   padding(Spacing.normal)
   ```

3. **Use typography styles**
   ```kotlin
   Text(style = MaterialTheme.typography.bodyLarge)
   ```

4. **Provide content descriptions**
   ```kotlin
   Icon(contentDescription = "Search")
   ```

5. **Wrap with theme**
   ```kotlin
   ElectronicServicesTheme { /* content */ }
   ```

### DON'T

1. **Don't hardcode colors**
   ```kotlin
   // Avoid
   Text(color = Color(0xFF000000))
   ```

2. **Don't hardcode spacing**
   ```kotlin
   // Avoid
   padding(16.dp)
   ```

3. **Don't create custom text styles**
   ```kotlin
   // Avoid
   Text(fontSize = 24.sp, fontWeight = FontWeight.Bold)
   ```

4. **Don't skip accessibility**
   ```kotlin
   // Avoid
   Icon(contentDescription = null)
   ```

5. **Don't use small touch targets**
   ```kotlin
   // Avoid - too small
   Icon(modifier = Modifier.size(20.dp).clickable { })
   ```

## Component Selection Guide

| Need                | Component                                  |
|---------------------|--------------------------------------------|
| Primary action      | `PrimaryButton`                            |
| Secondary action    | `SecondaryButton`                          |
| Dismiss action      | `AppTextButton`                            |
| Toolbar icon        | `AppIconButton`                            |
| Main screen action  | `AppFloatingActionButton`                  |
| Multiple selections | `AppCheckbox` or `MultiSelectDropdown`     |
| Single selection    | `AppRadioButton` or `SingleSelectDropdown` |
| Text input          | `AppTextField`                             |
| Loading state       | `LoadingView` or `CompactLoader`           |
| Progress tracking   | `AppProgressIndicator`                     |
| Quick feedback      | `AppSnackbar`                              |
| Important message   | `AppDialog`                                |
| Date selection      | `AppDatePicker`                            |
| Time selection      | `AppTimePicker`                            |
| Additional options  | `AppBottomSheet`                           |
| Filters/tags        | `AppChip`                                  |

## Responsive Design

All components adapt to different screen sizes:

```kotlin
@Composable
fun ResponsiveLayout() {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Column(
        modifier = Modifier.padding(
            horizontal = if (isTablet) Spacing.extraLarge else Spacing.normal
        )
    ) {
        // Content
    }
}
```

## Dark Theme Support

All components automatically support dark theme:

```kotlin
ElectronicServicesTheme(
    darkTheme = isSystemInDarkTheme()
) {
    // Your app content adapts automatically
}
```

## Accessibility Features

- **Touch Targets**: 48dp minimum on all interactive elements
- **Color Contrast**: WCAG AA compliant color combinations
- **Screen Reader**: Content descriptions on all icons and interactive elements
- **Keyboard Navigation**: Tab order follows logical flow
- **Focus Indicators**: Yellow border on focused elements
- **State Announcements**: Clear feedback for state changes

## Testing Checklist

When implementing design system components:

- [ ] All interactive elements are at least 48dp
- [ ] Colors come from theme (no hardcoded colors)
- [ ] Spacing follows 4dp grid
- [ ] Typography uses theme styles
- [ ] Icons have content descriptions
- [ ] Works in both light and dark themes
- [ ] Buttons show proper states (disabled, pressed)
- [ ] Loading states are handled
- [ ] Error states are shown with proper colors
- [ ] Forms have validation feedback

## Resources

- [Material 3 Guidelines](https://m3.material.io/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)

## Contributing

When adding new components:

1. Follow established design principles
2. Ensure accessibility compliance
3. Support both light and dark themes
4. Add comprehensive documentation
5. Include usage examples
6. Test on multiple screen sizes

---
