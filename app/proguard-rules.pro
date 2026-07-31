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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ── Lottie ────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ── Google Play In-App Review ─────────────────────────────────────────
-keep class com.google.android.play.core.review.** { *; }
-dontwarn com.google.android.play.core.review.**

# ── Media (notification) ──────────────────────────────────────────────
-keep class androidx.media.** { *; }

# ── App models (Parcelize) ────────────────────────────────────────────
-keep class com.example.muzik.myapplication.models.** { *; }

# ── Stack trace debugging ─────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
