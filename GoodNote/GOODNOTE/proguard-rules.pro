# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\KIT\sdk-19\sdk/tools/proguard/proguard-android.txt
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
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-ignorewarning

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.view.View
-keep public class * implements java.io.Serializable

-keepattributes *Annotation*
-keepattributes Signature

#-keep class com.android.gnotes.

-keepattributes JavascriptInterface

-keepattributes Exceptions,InnerClasses,Signature
-keepattributes SourceFile,LineNumberTable

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements java.io.Serializable {
	<fields>;
	<methods>;
}


# Remove log codes
-assumenosideeffects class android.util.Log {
   public static int d(java.lang.String, java.lang.String);
   public static int i(java.lang.String, java.lang.String);
   public static int e(java.lang.String, java.lang.String);
   public static int w(java.lang.String, java.lang.String);
   public static int v(java.lang.String, java.lang.String);
}

# Remove log codes at com.androidplus.util.LogUtils
-assumenosideeffects class com.androidplus.util.LogUtils{
   public static void d(java.lang.String, java.lang.String);
   public static void i(java.lang.String, java.lang.String);
   public static void e(java.lang.String, java.lang.String);
   public static void w(java.lang.String, java.lang.String);
   public static void v(java.lang.String, java.lang.String);
}

-keepclassmembers class * extends android.view.View {
	<methods>;
}

-keepclassmembers class ** {
    public void onEvent*(**);
}

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep class android.support.v4.**{*;}

-keep class **.R$* {*;}
-keep class **.R{*;}
-dontwarn **.R$*

-dontshrink

#注释调来利用proguard打包时的优化，打release包时自动去掉log
#-dontoptimize

-dontwarn android.webkit.WebView
