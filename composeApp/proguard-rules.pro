# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.pianoquest.app.**$$serializer { *; }
-keepclassmembers class com.pianoquest.app.** { *** Companion; kotlinx.serialization.KSerializer serializer(...); }
-dontwarn javax.sound.**
