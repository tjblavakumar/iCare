package com.icare.app.data.model

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val type: String = "status_update",  // "status_update", "account_deleted", "connection_request"
    val fromUserId: String = "",
    val fromDisplayName: String = "",
    val emoji: String = "",
    val label: String = "",
    val message: String = "",  // Custom message for account_deleted type
    val timestamp: Timestamp? = null,
    val read: Boolean = false
)
