package com.icare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.icare.app.ui.navigation.ICareNavigation
import com.icare.app.ui.navigation.Screen
import com.icare.app.ui.theme.ICareTheme
import com.icare.app.ui.theme.TextSizeScale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PERMISSIONS_SHOWN = booleanPreferencesKey("permissions_shown")
        val TEXT_SIZE_SCALE = stringPreferencesKey("text_size_scale")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var startDestination by remember { mutableStateOf<String?>(null) }
            var textSizeScale by remember { mutableStateOf(TextSizeScale.NORMAL) }

            LaunchedEffect(Unit) {
                val prefs = dataStore.data.first()
                val onboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: false
                val permissionsShown = prefs[PERMISSIONS_SHOWN] ?: false
                val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
                val savedScale = prefs[TEXT_SIZE_SCALE] ?: TextSizeScale.NORMAL.name
                
                textSizeScale = try {
                    TextSizeScale.valueOf(savedScale)
                } catch (e: Exception) {
                    TextSizeScale.NORMAL
                }

                startDestination = when {
                    !onboardingCompleted -> Screen.Onboarding.route
                    !isLoggedIn -> Screen.Login.route
                    isLoggedIn && !permissionsShown -> Screen.Permissions.route
                    else -> Screen.Home.route
                }

                if (!onboardingCompleted) {
                    dataStore.edit { it[ONBOARDING_COMPLETED] = true }
                }
            }
            
            // Observe text size changes
            LaunchedEffect(Unit) {
                dataStore.data.collect { prefs ->
                    val savedScale = prefs[TEXT_SIZE_SCALE] ?: TextSizeScale.NORMAL.name
                    textSizeScale = try {
                        TextSizeScale.valueOf(savedScale)
                    } catch (e: Exception) {
                        TextSizeScale.NORMAL
                    }
                }
            }

            ICareTheme(textSizeScale = textSizeScale) {
                startDestination?.let { destination ->
                    ICareNavigation(startDestination = destination)
                }
            }
        }
    }
}
