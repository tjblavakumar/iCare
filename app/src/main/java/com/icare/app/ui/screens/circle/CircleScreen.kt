package com.icare.app.ui.screens.circle

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.icare.app.ui.components.ContactCard
import com.icare.app.ui.components.EditNicknameDialog
import com.icare.app.ui.theme.SoothingBlue
import com.icare.app.ui.theme.WarmCoral

@Composable
fun CircleScreen(
    onContactClick: (String) -> Unit,
    onAddContactClick: () -> Unit,
    viewModel: CircleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Auto-refresh when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Circle",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
            Row {
                IconButton(onClick = onAddContactClick) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Contact",
                        tint = SoothingBlue
                    )
                }
                IconButton(onClick = { viewModel.loadContacts() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = SoothingBlue
                    )
                }
            }
        }
        
        HorizontalDivider(color = SoothingBlue.copy(alpha = 0.3f))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WarmCoral)
                }
            }

            uiState.contacts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "\uD83D\uDC65",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No contacts yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add people from Settings to start seeing their status.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.contacts, key = { it.userId }) { contact ->
                        ContactCard(
                            contact = contact,
                            onClick = { onContactClick(contact.userId) },
                            onNameClick = { viewModel.startEditingNickname(contact) },
                            onCallClick = {
                                val phoneNumber = contact.phone.ifEmpty { contact.email }
                                if (phoneNumber.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:$phoneNumber")
                                    context.startActivity(intent)
                                }
                            },
                            onTextClick = {
                                val phoneNumber = contact.phone.ifEmpty { contact.email }
                                if (phoneNumber.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_SENDTO)
                                    intent.data = Uri.parse("smsto:$phoneNumber")
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    uiState.editingContact?.let { contact ->
        EditNicknameDialog(
            contact = contact,
            onDismiss = { viewModel.cancelEditingNickname() },
            onSave = { nickname -> viewModel.saveNickname(contact.userId, nickname) }
        )
    }
}
