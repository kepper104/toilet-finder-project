package com.kepper104.toiletseverywhere

import android.content.Context
import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.ui.screen.NavGraphs
import com.kepper104.toiletseverywhere.presentation.ui.state.DarkModeStatus
import com.kepper104.toiletseverywhere.ui.theme.ToiletsEverywhereTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint

val Context.dataStore by preferencesDataStore(name = "toilets_v1")
var isNavStackReady = false

/**
 * App
 *
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
            val enableDarkTheme = when (mainViewModel.settingsState.selectedDarkModeOption){
                DarkModeStatus.FORCED_ON -> true
                DarkModeStatus.FORCED_OFF -> false
                DarkModeStatus.AUTO -> isSystemInDarkTheme()
            }
            Log.d(Tags.CompositionLogger.tag, "Recomposing main")
            ToiletsEverywhereTheme(enableDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DestinationsNavHost(navGraph = NavGraphs.root)
                }
            }
        }
    }
}
