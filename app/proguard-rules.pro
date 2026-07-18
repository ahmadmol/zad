# Add project specific ProGuard rules here.
# Kept ready for a future verified R8 enablement.

-keep class com.batoulapps.adhan.** { *; }
-keep class com.batoulapps.adhan2.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Media3 / Quran audio
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Manifest-instantiated components
-keep class com.example.feature.quran.service.QuranAudioService { *; }
-keep class com.example.feature.prayer.worker.PrayerNotificationReceiver { *; }
-keep class com.example.feature.prayer.worker.PrayerSystemReconciliationReceiver { *; }

# Koin constructors used via reflection
-keepclassmembers class * {
    @org.koin.core.annotation.* <init>(...);
}
