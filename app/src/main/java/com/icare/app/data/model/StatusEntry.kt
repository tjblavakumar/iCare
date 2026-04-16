package com.icare.app.data.model

import com.google.firebase.Timestamp

data class StatusEntry(
    val id: String = "",
    val emojiId: String = "",
    val emoji: String = "",
    val label: String = "",
    val timestamp: Timestamp? = null,
    val timezone: String = ""
)
