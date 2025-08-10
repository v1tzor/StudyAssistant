-keep class app.cash.sqldelight.** { *; }
-keep class kotlin.** { *; }

-keep class com.google.api.client.** { *; }
-keep class com.google.auth.** { *; }

-keep class ru.aleshin.studyassistant.core.api.auth.** { *; }
-keep class ru.aleshin.studyassistant.core.api.auth.WebAuthComponent$Companion { *; }
-keep class ru.aleshin.studyassistant.core.api.auth.KeepAliveService { *; }
-keep class * implements java.util.concurrent.Flow { *; }
-keep class android.net.Uri { *; }
-keep class java.net.URL { *; }

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-printmapping mapping.txt


-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn com.squareup.okhttp.CipherSuite
-dontwarn com.squareup.okhttp.ConnectionSpec
-dontwarn com.squareup.okhttp.TlsVersion
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
-dontwarn kotlin.native.HiddenFromObjC
-dontwarn kotlin.native.ObjCName

-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}