package com.icare.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.icare.app.data.model.AppNotification
import com.icare.app.ui.theme.BadRed
import com.icare.app.ui.theme.CardBackground
import com.icare.app.ui.theme.CardTextPrimary
import com.icare.app.ui.theme.CardTextSecondary
import com.icare.app.ui.theme.LowAmber
import com.icare.app.ui.theme.WarmCoral
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Alerts", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCoral),
            actions = {
                if (uiState.notifications.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.deleteAllNotifications() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", color = Color.White)
                    }
                }
            }
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WarmCoral)
                }
            }

            uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "\uD83D\uDD14", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notifications yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You'll be notified when someone in your circle feels low or bad.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onRead = { viewModel.markAsRead(notification.id) },
                            onDelete = { viewModel.deleteNotification(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onRead: () -> Unit,
    onDelete: () -> Unit
) {
    val isAccountDeleted = notification.type == "account_deleted"
    val isConnectionRequest = notification.type == "connection_request"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.read) 1.dp else 3.dp),
        onClick = { if (!notification.read) onRead() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unread indicator
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isAccountDeleted) BadRed else WarmCoral)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = notification.emoji.ifEmpty { if (isAccountDeleted || isConnectionRequest) "👋" else "😊" },
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isAccountDeleted -> notification.message.ifEmpty { 
                            "${notification.fromDisplayName.ifEmpty { "Someone" }} has left iCare" 
                        }
                        isConnectionRequest -> notification.message.ifEmpty {
                            "${notification.fromDisplayName.ifEmpty { "Someone" }} wants to connect"
                        }
                        notification.fromDisplayName.isNotEmpty() -> "${notification.fromDisplayName} is ${notification.label.lowercase()}"
                        else -> "Someone is ${notification.label.lowercase()}"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold
                    ),
                    color = CardTextPrimary
                )

                notification.timestamp?.let { ts ->
                    Text(
                        text = formatNotificationTime(ts),
                        style = MaterialTheme.typography.labelSmall,
                        color = CardTextSecondary
                    )
                }
            }

            // Severity indicator
            val severityColor = when {
                isAccountDeleted -> BadRed
                notification.label.contains("bad", ignoreCase = true) -> BadRed
                notification.label.contains("low", ignoreCase = true) -> LowAmber
                else -> Color.Transparent
            }

            if (severityColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(severityColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CardTextSecondary
                )
            }
        }
    }
}

private fun formatNotificationTime(timestamp: Timestamp): String {
    val date = Date(timestamp.seconds * 1000)
    val now = Date()
    val diffMs = now.time - date.time
    val diffMin = diffMs / (1000 * 60)
    val diffHour = diffMs / (1000 * 60 * 60)
    val diffDay = diffMs / (1000 * 60 * 60 * 24)

    return when {
        diffMin < 1 -> "Just now"
        diffMin < 60 -> "${diffMin}m ago"
        diffHour < 24 -> "${diffHour}h ago"
        diffDay < 2 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(date)
        }
    }
}
