# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keep class com.hostelhub.data.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Socket.IO
-keep class io.socket.** { *; }
