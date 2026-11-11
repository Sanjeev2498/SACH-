# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep transaction entity
-keep class com.example.sachvault.data.** { *; }

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
