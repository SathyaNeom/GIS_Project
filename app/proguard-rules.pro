# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# ================================================================================================
# KOTLIN
# ================================================================================================
-dontwarn kotlin.**
-dontwarn org.jetbrains.annotations.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.enbridge.electronicservices.**$$serializer { *; }
-keepclassmembers class com.enbridge.electronicservices.** {
    *** Companion;
}
-keepclasseswithmembers class com.enbridge.electronicservices.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ================================================================================================
# JETPACK COMPOSE
# ================================================================================================
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class androidx.compose.** {
    <init>(...);
}
-dontwarn androidx.compose.**

# Keep Composable functions
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}
-keep @androidx.compose.runtime.Stable class ** { *; }
-keep @androidx.compose.runtime.Immutable class ** { *; }

# ================================================================================================
# HILT / DAGGER
# ================================================================================================
-dontwarn com.google.errorprone.annotations.**
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keepclasseswithmembernames class * {
    @dagger.* <fields>;
}
-keepclasseswithmembernames class * {
    @dagger.* <methods>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <methods>;
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **_Impl { *; }
-keep class **_ViewBinding { *; }

# ================================================================================================
# ROOM DATABASE
# ================================================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep Room generated classes
-keep class **_Impl { *; }
-keep class **$Companion { *; }

# ================================================================================================
# KTOR CLIENT
# ================================================================================================
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class io.ktor.** {
    volatile <fields>;
}
-keepclassmembernames class io.ktor.** {
    <fields>;
}
-dontwarn io.ktor.**
-dontwarn kotlinx.atomicfu.**
-dontwarn org.slf4j.**

# ================================================================================================
# ARCGIS MAPS SDK
# ================================================================================================
-keep class com.arcgismaps.** { *; }
-keep class com.esri.** { *; }
-keepclassmembers class com.arcgismaps.** {
    <init>(...);
    public <methods>;
}
-keepclassmembers class com.esri.** {
    <init>(...);
    public <methods>;
}
-dontwarn com.arcgismaps.**
-dontwarn com.esri.**

# Keep ArcGIS native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ================================================================================================
# DOMAIN MODELS (Keep data classes for serialization)
# ================================================================================================
-keep class com.enbridge.electronicservices.domain.entity.** { *; }
-keep class com.enbridge.electronicservices.data.api.dto.** { *; }
-keep class com.enbridge.electronicservices.data.local.entity.** { *; }

-keepclassmembers class com.enbridge.electronicservices.domain.entity.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class com.enbridge.electronicservices.data.api.dto.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class com.enbridge.electronicservices.data.local.entity.** {
    <init>(...);
    <fields>;
}

# ================================================================================================
# GENERAL ANDROID
# ================================================================================================
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep View constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================================================================================================
# SECURITY & OBFUSCATION
# ================================================================================================
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove printStackTrace in release
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# ================================================================================================
# OPTIMIZATION FLAGS
# ================================================================================================
-optimizationpasses 5
-dontpreverify
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ================================================================================================
# GSON (if used in future)
# ================================================================================================
# Uncomment if you add Gson
#-keepattributes Signature
#-keepattributes *Annotation*
#-dontwarn sun.misc.**
#-keep class com.google.gson.** { *; }
#-keep class * implements com.google.gson.TypeAdapter
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer

# ================================================================================================
# RETROFIT (if used in future)
# ================================================================================================
# Uncomment if you add Retrofit
#-keepattributes Signature, InnerClasses, EnclosingMethod
#-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
#-keepclassmembers,allowshrinking,allowobfuscation interface * {
#    @retrofit2.http.* <methods>;
#}
#-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
#-dontwarn javax.annotation.**
#-dontwarn kotlin.Unit
#-dontwarn retrofit2.KotlinExtensions
#-dontwarn retrofit2.KotlinExtensions$*