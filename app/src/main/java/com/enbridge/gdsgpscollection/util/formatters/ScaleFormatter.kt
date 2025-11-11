package com.enbridge.gdsgpscollection.util.formatters

/**
 * Utility object for formatting map scales consistently across the application.
 *
 * Provides standardized formatting methods for map scale values to ensure
 * consistent display in UI components.
 *
 * @author Sathya Narayanan
 */
object ScaleFormatter {

    /**
     * Formats map scale for display with appropriate units (M for millions, K for thousands).
     *
     * Examples:
     * - 5,000,000 → "5.0M"
     * - 50,000 → "50.0K"
     * - 500 → "500"
     *
     * @param scale The map scale value
     * @return Formatted scale string
     */
    fun formatScale(scale: Double): String {
        return when {
            scale >= 1_000_000 -> String.format("%.1fM", scale / 1_000_000)
            scale >= 1_000 -> String.format("%.1fK", scale / 1_000)
            else -> String.format("%.0f", scale)
        }
    }

    /**
     * Formats scale with ratio notation (1:n).
     *
     * Examples:
     * - 5,000,000 → "1:5,000,000"
     * - 50,000 → "1:50,000"
     *
     * @param scale The map scale value
     * @param includeCommas Whether to include comma separators (default: true)
     * @return Formatted scale ratio string
     */
    fun formatScaleRatio(scale: Double, includeCommas: Boolean = true): String {
        val scaleInt = scale.toLong()
        return if (includeCommas) {
            "1:${String.format("%,d", scaleInt)}"
        } else {
            "1:$scaleInt"
        }
    }

    /**
     * Formats scale with compact ratio notation using K/M suffixes.
     *
     * Examples:
     * - 5,000,000 → "1:5M"
     * - 50,000 → "1:50K"
     *
     * @param scale The map scale value
     * @return Compact formatted scale ratio string
     */
    fun formatScaleRatioCompact(scale: Double): String {
        return "1:${formatScale(scale)}"
    }

    /**
     * Formats scale for accessibility/screen readers.
     *
     * Examples:
     * - 5,000,000 → "Scale 1 to 5 million"
     * - 50,000 → "Scale 1 to 50 thousand"
     *
     * @param scale The map scale value
     * @return Human-readable scale description
     */
    fun formatScaleAccessible(scale: Double): String {
        return when {
            scale >= 1_000_000 -> {
                val millions = scale / 1_000_000
                "Scale 1 to ${String.format("%.1f", millions)} million"
            }

            scale >= 1_000 -> {
                val thousands = scale / 1_000
                "Scale 1 to ${String.format("%.0f", thousands)} thousand"
            }

            else -> {
                "Scale 1 to ${String.format("%.0f", scale)}"
            }
        }
    }

    /**
     * Formats scale or returns a placeholder if null/invalid.
     *
     * @param scale The scale value to format (nullable)
     * @param placeholder Placeholder text for invalid values (default: "--")
     * @return Formatted scale or placeholder
     */
    fun formatScaleOrPlaceholder(scale: Double?, placeholder: String = "--"): String {
        return if (scale != null && scale.isFinite() && scale > 0) {
            formatScale(scale)
        } else {
            placeholder
        }
    }

    /**
     * Determines the scale category for UI purposes.
     *
     * @param scale The map scale value
     * @return Scale category (World, Country, Region, City, Street, Building)
     */
    fun getScaleCategory(scale: Double): ScaleCategory {
        return when {
            scale >= 50_000_000 -> ScaleCategory.WORLD
            scale >= 10_000_000 -> ScaleCategory.COUNTRY
            scale >= 1_000_000 -> ScaleCategory.REGION
            scale >= 100_000 -> ScaleCategory.CITY
            scale >= 10_000 -> ScaleCategory.STREET
            else -> ScaleCategory.BUILDING
        }
    }

    /**
     * Scale categories for different zoom levels.
     */
    enum class ScaleCategory {
        WORLD,
        COUNTRY,
        REGION,
        CITY,
        STREET,
        BUILDING
    }
}
