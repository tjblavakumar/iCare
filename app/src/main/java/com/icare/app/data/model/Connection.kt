package com.icare.app.data.model

import com.google.firebase.Timestamp

enum class ConnectionStatus {
    PENDING,
    ACCEPTED
}

data class Connection(
    val id: String = "",
    val userA: String = "",
    val userB: String = "",
    val status: String = ConnectionStatus.PENDING.name.lowercase(),
    val initiatedBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun getOtherUserId(currentUserId: String): String {
        return if (userA == currentUserId) userB else userA
    }

    fun isAccepted(): Boolean = status == ConnectionStatus.ACCEPTED.name.lowercase()
    fun isPending(): Boolean = status == ConnectionStatus.PENDING.name.lowercase()
}
