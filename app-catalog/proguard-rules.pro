# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-keep class com.enbridge.gdsgpscollection.catalog.** { *; }
-keep class com.enbridge.gdsgpscollection.designsystem.** { *; }

# Keep all Composable functions
-keep @androidx.compose.runtime.Composable public class * {
    public <methods>;
}
