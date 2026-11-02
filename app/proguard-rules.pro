# Add project specific ProGuard / R8 rules.

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
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Manifest-instantiated components
-keep class com.example.feature.quran.service.QuranAudioService { *; }
-keep class com.example.feature.prayer.worker.PrayerNotificationReceiver { *; }
-keep class com.example.feature.prayer.worker.PrayerSystemReconciliationReceiver { *; }
-keep class com.example.mol.MainActivity { *; }
-keep class com.example.mol.IhsanApp { *; }
-keep class com.example.mol.BarakahApp { *; }

# ViewModels / Koin constructor injection
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.example.feature.** { <init>(...); }
-keep class com.example.mol.di.** { *; }

# Kotlin Serialization (if used by feature models)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.example.feature.**$$serializer { *; }
-keepclassmembers class com.example.feature.** {
    *** Companion;
}
