# Add project specific ProGuard rules here.

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson models used by Retrofit
-keep class com.capstone.nik.mixology.Network.remoteModel.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn retrofit2.Platform$Java8
-dontwarn javax.annotation.**
-dontwarn com.google.errorprone.annotations.**
