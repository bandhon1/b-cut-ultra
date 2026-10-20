package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EditorScreen
import com.example.ui.theme.BCutUltraTheme
import com.example.viewmodel.VideoEditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BCutUltraTheme {
                val viewModel: VideoEditorViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()
                EditorScreen(
                    viewModel = viewModel,
                    state = state
                )
            }
        }
    }
}
