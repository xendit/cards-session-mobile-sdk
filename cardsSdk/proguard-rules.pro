# Keep CardSession related classes
-keep class com.cards.session.cards.models.** { *; }
-keep class com.cards.session.cards.network.** { *; }
-keep class com.cards.session.cards.sdk.** { *; }
-keep class com.cards.session.cards.ui.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep MethodHandles Lookup
-keepclasseswithmembers class java.lang.invoke.MethodHandles$Lookup {
    *;
}

# Keep toString() methods
-keepclassmembers class * {
    java.lang.String toString();
}

## don't warn on non-existent classes
-dontwarn java.lang.invoke.StringConcatFactory
  