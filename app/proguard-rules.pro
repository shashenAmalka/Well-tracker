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
#-renamesourcefileattribute SourceFile

# ======================================
# Android 15 16KB Page Size Compatibility
# ======================================

# Keep native methods for proper JNI alignment
-keepclasseswithmembernames class * {
    native <methods>;
}

# Ensure proper alignment of native libraries
-keepclassmembers class * {
    native <methods>;
}

# Keep RenderScript classes (used for blur effects)
-keepclassmembers class androidx.renderscript.** { *; }
-keep class androidx.renderscript.** { *; }
-dontwarn androidx.renderscript.**

# Keep all JNI related classes
-keep class * extends java.lang.Throwable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Optimize native library loading
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ======================================
# App-specific ProGuard rules
# ======================================

# Keep data models for Gson serialization
-keep class com.example.welltracker.data.models.** { *; }
-keepclassmembers class com.example.welltracker.data.models.** { *; }

# Keep SharedPreferences manager
-keep class com.example.welltracker.data.SharedPreferencesManager { *; }
-keepclassmembers class com.example.welltracker.data.SharedPreferencesManager { *; }

# Keep receivers for alarm functionality
-keep class com.example.welltracker.receiver.** { *; }
-keepclassmembers class com.example.welltracker.receiver.** { *; }

# Keep WorkManager workers
-keep class com.example.welltracker.worker.** { *; }
-keepclassmembers class com.example.welltracker.worker.** { *; }

# Keep ViewBinding classes
-keep class com.example.welltracker.databinding.** { *; }

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**