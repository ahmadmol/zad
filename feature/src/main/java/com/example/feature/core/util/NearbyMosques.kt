package com.example.feature.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/** The map provider owns location and results. Ihsan does not fabricate a mosque directory. */
fun openNearbyMosques(context: Context) {
    val opened = openExternalActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=mosques+near+me")), onUnavailable = {})
    if (!opened) openExternalActivity(context, Intent(Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=mosques+near+me")))
}
