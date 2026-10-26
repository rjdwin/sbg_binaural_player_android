# R8 / ProGuard Optimization Rules for Binaural Player

# Keep JNI native methods across all classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Binaural_decoder and callback methods invoked from native C (sbagen.c)
-keep class sbgplay.binaural.in.Binaural_decoder {
    void out(short[]);
}

# Keep ViewModel constructors for Compose Hilt / ViewModel reflection
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
