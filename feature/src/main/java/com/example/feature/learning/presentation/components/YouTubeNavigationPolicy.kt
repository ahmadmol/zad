package com.example.feature.learning.presentation.components

import java.net.URI

internal object YouTubeNavigationPolicy {
    fun allowsExternal(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme == "https" && uri.rawUserInfo == null && uri.port in setOf(-1, 443) &&
            uri.host?.lowercase() in setOf("youtube.com", "www.youtube.com", "youtu.be")
    }.getOrDefault(false)

    fun allowsMainFrame(value: String): Boolean {
        if (value == "about:blank") return true
        return runCatching {
            val uri = URI(value)
            uri.scheme == "https" && uri.rawUserInfo == null && uri.port in setOf(-1, 443) &&
                uri.host?.lowercase() in setOf("youtube.com", "www.youtube.com") &&
                (uri.rawPath.isNullOrEmpty() || uri.rawPath == "/" ||
                    uri.rawPath.matches(Regex("/embed/[A-Za-z0-9_-]{11}")))
        }.getOrDefault(false)
    }
}
