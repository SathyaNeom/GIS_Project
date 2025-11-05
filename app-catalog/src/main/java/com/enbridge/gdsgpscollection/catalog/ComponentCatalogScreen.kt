package com.enbridge.gdsgpscollection.catalog

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.components.*
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentCatalogScreen() {
    var darkTheme by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    GdsGpsCollectionTheme(darkTheme = darkTheme) {
        AppScaffold(
            topBar = {
                AppTopBar(
                    title = "Design System Catalog",
                    onActionClick = { darkTheme = !darkTheme },
                    navigationIcon = {
                        AppIconButton(
                            icon = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            onClick = { darkTheme = !darkTheme }
                        )
                    }
                )
            },
            snackbarHost = {
                AppSnackbarHost(
                    hostState = snackbarHostState,
                    snackbarType = SnackbarType.INFO
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                    .padding(Spacing.normal),
                verticalArrangement = Arrangement.spacedBy(Spacing.large)
            ) {
                // Header
                item {
                    CatalogHeader()
                }

                // Theme Section
                item {
                    ThemeSection()
                }

                // Buttons Section
                item {
                    ButtonsSection()
                }

                // Selection Controls Section
                item {
                    SelectionControlsSection()
                }

                // Text Fields Section
                item {
                    TextFieldsSection()
                }

                // Progress Indicators Section
                item {
                    ProgressIndicatorsSection()
                }

                // Dialogs Section
                item {
                    DialogsSection()
                }

                // Snackbars Section
                item {
                    SnackbarsSection(snackbarHostState, scope)
                }

                // Dropdowns Section
                item {
                    DropdownsSection()
                }

                // Loading Views Section
                item {
                    LoadingViewsSection(scope)
                }

                // Pickers Section
                item {
                    PickersSection()
                }

                // Bottom Sheets Section
                item {
                    BottomSheetsSection()
                }

                // Cards Section
                item {
                    CardsSection()
                }

                // Logo Section
                item {
                    LogoSection()
                }

                // Spacing Guide
                item {
                    SpacingGuideSection()
                }

                // Footer
                item {
                    Spacer(modifier = Modifier.height(Spacing.large))
                    Text(
                        text = "End of Catalog",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun CatalogHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🎨 Design System Catalog",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(
            text = "GPS Device Project",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(
            text = "Comprehensive showcase of all design system components",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionHeader(title: String, description: String? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.small))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(Spacing.normal))
    }
}

@Composable
fun ThemeSection() {
    InfoCard {
        SectionHeader(
            title = "🎭 Theme Colors",
            description = "Primary: Yellow (#FFB81C), Secondary: Grey (#353535)"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
            ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
            ColorSwatch("Error", MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ColorSwatch(label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(60.dp),
            color = color,
            shape = MaterialTheme.shapes.medium
        ) {}
        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ButtonsSection() {
    InfoCard {
        SectionHeader(
            title = "🔘 Buttons",
            description = "All button variants with proper states and styling"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Text("Primary Button:", style = MaterialTheme.typography.labelLarge)
            PrimaryButton(
                text = "Primary Action",
                onClick = { },
                icon = Icons.Default.Check
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Secondary Button:", style = MaterialTheme.typography.labelLarge)
            SecondaryButton(
                text = "Secondary Action",
                onClick = { },
                icon = Icons.Default.Info
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Text Button:", style = MaterialTheme.typography.labelLarge)
            AppTextButton(
                text = "Cancel",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Icon Button:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                AppIconButton(
                    icon = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    onClick = { }
                )
                AppIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    onClick = { }
                )
                AppIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Floating Action Button:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                AppFloatingActionButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add",
                    onClick = { }
                )
                AppFloatingActionButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit",
                    onClick = { },
                    useSecondaryColor = true
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Disabled State:", style = MaterialTheme.typography.labelLarge)
            PrimaryButton(
                text = "Disabled Button",
                onClick = { },
                enabled = false
            )
        }
    }
}

@Composable
fun SelectionControlsSection() {
    var checkbox1 by remember { mutableStateOf(false) }
    var checkbox2 by remember { mutableStateOf(true) }
    var radioOption by remember { mutableStateOf("option1") }
    var chip1Selected by remember { mutableStateOf(false) }
    var chip2Selected by remember { mutableStateOf(true) }

    InfoCard {
        SectionHeader(
            title = "☑️ Selection Controls",
            description = "Checkboxes, radio buttons, and chips"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.normal)) {
            Text("Checkboxes:", style = MaterialTheme.typography.labelLarge)
            AppCheckbox(
                checked = checkbox1,
                onCheckedChange = { checkbox1 = it },
                label = "Unchecked Checkbox"
            )
            AppCheckbox(
                checked = checkbox2,
                onCheckedChange = { checkbox2 = it },
                label = "Checked Checkbox"
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Radio Buttons:", style = MaterialTheme.typography.labelLarge)
            AppRadioButton(
                selected = radioOption == "option1",
                onClick = { radioOption = "option1" },
                label = "Option 1"
            )
            AppRadioButton(
                selected = radioOption == "option2",
                onClick = { radioOption = "option2" },
                label = "Option 2"
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Filter Chips:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                AppChip(
                    label = "Unselected",
                    selected = chip1Selected,
                    onClick = { chip1Selected = !chip1Selected },
                    leadingIcon = Icons.Default.FilterList
                )
                AppChip(
                    label = "Selected",
                    selected = chip2Selected,
                    onClick = { chip2Selected = !chip2Selected },
                    leadingIcon = Icons.Default.Star
                )
            }
        }
    }
}

@Composable
fun TextFieldsSection() {
    var textField1 by remember { mutableStateOf("") }
    var textField2 by remember { mutableStateOf("Sample Text") }
    var textField3 by remember { mutableStateOf("error@example.com") }
    var password by remember { mutableStateOf("") }

    InfoCard {
        SectionHeader(
            title = "📝 Text Fields",
            description = "Text input fields with validation and icons"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.normal)) {
            Text("Basic Text Field:", style = MaterialTheme.typography.labelLarge)
            AppTextField(
                value = textField1,
                onValueChange = { textField1 = it },
                label = "Label",
                placeholder = "Enter text here"
            )

            Text("With Icons:", style = MaterialTheme.typography.labelLarge)
            AppTextField(
                value = textField2,
                onValueChange = { textField2 = it },
                label = "Username",
                placeholder = "Enter username",
                leadingIcon = Icons.Default.Person,
                trailingIcon = Icons.Default.Check
            )

            Text("Error State:", style = MaterialTheme.typography.labelLarge)
            AppTextField(
                value = textField3,
                onValueChange = { textField3 = it },
                label = "Email",
                placeholder = "Enter email",
                leadingIcon = Icons.Default.Email,
                isError = true,
                supportingText = "Invalid email format"
            )

            Text("Password Field:", style = MaterialTheme.typography.labelLarge)
            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}

@Composable
fun ProgressIndicatorsSection() {
    InfoCard {
        SectionHeader(
            title = "⏳ Progress Indicators",
            description = "Circular and linear progress indicators"
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.normal),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Circular (Indeterminate):", style = MaterialTheme.typography.labelLarge)
            AppProgressIndicator(type = ProgressIndicatorType.CIRCULAR)

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Linear (Determinate - 65%):", style = MaterialTheme.typography.labelLarge)
            AppProgressIndicator(
                type = ProgressIndicatorType.LINEAR,
                progress = 0.65f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Compact Loader:", style = MaterialTheme.typography.labelLarge)
            CompactLoader(message = "Loading...")
        }
    }
}

@Composable
fun DialogsSection() {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    InfoCard {
        SectionHeader(
            title = "💬 Dialogs",
            description = "Type-based dialogs with icons and colored indicators"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SecondaryButton(
                    text = "Info",
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "Warning",
                    onClick = { showWarningDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SecondaryButton(
                    text = "Error",
                    onClick = { showErrorDialog = true },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "Success",
                    onClick = { showSuccessDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Dialog implementations
    if (showInfoDialog) {
        AppDialog(
            onDismissRequest = { showInfoDialog = false },
            title = "Information",
            type = DialogType.INFO,
            content = {
                Text("This is an informational dialog with helpful content.")
            },
            confirmButton = {
                PrimaryButton(text = "OK", onClick = { showInfoDialog = false })
            }
        )
    }

    if (showWarningDialog) {
        AppDialog(
            onDismissRequest = { showWarningDialog = false },
            title = "Warning",
            type = DialogType.WARNING,
            content = {
                Text("This action requires your attention. Are you sure you want to proceed?")
            },
            confirmButton = {
                PrimaryButton(text = "Proceed", onClick = { showWarningDialog = false })
            },
            dismissButton = {
                AppTextButton(text = "Cancel", onClick = { showWarningDialog = false })
            }
        )
    }

    if (showErrorDialog) {
        AppDialog(
            onDismissRequest = { showErrorDialog = false },
            title = "Error",
            type = DialogType.ERROR,
            content = {
                Text("An error occurred while processing your request. Please try again.")
            },
            confirmButton = {
                PrimaryButton(text = "Retry", onClick = { showErrorDialog = false })
            }
        )
    }

    if (showSuccessDialog) {
        AppDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = "Success",
            type = DialogType.SUCCESS,
            content = {
                Text("Your action was completed successfully!")
            },
            confirmButton = {
                PrimaryButton(text = "Great!", onClick = { showSuccessDialog = false })
            }
        )
    }
}

@Composable
fun SnackbarsSection(
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    InfoCard {
        SectionHeader(
            title = "🍿 Snackbars",
            description = "Type-based snackbars for feedback"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SecondaryButton(
                    text = "Info",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Information message")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "Success",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Success message")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Note: Snackbars appear at the bottom of the screen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DropdownsSection() {
    var singleSelection by remember { mutableStateOf<String?>(null) }
    var multiSelection by remember { mutableStateOf<List<String>>(emptyList()) }
    val options = listOf("Option 1", "Option 2", "Option 3", "Option 4")

    InfoCard {
        SectionHeader(
            title = "📋 Dropdowns",
            description = "Single and multi-select dropdowns"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.normal)) {
            Text("Single Select:", style = MaterialTheme.typography.labelLarge)
            SingleSelectDropdown(
                items = options,
                selectedItem = singleSelection,
                onItemSelected = { singleSelection = it },
                label = "Choose One"
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Multi Select with Chips:", style = MaterialTheme.typography.labelLarge)
            MultiSelectDropdown(
                items = options,
                selectedItems = multiSelection,
                onItemsSelected = { multiSelection = it },
                label = "Choose Multiple"
            )
        }
    }
}

@Composable
fun LoadingViewsSection(scope: kotlinx.coroutines.CoroutineScope) {
    var showFullScreenLoader by remember { mutableStateOf(false) }

    InfoCard {
        SectionHeader(
            title = "⏱️ Loading Views",
            description = "Animated brand loaders"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SecondaryButton(
                text = "Show Full Screen Loader",
                onClick = {
                    showFullScreenLoader = true
                    // Auto-hide after 2 seconds
                    scope.launch {
                        delay(2000)
                        showFullScreenLoader = false
                    }
                }
            )

            Text(
                text = "Compact loader shown above in Progress section",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showFullScreenLoader) {
        LoadingView(message = "Loading data...", fullScreen = true)
    }
}

@Composable
fun PickersSection() {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }

    InfoCard {
        SectionHeader(
            title = "📅 Date & Time Pickers",
            description = "Material 3 styled pickers"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.normal)) {
            Text("Date Picker:", style = MaterialTheme.typography.labelLarge)
            AppDatePicker(
                selectedDateMillis = selectedDate,
                onDateSelected = { selectedDate = it },
                label = "Select Date"
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text("Time Picker:", style = MaterialTheme.typography.labelLarge)
            AppTimePicker(
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                onTimeSelected = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                },
                label = "Select Time"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetsSection() {
    var showBasicSheet by remember { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }

    InfoCard {
        SectionHeader(
            title = "📱 Bottom Sheets",
            description = "Modal bottom sheets with drag handle"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            SecondaryButton(
                text = "Show Basic Sheet",
                onClick = { showBasicSheet = true }
            )
            SecondaryButton(
                text = "Show Action Sheet",
                onClick = { showActionSheet = true }
            )
        }
    }

    if (showBasicSheet) {
        AppBottomSheet(
            onDismissRequest = { showBasicSheet = false },
            title = "Bottom Sheet Title"
        ) {
            Text("This is the content of the bottom sheet.")
            Spacer(modifier = Modifier.height(Spacing.normal))
            Text("You can swipe down or tap outside to dismiss.")
        }
    }

    if (showActionSheet) {
        ActionBottomSheet(
            onDismissRequest = { showActionSheet = false },
            title = "Confirm Action",
            confirmButtonText = "Confirm",
            onConfirmClick = { showActionSheet = false }
        ) {
            Text("This bottom sheet has action buttons built in.")
        }
    }
}

@Composable
fun CardsSection() {
    InfoCard {
        SectionHeader(
            title = "🃏 Cards",
            description = "InfoCard container with elevation"
        )

        Text("This entire section is wrapped in an InfoCard!")
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(
            text = "Cards provide elevated surfaces for content grouping.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LogoSection() {
    InfoCard {
        SectionHeader(
            title = "🔧 App Logo",
            description = "Branded logo with icon and text"
        )

        AppLogo(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun SpacingGuideSection() {
    InfoCard {
        SectionHeader(
            title = "📐 Spacing Guide",
            description = "4dp grid system spacing constants"
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
            SpacingExample("extraSmall", "4dp")
            SpacingExample("small", "8dp")
            SpacingExample("medium", "12dp")
            SpacingExample("normal", "16dp")
            SpacingExample("large", "24dp")
            SpacingExample("extraLarge", "32dp")
            SpacingExample("huge", "48dp")
            SpacingExample("massive", "64dp")
        }
    }
}

@Composable
fun SpacingExample(name: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Spacing.$name",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
