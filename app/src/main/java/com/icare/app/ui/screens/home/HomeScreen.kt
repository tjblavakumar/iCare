package com.icare.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.icare.app.data.model.EmojiStatus
import com.icare.app.ui.components.EmojiButton
import com.icare.app.ui.theme.MediumGrey
import com.icare.app.ui.theme.WarmCoral
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Default 3 emojis
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmojiStatus.DEFAULTS.forEach { emoji ->
                EmojiButton(
                    emojiStatus = emoji,
                    isSelected = uiState.currentStatus?.emojiId == emoji.id,
                    onClick = { viewModel.updateStatus(emoji) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // More emojis toggle
        TextButton(onClick = { viewModel.toggleMoreEmojis() }) {
            Text(
                text = if (uiState.showMoreEmojis) "Show Less" else "+ More",
                color = WarmCoral,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Extra emojis
        AnimatedVisibility(
            visible = uiState.showMoreEmojis,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EmojiStatus.PREDEFINED_EXTRAS.forEach { emoji ->
                    EmojiButton(
                        emojiStatus = emoji,
                        isSelected = uiState.currentStatus?.emojiId == emoji.id,
                        onClick = { viewModel.updateStatus(emoji) },
                        size = 80.dp,
                        emojiSize = 36
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Current status display
        if (uiState.currentStatus != null) {
            val status = uiState.currentStatus!!
            Text(
                text = "Your current status: ${status.emoji} ${status.label}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            status.timestamp?.let { ts ->
                Text(
                    text = "Updated ${formatStatusTimestamp(ts)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MediumGrey,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Snackbar for update messages
        if (uiState.updateMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.clearMessage() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(uiState.updateMessage!!)
            }
        }
    }
}

private fun formatStatusTimestamp(timestamp: Timestamp): String {
    val date = Date(timestamp.seconds * 1000)
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return "at ${sdf.format(date)}"
}
