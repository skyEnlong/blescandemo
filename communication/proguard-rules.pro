# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in G:\adt-bundle-windows-x86_64-20140702\sdk/tools/proguard/proguard-android.txt
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
# 指定代码的压缩级别
-optimizationpasses 5
# 是否使用大小写混合
-dontusemixedcaseclassnames
# 是否混淆第三方jar
-dontskipnonpubliclibraryclasses
# 混淆时是否做预校验
-dontpreverify
# 混淆时是否记录日志
-verbose
# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#-ignorewarnings

#-libraryjars libs/android-support-v4.jar
#-libraryjars libs/baidumapapi_v3_0_0.jar
#-libraryjars libs/commons-codec.jar
#-libraryjars libs/commons-httpclient-3.1.jar
#-libraryjars libs/gson-2.2.2.jar
#-libraryjars libs/httpmime-4.2.jar
#-libraryjars libs/locSDK_3.1.jar
#-libraryjars libs/ShareSDK-Core-2.3.9.jar
#-libraryjars libs/ShareSDK-QQ-2.3.9.jar
#-libraryjars libs/ShareSDK-SinaWeibo-2.3.9.jar
#-libraryjars libs/UPBLEPayService.jar
#-libraryjars libs/UPTsmService.jar

# 保持哪些类不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class android.bluetooth.**{*;}

#gson解析不被混淆
-keep class com.google.**{*;}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe {*;}
#-keep class com.google.gson.stream.** {*;}
# Application classes that will be serialized/deserialized over Gson
-dontwarn com.u14studio.entity.**
-keep class com.u14studio.entity.**{*;}
##---------------End: proguard configuration for Gson  ----------
#shareSDK、
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-dontwarn cn.sharesdk.**
-dontwarn **.R$*
-keep class m.framework.**{*;}
#shareSDK结束

#百度地图不混淆
-dontwarn com.baidu.**
-keep class com.baidu.**{*;}
-keep class vi.com.gdi.bgl.android.**{*;}
-keep class android.content.Context.getExternalFilesDirs
-keep public class * extends android.content.Context.getExternalFilesDirs
#百度地图不混淆结束

-dontwarn org.apache.**
-keep class org.apache.**{*;}
-dontwarn android.support-v4.**
-keep class android.support-v4.**{*;}
-dontwarn com.alipay.android.app.**
-keep class com.alipay.android.app.**{*;}

#-keep class com.communication.ble.**{*;}
-keep class com.communication.bean.**{*;}
-keep interface com.communication.bean.**{*;}
#-keep interface com.communication.ble.**{*;}
-libraryjars UPBLEPayService.jar