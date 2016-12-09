# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/kevinroll/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep Event Bus event handler methods.
-keepclassmembers class com.appropel.** {
    void onEvent*(***);
}

# ButterKnife.
# Retain generated class which implement ViewBinder.
-keep public class * implements butterknife.internal.ViewBinder { public <init>(); }

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinder.
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Saripaar
-keep class com.mobsandgeeks.saripaar.** {*;}
-keep @com.mobsandgeeks.saripaar.annotation.ValidateUsing class * {*;}

-keep class com.appropel.kearsarge.view.validation.ValidLatLngRule {*;}
-keep class com.appropel.kearsarge.view.validation.ValidRouteRule {*;}

# Preference binding
-keep class me.denley.preferencebinder.** { *; }
-dontwarn me.denley.preferencebinder.internal.**
-keep class **$$SharedPreferenceBinder { *; }

-keepclasseswithmembernames class * {
    @me.denley.preferencebinder.* <fields>;
}

-keepclasseswithmembernames class * {
    @me.denley.preferencebinder.* <methods>;
}

# Suppress warnings
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
