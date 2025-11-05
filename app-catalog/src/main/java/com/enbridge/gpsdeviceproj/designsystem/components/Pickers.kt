package com.enbridge.gpsdeviceproj.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.enbridge.gpsdeviceproj.designsystem.theme.ElectronicServicesTheme
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Custom Date Picker with app theme styling
 * Launches DatePickerDialog when the text field is clicked
 * Returns selected date in milliseconds
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePicker(
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    label: String = "Select Date",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dateFormat: String = "MMM dd, yyyy"
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat(dateFormat, Locale.getDefault()) }

    val displayText = selectedDateMillis?.let {
        dateFormatter.format(Date(it))
    } ?: ""

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = { if (enabled) showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date"
                )
            }
        },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        shape = MaterialTheme.shapes.small
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )

        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = Spacing.large),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.large)
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            headlineContentColor = MaterialTheme.colorScheme.onSurface,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            currentYearContentColor = MaterialTheme.colorScheme.primary,
                            selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        showModeToggle = false
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.medium),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTextButton(
                            text = "Cancel",
                            onClick = { showDialog = false }
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        AppTextButton(
                            text = "OK",
                            onClick = {
                                onDateSelected(datePickerState.selectedDateMillis)
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Time Picker with app theme styling
 * Launches TimePickerDialog when the text field is clicked
 * Returns selected time as hours and minutes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePicker(
    selectedHour: Int?,
    selectedMinute: Int?,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    label: String = "Select Time",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    is24Hour: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    val displayText = if (selectedHour != null && selectedMinute != null) {
        if (is24Hour) {
            String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
        } else {
            val period = if (selectedHour < 12) "AM" else "PM"
            val displayHour = when (selectedHour) {
                0 -> 12
                in 1..12 -> selectedHour
                else -> selectedHour - 12
            }
            String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, selectedMinute, period)
        }
    } else {
        ""
    }

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = { if (enabled) showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Select Time"
                )
            }
        },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        shape = MaterialTheme.shapes.small
    )

    if (showDialog) {
        val currentCalendar = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour ?: currentCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedMinute ?: currentCalendar.get(Calendar.MINUTE),
            is24Hour = is24Hour
        )

        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.medium),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTextButton(
                            text = "Cancel",
                            onClick = { showDialog = false }
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        AppTextButton(
                            text = "OK",
                            onClick = {
                                onTimeSelected(timePickerState.hour, timePickerState.minute)
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AppDatePickerPreview() {
    ElectronicServicesTheme {
        var selectedDate by remember { mutableStateOf<Long?>(null) }

        Column(modifier = Modifier.padding(16.dp)) {
            AppDatePicker(
                selectedDateMillis = selectedDate,
                onDateSelected = { selectedDate = it },
                label = "Select Date"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AppTimePickerPreview() {
    ElectronicServicesTheme {
        var selectedHour by remember { mutableStateOf<Int?>(null) }
        var selectedMinute by remember { mutableStateOf<Int?>(null) }

        Column(modifier = Modifier.padding(16.dp)) {
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
