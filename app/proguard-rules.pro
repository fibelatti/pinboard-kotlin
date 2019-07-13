# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-obfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt

-repackageclasses 'fibelatti'

# Debugging
-keepattributes LineNumberTable, SourceFile

# Common attributes
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod, *Annotation*

# Remove log calls
-assumenosideeffects class android.util.Log {
    public static *** d(...);
}

# Kotlin
-keep class kotlin.** { *; }
-dontnote kotlin.coroutines.jvm.internal.DebugMetadataKt**
-dontnote kotlin.internal.PlatformImplementationsKt
-dontnote kotlin.jvm.internal.Reflection

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Material
-keepnames class androidx.** { *; }
-keepnames interface androidx.** { *; }
-keepnames class android.support.** { *; }
-keepnames interface android.support.** { *; }
-keepnames class com.google.android.material.** { *; }

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# OkHttp
-dontwarn okhttp3.**
-dontnote okhttp3.**

# Gson
-dontwarn sun.misc.**
-dontnote com.google.gson.internal.**

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
