package com.example.feature.quran.util

/**
 * Opens the exact verse on Quran.com in the browser. This does not download or play
 * audio inside the app. In-app playback stays behind [RecitationAvailability.RECORDINGS_APPROVED]
 * until the project owner records a per-reciter license.
 */
object OfficialRecitationLink {
    const val OWNER_APPROVAL =
        "التشغيل الداخلي يحتاج موافقة مالك المشروع على ترخيص كل قارئ قبل تغيير RECORDINGS_APPROVED."

    fun quranComVerse(surahId: Int, ayahNumber: Int): String? {
        if (surahId !in 1..114 || ayahNumber !in 1..286) return null
        return "https://quran.com/$surahId/$ayahNumber"
    }
}
