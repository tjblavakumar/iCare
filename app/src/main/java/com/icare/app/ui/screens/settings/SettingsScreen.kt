package com.icare.app.ui.screens.settings

import com.icare.app.BuildConfig
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import com.icare.app.di.dataStore
import com.icare.app.ui.theme.BadRed
import com.icare.app.ui.theme.TextSizeScale
import com.icare.app.ui.theme.WarmCoral
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToManageContacts: () -> Unit,
    onNavigateToPendingRequests: () -> Unit,
    onNavigateToAddContact: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTextSizeDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var currentTextSize by remember { mutableStateOf(TextSizeScale.NORMAL) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Load current text size setting
    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        val savedScale = prefs[stringPreferencesKey("text_size_scale")] ?: TextSizeScale.NORMAL.name
        currentTextSize = try {
            TextSizeScale.valueOf(savedScale)
        } catch (e: Exception) {
            TextSizeScale.NORMAL
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCoral)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // User info
            settingsState.currentUser?.let { user ->
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Show email
                if (user.email.isNotEmpty()) {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show phone if available
                if (user.phone.isNotEmpty()) {
                    Text(
                        text = user.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show recovery email for phone users
                if (user.phone.isNotEmpty() && user.recoveryEmail.isNotEmpty()) {
                    Text(
                        text = "Recovery: ${user.recoveryEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show iCareID if available
                if (user.iCareId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your iCare ID: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = user.iCareId,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmCoral
                        )
                    }
                    Text(
                        text = "Share this ID with friends to connect!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Contacts section
            Text(
                text = "CONTACTS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsItem(
                icon = Icons.Default.Group,
                title = "Manage Contacts",
                subtitle = "View and remove your connections",
                onClick = onNavigateToManageContacts
            )

            SettingsItem(
                icon = Icons.Default.PersonAdd,
                title = "Add Contact",
                subtitle = "Search by email or phone",
                onClick = onNavigateToAddContact
            )

            SettingsItem(
                icon = Icons.Default.Pending,
                title = "Pending Requests",
                subtitle = "Accept or reject connection requests",
                onClick = onNavigateToPendingRequests
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Account section
            Text(
                text = "ACCOUNT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Edit Display Name",
                subtitle = settingsState.currentUser?.displayName ?: "",
                onClick = {
                    editedName = settingsState.currentUser?.displayName ?: ""
                    showEditNameDialog = true
                }
            )

            SettingsItem(
                icon = Icons.Default.TextFields,
                title = "Text Size",
                subtitle = currentTextSize.label,
                onClick = { showTextSizeDialog = true }
            )

            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                onClick = {
                    viewModel.logout()
                    onLogout()
                }
            )

            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                subtitle = "Permanently delete all data",
                titleColor = BadRed,
                onClick = { showDeleteDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "iCare v${BuildConfig.VERSION_NAME}"
            )

            // Show error message if any
            settingsState.message?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearSettingsMessage() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }

        // Loading overlay during deletion
        if (settingsState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WarmCoral)
            }
        }
    }

    // Edit name dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Display Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDisplayName(editedName)
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save", color = WarmCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete account dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "⚠️ Delete Account Permanently?",
                    color = BadRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "This action CANNOT be undone.",
                        fontWeight = FontWeight.Bold,
                        color = BadRed
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("If you delete your account:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• All your data will be permanently erased")
                    Text("• You will be removed from everyone's circle")
                    Text("• Your circle members will be notified")
                    Text("• You'll need to set up everything from scratch if you return")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount { onLogout() }
                    }
                ) {
                    Text("Delete Forever", color = BadRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Keep My Account", color = WarmCoral)
                }
            }
        )
    }

    // Text size dialog
    if (showTextSizeDialog) {
        AlertDialog(
            onDismissRequest = { showTextSizeDialog = false },
            title = { Text("Text Size") },
            text = {
                Column {
                    TextSizeScale.entries.forEach { scale ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentTextSize = scale
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[stringPreferencesKey("text_size_scale")] = scale.name
                                        }
                                    }
                                    showTextSizeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = scale.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (scale == currentTextSize) FontWeight.Bold else FontWeight.Normal,
                                color = if (scale == currentTextSize) WarmCoral else MaterialTheme.colorScheme.onSurface
                            )
                            if (scale == currentTextSize) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text("✓", color = WarmCoral, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTextSizeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = if (titleColor == BadRed) BadRed else WarmCoral
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
