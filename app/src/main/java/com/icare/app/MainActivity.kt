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
import com.google.firebase.auth.FirebaseAuth
import com.icare.app.ui.navigation.ICareNavigation
import com.icare.app.ui.navigation.Screen
import com.icare.app.ui.theme.ICareTheme
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ICareTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val onboardingCompleted = dataStore.data
                        .map { prefs -> prefs[ONBOARDING_COMPLETED] ?: false }
                        .first()

                    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

                    startDestination = when {
                        !onboardingCompleted -> Screen.Onboarding.route
                        !isLoggedIn -> Screen.Login.route
                        else -> Screen.Home.route
                    }

                    if (!onboardingCompleted) {
                        dataStore.edit { prefs ->
                            prefs[ONBOARDING_COMPLETED] = true
                        }
                    }
                }

                startDestination?.let { destination ->
                    ICareNavigation(startDestination = destination)
                }
            }
        }
    }
}
