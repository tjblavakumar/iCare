package com.icare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icare.app.data.repository.ContactWithStatus
import com.icare.app.ui.theme.BadRed
import com.icare.app.ui.theme.CardBackground
import com.icare.app.ui.theme.CardTextPrimary
import com.icare.app.ui.theme.CardTextSecondary
import com.icare.app.ui.theme.HappyGreen
import com.icare.app.ui.theme.InactiveGrey
import com.icare.app.ui.theme.LowAmber
import com.icare.app.ui.theme.SoftTeal
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ContactCard(
    contact: ContactWithStatus,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onTextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        contact.isInactive -> InactiveGrey
        contact.currentStatus?.emojiId == "bad" -> BadRed
        contact.currentStatus?.emojiId == "low" -> LowAmber
        contact.currentStatus?.emojiId == "happy" -> HappyGreen
        else -> when {
            contact.currentStatus?.label?.contains("bad", ignoreCase = true) == true -> BadRed
            contact.currentStatus?.label?.contains("low", ignoreCase = true) == true -> LowAmber
            else -> HappyGreen
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            // Emoji
            Text(
                text = if (contact.isInactive) "\u2B55" else (contact.currentStatus?.emoji ?: "\u2B55"),
                fontSize = 36.sp
            )

            // Name and timestamp
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = contact.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CardTextPrimary
                )

                if (contact.isInactive) {
                    Text(
                        text = "Inactive for 48+ hours",
                        fontSize = 14.sp,
                        color = InactiveGrey
                    )
                } else {
                    val timestamp = contact.currentStatus?.timestamp
                    if (timestamp != null) {
                        Text(
                            text = "${contact.currentStatus.label} \u2022 ${formatTimestamp(timestamp)}",
                            fontSize = 14.sp,
                            color = statusColor
                        )
                    }
                }
            }

            // Quick actions
            IconButton(onClick = onCallClick) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = SoftTeal
                )
            }

            IconButton(onClick = onTextClick) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = "Text",
                    tint = SoftTeal
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Timestamp): String {
    val date = Date(timestamp.seconds * 1000)
    val now = Date()
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()

    val dayDiff = (now.time - date.time) / (1000 * 60 * 60 * 24)

    return when {
        dayDiff < 1 -> "Today ${sdf.format(date)}"
        dayDiff < 2 -> "Yesterday ${sdf.format(date)}"
        else -> {
            val dateSdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            dateSdf.timeZone = TimeZone.getDefault()
            dateSdf.format(date)
        }
    }
}
