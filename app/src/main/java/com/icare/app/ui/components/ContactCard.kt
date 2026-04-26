package com.icare.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.icare.app.data.model.EmojiCategory
import com.icare.app.data.model.EmojiStatus
import com.icare.app.data.repository.ContactWithStatus
import com.icare.app.ui.theme.BadRed
import com.icare.app.ui.theme.CardBackground
import com.icare.app.ui.theme.CardTextPrimary
import com.icare.app.ui.theme.CardTextSecondary
import com.icare.app.ui.theme.ClickableBlue
import com.icare.app.ui.theme.HappyGreen
import com.icare.app.ui.theme.InactiveGrey
import com.icare.app.ui.theme.LowAmber
import com.icare.app.ui.theme.SoftSky
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactCard(
    contact: ContactWithStatus,
    onClick: () -> Unit,
    onNameClick: () -> Unit,
    onCallClick: () -> Unit,
    onTextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine color based on emoji category
    val emojiCategory = contact.currentStatus?.emojiId?.let { emojiId ->
        EmojiStatus.allAvailable().find { it.id == emojiId }?.category
    }
    
    val statusColor = when {
        contact.isInactive -> InactiveGrey
        emojiCategory == EmojiCategory.NEGATIVE -> BadRed
        emojiCategory == EmojiCategory.NEUTRAL -> LowAmber
        emojiCategory == EmojiCategory.POSITIVE -> HappyGreen
        else -> HappyGreen // Default for unknown emojis
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
                // Name is clickable to edit nickname
                Text(
                    text = contact.effectiveDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = ClickableBlue,
                    modifier = Modifier.clickable(onClick = onNameClick)
                )
                
                if (contact.customDisplayName != null) {
                    Text(
                        text = contact.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = CardTextSecondary
                    )
                }

                if (contact.isInactive) {
                    Text(
                        text = "Inactive for 48+ hours",
                        style = MaterialTheme.typography.labelSmall,
                        color = InactiveGrey
                    )
                } else {
                    val timestamp = contact.currentStatus?.timestamp
                    if (timestamp != null) {
                        Text(
                            text = "${contact.currentStatus.label} \u2022 ${formatTimestamp(timestamp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
            }

            // Quick actions - only show if contact has a phone number
            if (contact.phone.isNotEmpty()) {
                IconButton(onClick = onCallClick) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = SoftSky
                    )
                }

                IconButton(onClick = onTextClick) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Text",
                        tint = SoftSky
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Timestamp): String {
    val date = Date(timestamp.seconds * 1000)
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()

    val cal = java.util.Calendar.getInstance()
    val todayStart = (cal.clone() as java.util.Calendar).apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val yesterdayStart = (todayStart.clone() as java.util.Calendar).apply {
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        date.time >= todayStart.timeInMillis -> "Today ${sdf.format(date)}"
        date.time >= yesterdayStart.timeInMillis -> "Yesterday ${sdf.format(date)}"
        else -> {
            val dateSdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            dateSdf.timeZone = TimeZone.getDefault()
            dateSdf.format(date)
        }
    }
}
