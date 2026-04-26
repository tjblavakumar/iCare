package com.icare.app.ui.screens.circle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.icare.app.data.model.StatusEntry
import com.icare.app.ui.components.CompactScreenHeader
import com.icare.app.ui.theme.CardBackground
import com.icare.app.ui.theme.CardTextPrimary
import com.icare.app.ui.theme.CardTextSecondary
import com.icare.app.ui.theme.SoothingBlue
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ContactHistoryScreen(
    contactId: String,
    onBack: () -> Unit,
    viewModel: ContactHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(contactId) {
        viewModel.loadHistory(contactId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CompactScreenHeader(
            title = "Past 7 Days",
            onBack = onBack
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SoothingBlue)
                }
            }

            uiState.history.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history available",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var lastDate = ""
                    items(uiState.history) { entry ->
                        val entryDate = formatDate(entry.timestamp)
                        if (entryDate != lastDate) {
                            lastDate = entryDate
                            if (lastDate.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = entryDate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            HorizontalDivider()
                        }

                        HistoryEntryCard(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: StatusEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.emoji,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = CardTextPrimary
                )
                Text(
                    text = formatTime(entry.timestamp),
                    fontSize = 13.sp,
                    color = CardTextSecondary
                )
            }
        }
    }
}

private fun formatDate(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = Date(timestamp.seconds * 1000)
    val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date)
}

private fun formatTime(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = Date(timestamp.seconds * 1000)
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date)
}
